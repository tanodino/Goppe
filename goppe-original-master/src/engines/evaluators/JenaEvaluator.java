package engines.evaluators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;

import propertypaths.Pair;

public class JenaEvaluator extends Evaluator {
    private static Model model = null;

    public JenaEvaluator() {
	super();
    }

    public JenaEvaluator(String databasePath) {
	super();
	model = FileManager.get().loadModel(databasePath, null, "N-TRIPLES");
    }

    public ArrayList<Pair<String, String>> evaluate(String dataPath, String queryPath) throws IOException {
	//FileManager.get().addLocatorClassLoader(JenaEvaluator.class.getClassLoader());

	if (model == null)
	    model = FileManager.get().loadModel(dataPath, null, "N-TRIPLES");
	this.evaluationTime = Long.MAX_VALUE;
	this.solutions = new ArrayList<>();
	
	String queryString = new String(Files.readAllBytes(Paths.get(queryPath)));
	Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
	System.out.println(query);
	long start = System.nanoTime();
	try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
	    // Planning execution, not solving
	    ResultSet results = qexec.execSelect();
	    for (; results.hasNext();) {
		QuerySolution soln = results.nextSolution();
		RDFNode x = soln.get("x");
		RDFNode y = soln.get("y");
		Pair<String, String> sol = new Pair<String, String>(x.toString(), y.toString());
		this.solutions.add(sol);
	    }
	    this.evaluationTime = (System.nanoTime() - start);
	}
	return solutions;
    }

    public ArrayList<Pair<String, String>> evaluateString(String dataPath, String queryPath) throws IOException {
	//FileManager.get().addLocatorClassLoader(JenaEvaluator.class.getClassLoader());

	Model model = FileManager.get().loadModel(dataPath, null, "N-TRIPLES");
	System.out.println("Parsing " + queryPath);
	Query query = QueryFactory.create(queryPath, Syntax.syntaxARQ);
	long start = System.nanoTime();
	try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
	    // Planning execution, not solving
	    ResultSet results = qexec.execSelect();
	    for (; results.hasNext();) {
		QuerySolution soln = results.nextSolution();
		RDFNode x = soln.get("x");
		RDFNode y = soln.get("y");
		Pair<String, String> sol = new Pair<String, String>(x.toString(), y.toString());
		this.solutions.add(sol);
	    }
	    this.evaluationTime = (System.nanoTime() - start);
	}
	return solutions;
    }

}
