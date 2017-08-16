package propertypaths;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;

import data.GraphDatabase;

public class PropertyPathEvaluator {

    private ArrayList<QueryTree>		   asts		     = null;
    private HashMap<QueryTree, ArrayList<PairInt>> solutions	     = null;
    private QueryTree				   currentAst	     = null;
    private long				   start	     = 0l;
    private long				   end		     = 0l;

    private long				   astGenerationTime = 0l;
    private long				   astFusionTime     = 0l;
    private long				   evaluationTime    = 0l;

    public PropertyPathEvaluator(String queryPath, String databasePath) {

	// Query parsing (via Jena parser)
	// Creates ASTs in ARQParser
	start = System.nanoTime();
	ARQParser.parse(queryPath, true);	
	end = System.nanoTime();
	astGenerationTime = end - start;
	//ARQParser.getAsts().get(0).print();
	//System.out.println("#####");
	asts = ARQParser.getAsts();
	currentAst = asts.get(0);
	if (!currentAst.isFused()) {
	    start = System.nanoTime();
	    currentAst.fuseGroups();
	    end = System.nanoTime();
	    //currentAst.print();
	    astFusionTime = end - start;
	}
	if (!currentAst.isFixedLeft()) {
	    currentAst.computeSelectivity(currentAst.getRoot());
	    //ARQParser.getAsts().get(0).print();
	    start = System.nanoTime();
	    GraphDatabase.getDatabase().evaluateQueryTreeQP(currentAst.getRoot());
	    end = System.nanoTime();
	    evaluationTime = end - start;
	    //System.out.println("Evaluation time : " + (end - start));
	} else {
	    currentAst.computeSelectivity(currentAst.getRoot());
	    //ARQParser.getAsts().get(0).print();
	    start = System.nanoTime();
	    GraphDatabase.getDatabase().evaluateQueryTree(currentAst.getRoot());
	    end = System.nanoTime();
	    evaluationTime = end - start;
	    //System.out.println("Evaluation time : " + (end - start));  
	}

	solutions = new HashMap<>();
	solutions.put(currentAst, currentAst.getRoot().getCurrentSolutions());

    }

    public ArrayList<PairInt> getSolutionsIntegers(QueryTree qt) {
	return this.solutions.get(qt);
    }

    public ArrayList<Pair<String, String>> getSolutions(QueryTree qt) {

	ArrayList<Pair<String, String>> solutionsString = new ArrayList<>();
	for (PairInt s : solutions.get(currentAst)) {
	    String x = StringEscapeUtils
		    .unescapeJava((String) GraphDatabase.getDatabase().convertIntToStringSO(s.getLeft()));
	    String y = StringEscapeUtils
		    .unescapeJava((String) GraphDatabase.getDatabase().convertIntToStringSO(s.getRight()));
	    solutionsString.add(new Pair<String, String>(x, y));
	}
	return solutionsString;
    }

    public QueryTree getCurrentAst() {
	return this.currentAst;
    }

    public long getAstGenerationTime() {
	return astGenerationTime;
    }

    public long getAstFusionTime() {
	return astFusionTime;
    }

    public long getEvaluationTime() {
	return evaluationTime;
    }

}
