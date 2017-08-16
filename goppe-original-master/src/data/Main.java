package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import benchmark.AmberBench;
import benchmark.VirtuosoBench;
import engines.comparators.JenaComparator;
import engines.evaluators.JenaEvaluator;
import engines.evaluators.JenaEvaluatorFixed;
import engines.evaluators.VirtuosoEvaluator;
import propertypaths.ARQParser;
import propertypaths.Pair;
import propertypaths.PropertyPathEvaluator;

public class Main {

    public static void main(String[] args) throws Exception {

	Logger.getRootLogger().removeAllAppenders();
	Logger.getRootLogger().addAppender(new NullAppender());

	//String databasePath = "examples/testInvDB.nt";
	//String databasePath = "examples/redac.nt";
	//String databasePath = "/home/brett/Downloads/rdf_files/examples/dbpedia.nt";
	String databasePath = "examples/dbpedia.nt";
	//	String queryPath = "/home/brett/Downloads/rdf_files/benchmarks/queries/inverseVirtuoso/queryVirtuoso10.txt";
	//String queryPath = "examples/example.txt";

	String queryPath = "examples/example.txt";
	//GraphDatabase.getDatabase(databasePath);

	simpleQuery(databasePath, queryPath);

	//jenaVsJena(databasePath, queryPath);
	//virtuosoQuery();

	//interactiveQuery();
	//testInverser();
	//virtuosoComparison();
	//bench();
	// test();
	// testOtil();
	// testQuery();
    }

    private static void virtuosoQuery() throws IOException {
	VirtuosoEvaluator ve = new VirtuosoEvaluator("http://d4test.org");

	ArrayList<Pair<String, String>> solutionsAllegro = ve.evaluate("", "examples/example.txt");
	System.out.println("# Virtuoso:\t" + solutionsAllegro.size());
	System.out.println("Virtuoso Evaluation time :\t" + (ve.getEvaluationTime() / 1000000) + " ms");

	for (int i = 0; i < 10; i++) {
	    solutionsAllegro = ve.evaluate("", "examples/example.txt");
	    System.out.println("# Virtuoso:\t" + solutionsAllegro.size());
	    System.out.println("Virtuoso Evaluation time :\t" + (ve.getEvaluationTime() / 1000000) + " ms");
	}
	solutionsAllegro = ve.evaluate("", "examples/example.txt");
	System.out.println("# Virtuoso:\t" + solutionsAllegro.size());
	System.out.println("Virtuoso Evaluation time :\t" + (ve.getEvaluationTime() / 1000000) + " ms");

    }

    public static void jenaVsJena(String databasePath, String query) throws IOException {
	JenaEvaluator je = new JenaEvaluator();
	ArrayList<Pair<String, String>> solutionsJena = je.evaluate(databasePath, query);
	System.out.println("# Jena:\t" + solutionsJena.size());
	System.out.println("Jena Evaluation time :\t" + (je.getEvaluationTime() / 1000000) + " ms");

	ARQParser.parse(query, true);
	query = ARQParser.getProcessedQuery();
	JenaEvaluator je2 = new JenaEvaluator();
	ArrayList<Pair<String, String>> solutionsJena2 = je2.evaluateString(databasePath, query);
	System.out.println("# Jena:\t" + solutionsJena2.size());
	System.out.println("Jena Evaluation time :\t" + (je2.getEvaluationTime() / 1000000) + " ms");

	JenaComparator jc = new JenaComparator(solutionsJena, solutionsJena2);
	jc.checkItself();
	if (!jc.isValid())
	    System.out.println("\tDifferences found. Please see '" + jc.getCheckLog() + "' for more information.");
    }

    private static void simpleQuery(String databasePath, String queryPath) throws Exception {
	//String databasePath = "examples/example2.nt";
	//String databasePath = "examples/dbpedia.nt";
	//String databasePath = "examples/geo2.nt";
	//String databasePath = "/home/minitapir/Downloads/rdf_datasets/geo2.nt";
	//String queryPath = "examples/bench_queries/query2.txt";
	//String queryPath = "examples/bench_queries/query3.txt";
	//String queryPath = "examples/example.txt";

	GraphDatabase.getDatabase(databasePath);
	System.out.println("# triples : " + GraphDatabase.getDatabase().getTriplesCount());
	System.out.println("# predicates : " + GraphDatabase.getDatabase().predicatesNumber() / 2);

	PropertyPathEvaluator ppe = new PropertyPathEvaluator(queryPath, databasePath);
	ArrayList<Pair<String, String>> solutionsGraph = ppe.getSolutions(ppe.getCurrentAst());
	/*
	String pretty = "$\\{";
	for (Pair<String, String> s : solutionsGraph) {
	    String x = "s_" + s.getLeft().substring(s.getLeft().lastIndexOf('/') + 2, s.getLeft().length() - 1);
	    String y = "s_" + s.getRight().substring(s.getRight().lastIndexOf('/') + 2, s.getRight().length() - 1);
	
	    pretty += "(" + x + "," + y + "),";
	}
	pretty = pretty.substring(0, pretty.length() - 1);
	pretty += "\\}$";
	System.out.println(pretty);
	
	String solutionsFile = "solutions.txt";
	BufferedWriter writer = new BufferedWriter(new FileWriter(solutionsFile));
	for (Pair<String, String> solution : solutionsGraph) {
	    writer.write(solution + "\n");
	}
	System.out.println("AMbER Evaluation time : \t" + (ppe.getEvaluationTime() / 1000000) + " ms");
	System.out.println("AMbER Evaluation # : " + solutionsGraph.size());
	
	//JenaEvaluatorFixed je = new JenaEvaluatorFixed();
	JenaEvaluator je = new JenaEvaluator();
	ArrayList<Pair<String, String>> solutionsJena = je.evaluate(databasePath, queryPath);
	System.out.println("# Jena:\t" + solutionsJena.size());
	System.out.println("Jena Evaluation time :\t" + (je.getEvaluationTime() / 1000000) + " ms");
	//System.out.println(solutionsJena);
	
	VirtuosoEvaluator ve = new VirtuosoEvaluator();
	ArrayList<Pair<String, String>> solutionsAllegro = ve.evaluate(databasePath, queryPath);
	System.out.println("# Virtuoso:\t" + solutionsAllegro.size());
	System.out.println("Virtuoso Evaluation time :\t" + (ve.getEvaluationTime() / 1000000) + " ms");
	
	
	AllegroEvaluator ae = new AllegroEvaluator();
	ArrayList<Pair<String, String>> solutionsAllegro = ae.evaluate(databasePath, queryPath);
	System.out.println("# Allegro:\t" + solutionsAllegro.size());
	System.out.println("Allegro Evaluation time :\t" + (ae.getEvaluationTime() / 1000000) + " ms");
	
	JenaComparator jc = new JenaComparator(solutionsGraph, solutionsJena);
	if (!jc.isValid())
	    System.out.println("\tDifferences found. Please see '" + jc.getCheckLog() + "' for more information.");
	*/
    }

