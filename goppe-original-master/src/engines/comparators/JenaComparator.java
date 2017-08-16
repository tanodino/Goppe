package engines.comparators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import propertypaths.Pair;

public class JenaComparator extends Comparator {

    private ArrayList<Pair<String, String>> solutionsGraph = null;
    private ArrayList<Pair<String, String>> solutionsJena  = null;
    private boolean			    same	   = false;
    private String			    checklog	   = "check.log";

    public JenaComparator(ArrayList<Pair<String, String>> solutionsGraph,
	    ArrayList<Pair<String, String>> solutionsJena) {
	this.solutionsGraph = solutionsGraph;
	this.solutionsJena = solutionsJena;
	this.same = checkSolutions();
	//this.same = checkItself();
    }

    protected boolean checkSolutions() {
	System.out.println("Checking solutions ...");
	System.out.println("# Graph : " + solutionsGraph.size() + ", # Jena : " + solutionsJena.size());
	HashMap<Pair<String, String>, Integer> seenInJena = new HashMap<>();
	HashMap<Pair<String, String>, Integer> seenInGraph = new HashMap<>();
	boolean valid = true;
	PrintStream out = null;
	try {
	    out = new PrintStream(new FileOutputStream(checklog));
	} catch (FileNotFoundException e) {
	    System.err.println("Error while trying to write in log file : " + checklog);
	}
	System.setErr(out);

	for (Pair<String, String> sol : solutionsJena) {
	    if (seenInJena.get(sol) != null) {
		System.err.println("WARNING : Jena added " + sol + " twice");
		int i = seenInJena.get(sol);
		seenInJena.put(sol, i++);
	    } else {
		seenInJena.put(sol, 1);
	    }
	}
	for (Pair<String, String> sol : solutionsGraph) {
	    String x = sol.getLeft();
	    String y = sol.getRight();
	    if (x.startsWith("<")) {
		x = x.substring(1, x.length() - 1);

	    } else {
		final String regex = "\\\"([\\s\\S.]*)\\\"(?:(@[a-zA-Z]+)|(?:(\\^\\^)<((?:[^:\\/?#]+:)?(?:\\/\\/[^\\/?#]*?)?[^?#]*?(?:\\\\?[^#]*?)?(?:#.*?)?)>))?";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(x);
		matcher.find();
		x = matcher.group(1);
		if (matcher.group(2) != null)
		    x += matcher.group(2);
		else if (matcher.group(3) != null && matcher.group(4) != null) {
		    x += matcher.group(3) + matcher.group(4);
		}

	    }

	    if (y.startsWith("<"))
		y = y.substring(1, y.length() - 1);
	    else {
		final String regex = "\\\"([\\s\\S.]*)\\\"(?:(@[a-zA-Z]+)|(?:(\\^\\^)<((?:[^:\\/?#]+:)?(?:\\/\\/[^\\/?#]*?)?[^?#]*?(?:\\\\?[^#]*?)?(?:#.*?)?)>))?";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(y);
		matcher.find();

		y = matcher.group(1);
		if (matcher.group(2) != null)
		    y += matcher.group(2);
		else if (matcher.group(3) != null && matcher.group(4) != null) {
		    y += matcher.group(3) + matcher.group(4);
		}
	    }

	    sol.setLeft(x.replaceAll("(\\\")", "\\\\\""));
	    sol.setRight(y.replaceAll("(\\\")", "\\\\\""));
	    if (seenInGraph.get(sol) != null) {
		System.err.println("WARNING : Graph added " + sol + " twice");
		int i = seenInGraph.get(sol);
		seenInGraph.put(sol, i++);
	    } else {
		seenInGraph.put(sol, 1);
	    }
	}
	for (Pair<String, String> sol : solutionsGraph) {
	    if (seenInJena.get(sol) == null) {
		System.err.println("Miss in jena : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    } else if (seenInJena.get(sol) != seenInGraph.get(sol)) {
		System.err.println("Different numbers : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    }
	}
	for (Pair<String, String> sol : solutionsJena) {
	    if (seenInGraph.get(sol) == null) {
		System.err.println("Miss in graph : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    } else if (seenInJena.get(sol) != seenInGraph.get(sol)) {
		System.err.println("Different numbers : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    }
	}

	if (solutionsGraph.size() != solutionsJena.size()) {
	    System.err.println("# Jena : " + solutionsJena.size() + ", " + "# Graph : " + solutionsGraph.size());
	    valid = false;
	}

	System.out.println("Done.");
	return valid;
    }

    public boolean checkItself() {
	System.out.println("Checking solutions ...");
	System.out.println("# Jena1 : " + solutionsGraph.size() + ", # Jena2 : " + solutionsJena.size());
	HashMap<Pair<String, String>, Boolean> seenInJena = new HashMap<>();
	HashMap<Pair<String, String>, Boolean> seenInGraph = new HashMap<>();
	boolean valid = true;
	PrintStream out = null;
	try {
	    out = new PrintStream(new FileOutputStream(checklog));
	} catch (FileNotFoundException e) {
	    System.err.println("Error while trying to write in log file : " + checklog);
	}
	System.setErr(out);

	for (Pair<String, String> sol : solutionsJena) {
	    if (seenInJena.get(sol) != null) {
		System.err.println("WARNING : Jena1 added " + sol + " twice");
	    }
	    seenInJena.put(sol, true);
	}

	for (Pair<String, String> sol : solutionsGraph) {
	    if (seenInGraph.get(sol) != null) {
		System.err.println("WARNING : Jena2 added " + sol + " twice");
	    }
	    seenInGraph.put(sol, true);
	}

	for (Pair<String, String> sol : solutionsGraph) {
	    if (seenInJena.get(sol) == null) {
		System.err.println("Miss in jena1 : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    }
	}
	for (Pair<String, String> sol : solutionsJena) {
	    if (seenInGraph.get(sol) == null) {
		System.err.println("Miss in Jena2 : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    }
	}

	if (solutionsGraph.size() != solutionsJena.size()) {
	    System.err.println("# Jena1 : " + solutionsJena.size() + ", " + "# Jena2 : " + solutionsGraph.size());
	    valid = false;
	}
	same = valid;
	return valid;
    }

    public boolean isValid() {
	return same;
    }

    public String getCheckLog() {
	return checklog;
    }

}
