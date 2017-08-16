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

public class JenaEvaluatorFixed extends Evaluator {
    private static Model model = null;

    public ArrayList<Pair<String, String>> evaluate(String dataPath, String queryPath) throws IOException {
	FileManager.get().addLocatorClassLoader(JenaEvaluatorFixed.class.getClassLoader());
	if (model == null)
	    model = FileManager.get().loadModel(dataPath, null, "N-TRIPLES");

	String queryString = new String(Files.readAllBytes(Paths.get(queryPath)));
	Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

	String pattern = query.getQueryPattern().toString();
	String uri = pattern.substring(pattern.indexOf("<") + 1, pattern.indexOf(">"));

	Long start = System.nanoTime();
	try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
	    ResultSet results = qexec.execSelect();
	    for (; results.hasNext();) {
		QuerySolution soln = results.nextSolution();
		RDFNode y = soln.get("y");
		Pair<String, String> sol = new Pair<String, String>(uri, y.toString());
		this.solutions.add(sol);
	    }
	    this.evaluationTime = (System.nanoTime() - start);
	}
	return solutions;
    }
}
