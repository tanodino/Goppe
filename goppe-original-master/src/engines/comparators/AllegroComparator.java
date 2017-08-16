package engines.comparators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import propertypaths.Pair;

public class AllegroComparator extends Comparator {

    private ArrayList<Pair<String, String>> solutionsGraph   = null;
    private ArrayList<Pair<String, String>> solutionsAllegro = null;
    private boolean			    same	     = false;
    private String			    checklog	     = "check.log";

    public AllegroComparator(ArrayList<Pair<String, String>> solutionsGraph,
	    ArrayList<Pair<String, String>> solutionsAllegro) {
	this.solutionsGraph = solutionsGraph;
	this.solutionsAllegro = solutionsAllegro;
	this.same = checkSolutions();
    }

    protected boolean checkSolutions() {
	System.out.println("Checking solutions ...");
	System.out.println("# Graph : " + solutionsGraph.size() + ", # Allegro : " + solutionsAllegro.size());
	HashMap<Pair<String, String>, Boolean> seenInAllegro = new HashMap<>();
	HashMap<Pair<String, String>, Boolean> seenInGraph = new HashMap<>();
	boolean valid = true;
	PrintStream out = null;
	try {
	    out = new PrintStream(new FileOutputStream(checklog));
	} catch (FileNotFoundException e) {
	    System.err.println("Error while trying to write in log file : " + checklog);
	}
	System.setErr(out);

	for (Pair<String, String> sol : solutionsAllegro) {
	    if (seenInAllegro.get(sol) != null) {
		System.err.println("WARNING : Allegro added " + sol + " twice");
	    }
	    seenInAllegro.put(sol, true);
	}
	for (Pair<String, String> sol : solutionsGraph) {
	    String x = sol.getLeft();
	    String y = sol.getRight();
	    if (x.startsWith("<")) {
		x = x.substring(1, x.length() - 1);

	    } else {
		final String regex = "\\\"([\\s\\S.]*)\\\"(@[a-zA-Z]+)?";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(x);
		matcher.find();

		x = matcher.group(1);
		if (matcher.group(2) != null)
		    x += matcher.group(2);

	    }

	    if (y.startsWith("<"))
		y = y.substring(1, y.length() - 1);
	    else {
		final String regex = "\\\"([\\s\\S.]*)\\\"(@[a-zA-Z]+)?";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(y);
		matcher.find();

		y = matcher.group(1);
		if (matcher.group(2) != null)
		    y += matcher.group(2);

	    }

	    sol.setLeft(x.replaceAll("(\\\")", "\\\\\""));
	    sol.setRight(y.replaceAll("(\\\")", "\\\\\""));
	    if (seenInGraph.get(sol) != null) {
		System.err.println("WARNING : Graph added " + sol + " twice");
	    }
	    seenInGraph.put(sol, true);
	}
	for (Pair<String, String> sol : solutionsGraph) {
	    if (seenInAllegro.get(sol) == null) {
		System.err.println("Miss in Allegro : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    }
	}
	for (Pair<String, String> sol : solutionsAllegro) {
	    if (seenInGraph.get(sol) == null) {
		System.err.println("Miss in graph : " + sol.getLeft() + " " + sol.getRight());
		valid = false;
	    }
	}

	if (solutionsGraph.size() != solutionsAllegro.size()) {
	    System.err.println("# Allegro : " + solutionsAllegro.size() + ", " + "# Graph : " + solutionsGraph.size());
	    valid = false;
	}

	System.out.println("Done.");
	return valid;

    }

    public boolean isValid() {
	return same;
    }

    public String getCheckLog() {
	return checklog;
    }

}