    @SuppressWarnings("unused")
    private static void bench() throws IOException, InterruptedException {
	//String databasePath = "examples/geo2.nt";
	//String databasePath = "/home/minitapir/Downloads/rdf_datasets/geo2.nt";
	String databasePath = "examples/dbpedia.nt";
	String queryFolder = "benchmarks/queries/";

	String alt = "altAndSeq/";
	String distinct = "distinct/";
	String inverse = "inverseVirtuoso/";
	String kleene = "kleenexVirtuoso/";
	String ndistinct = "ndistinct/";

	AmberBench j1 = new AmberBench(databasePath, queryFolder + alt, "benchmarks/GoPPe/AMBERaltAndSeqVerif", 10,
		180);
	AmberBench j2 = new AmberBench(databasePath, queryFolder + distinct, "benchmarks/GoPPe/AMBERdistinctVerif", 10,
		180);
	AmberBench j3 = new AmberBench(databasePath, queryFolder + inverse,
		"benchmarks/GoPPe/AMBERinverseVirtuosoVerif", 10, 180);
	AmberBench j4 = new AmberBench(databasePath, queryFolder + kleene, "benchmarks/GoPPe/AMBERkleenexVirtuosoVerif",
		10, 180);

	/*
	AmberBench j5 = new AmberBench(databasePath, queryFolder + ndistinct,
		"benchmarks/GoPPe/AMBERndistinct", 10, 180);
	*/
	/*
	VirtuosoBench p1 = new VirtuosoBench(databasePath, queryFolder + alt, "benchmarks/VirtuosoSuperBenchWarm/dump",
		1, 180);
	VirtuosoBench p2 = new VirtuosoBench(databasePath, queryFolder + distinct,
		"benchmarks/VirtuosoSuperBenchWarm/dump", 1, 180);
	VirtuosoBench p3 = new VirtuosoBench(databasePath, queryFolder + inverse,
		"benchmarks/VirtuosoSuperBenchWarm/dump", 1, 180);
	VirtuosoBench p4 = new VirtuosoBench(databasePath, queryFolder + kleene,
		"benchmarks/VirtuosoSuperBenchWarm/dump", 1, 180);
	VirtuosoBench p5 = new VirtuosoBench(databasePath, queryFolder + ndistinct,
		"benchmarks/VirtuosoSuperBenchWarm/dump", 1, 180);
	
	*/
    }

    private static void interactiveQuery() {

	//String databasePath = "/home/minitapir/Downloads/rdf_datasets/geo2.nt";
	String databasePath = "/home/brett/Downloads/rdf_files/examples/dbpedia.nt";
	//String databasePath = "/home/brett/Downloads/rdf_files/yago_links.nt";
	//String databasePath = "examples/dbpedia.nt";

	// GraphDatabase loading
	long start = System.currentTimeMillis();
	GraphDatabase.getDatabase(databasePath);
	long end = System.currentTimeMillis();
	System.out.println("Graph database creation time : " + (end - start) + " ms");
	System.out.println("# triples : " + GraphDatabase.getDatabase().getTriplesCount());
	System.out.println("# predicates : " + GraphDatabase.getDatabase().predicatesNumber());
	boolean go = true;

	Scanner input = new Scanner(System.in);

	String prefix = "";
	while (go) {
	    try {
		String queryPath = "examples/";
		System.out.println("query file to load (from " + queryPath + prefix + ") : ");
		String path = input.nextLine();
		if (path.startsWith("cd")) {
		    prefix = path.substring(path.indexOf(" ") + 1);
		} else if (path.equals("stop")) {
		    System.out.println("Exiting...");
		    go = false;
		} else {
		    queryPath = queryPath + prefix + path;
		    simpleQuery(databasePath, queryPath);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	input.close();
    }
}
