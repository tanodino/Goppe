package propertypaths;

import java.util.ArrayList;
import java.util.TreeSet;

import com.gs.collections.api.iterator.MutableIntIterator;
import com.gs.collections.impl.map.mutable.primitive.IntBooleanHashMap;

import data.GraphDatabase;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class TreeLeaf extends TreeNode {

    private String	   regex = "";
    private GraphAutomaton ga	 = null;

    public TreeLeaf(OPERATOR o, String r, QueryTree qt) {
	super(o, qt);
	isLeaf = true;
	setRegex(r);
    }

    public TreeLeaf() {
	super();
    }

    public String toString() {
	return this.getOperator() + " " + getRegex();
    }

    public String getRegex() {
	return this.regex;
    }

    public boolean isKleene() {
	return !(this.getOperator() == OPERATOR.Link) && !(this.getOperator() == OPERATOR.NEG);
    }

    public void setRegex(String regex) {
	this.regex = regex;
    }

    public void computeSelectivity() {
	PPSolver solv = new PPSolver(this.getRegex());
	RegExp r = new RegExp(solv.getRegex());
	Automaton a = r.toAutomaton();
	a.determinize();
	ga = new GraphAutomaton(a);

	Long selectivityStart = 0L;
	Long selectivityEnd = 0L;

	StateNode start = ga.getInitialState();
	if (start.isFinal()) {
	    selectivityStart = GraphDatabase.getDatabase().getNodesCount();
	    selectivityEnd = GraphDatabase.getDatabase().getNodesCount();
	} else {
	    for (Integer label : start.getOutLabels()) {
		int newlabel = label;
		if (label < GraphDatabase.getDatabase().getCharacterShift()) {
		    if (GraphDatabase.getDatabase().getPredicatesIndexOut().get(label) != null) {
			selectivityStart += GraphDatabase.getDatabase().getPredicatesIndexOut().get(label).size();
		    }

		} else if (label < 2 * GraphDatabase.getDatabase().getCharacterShift()) {
		    newlabel = label - GraphDatabase.getDatabase().getCharacterShift();
		    if (GraphDatabase.getDatabase().getPredicatesIndexIn().get(newlabel) != null) {
			selectivityStart += GraphDatabase.getDatabase().getPredicatesIndexIn().get(newlabel).size();
		    }

		} else {
		    selectivityStart += GraphDatabase.getDatabase().getNodesCount();
		}
	    }

	    ArrayList<StateNode> finalStates = ga.getFinalStates();
	    for (StateNode f : finalStates) {
		for (Integer label : f.getInLabels()) {
		    int newlabel = label;
		    if (label < GraphDatabase.getDatabase().getCharacterShift()) {
			if (GraphDatabase.getDatabase().getPredicatesIndexIn().get(label) != null) {
			    selectivityEnd += GraphDatabase.getDatabase().getPredicatesIndexIn().get(label).size();
			}

		    } else if (label < 2 * GraphDatabase.getDatabase().getCharacterShift()) {
			newlabel = label - GraphDatabase.getDatabase().getCharacterShift();
			if (GraphDatabase.getDatabase().getPredicatesIndexOut().get(newlabel) != null) {
			    selectivityEnd += GraphDatabase.getDatabase().getPredicatesIndexOut().get(newlabel).size();
			}

		    } else {
			selectivityEnd += GraphDatabase.getDatabase().getNodesCount();
		    }
		}
	    }
	}
	setStartSelectivity(selectivityStart);
	setEndSelectivity(selectivityEnd);

	long min = Long.MAX_VALUE;
	for (StateNode s : this.ga.getStates().values()) {
	    if (s.isFinal() && s.isInit()) {
		min = GraphDatabase.getDatabase().getNodesCount();
		break;
	    }
	    long acc = 0;
	    for (int l : s.getOutLabels()) {
		//System.out.println(l);
		if (l < GraphDatabase.getDatabase().getCharacterShift()) {
		    TreeSet<Integer> neigh = GraphDatabase.getDatabase().getPredicatesIndexOut().get(l);
		    acc += (neigh == null) ? 0L : neigh.size();
		} else if (l < 2 * GraphDatabase.getDatabase().getCharacterShift()) {
		    TreeSet<Integer> neigh = GraphDatabase.getDatabase().getPredicatesIndexIn()
			    .get(l - GraphDatabase.getDatabase().getCharacterShift());
		    acc += (neigh == null) ? 0L : neigh.size();
		} else {
		    IntBooleanHashMap forbiddenOut = GraphDatabase.getDatabase().getNegationSet(l);
		    IntBooleanHashMap forbiddenIn = GraphDatabase.getDatabase().getNegationSetRev(l);
		    if (!forbiddenOut.isEmpty()) {
			MutableIntIterator x = GraphDatabase.getDatabase().getPredicatesIndexOut().keySet()
				.intIterator();
			while (x.hasNext()) {
			    int b = x.next();
			    if (!forbiddenOut.get(b)) {
				acc += GraphDatabase.getDatabase().getPredicatesIndexOut().get(b).size();
			    }
			}
		    }
		    if (!forbiddenIn.isEmpty()) {
			MutableIntIterator x = GraphDatabase.getDatabase().getPredicatesIndexIn().keySet()
				.intIterator();
			while (x.hasNext()) {
			    int b = x.next();
			    if (!forbiddenIn.get(b + GraphDatabase.getDatabase().getCharacterShift())) {
				acc += GraphDatabase.getDatabase().getPredicatesIndexIn().get(b).size();
			    }
			}
		    }
		}
	    }
	    if (min > acc && s.getOutLabels() != null && !s.getOutLabels().isEmpty()) {
		min = acc;
	    }
	}
	//System.out.println("Leaf selectivity " + this.getRegex() + " : " + min);
	this.selectivity = min;
    }

    public GraphAutomaton getGraphAutomaton() {
	return this.ga;
    }
}
