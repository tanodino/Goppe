package engines.evaluators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;

import propertypaths.Pair;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class VirtuosoEvaluator {

    private String databaseURI	   = "";
    private long   evaluationTime  = 0l;
    private int	   solutionsNumber = 0;

    public VirtuosoEvaluator(String databaseURI) {
	this.databaseURI = databaseURI;
    }

    public ArrayList<Pair<String, String>> evaluate(String dataPath, String queryPath) throws IOException {

	ArrayList<Pair<String, String>> solutions = new ArrayList<>();
	VirtGraph set = new VirtGraph("jdbc:virtuoso://localhost:1111/", "dba", "dba");

	String queryString = new String(Files.readAllBytes(Paths.get(queryPath)));
	queryString = queryString.substring(0, queryString.indexOf("?y") + 2) + " from <" + databaseURI + "> "
		+ queryString.substring(queryString.indexOf("?y") + 2);

	Query sparql = QueryFactory.create(queryString, Syntax.syntaxARQ);

	
	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
	ResultSet results = vqe.execSelect();
	Long start = System.nanoTime();
	while (results.hasNext()) {
	    QuerySolution result = results.nextSolution();
	    RDFNode x = result.get("x");
	    String xString = "";
	    if (x == null)
		xString = "fixedLeft";
	    else
		xString = x.toString();
	    RDFNode y = result.get("y");
	    Pair<String, String> sol = new Pair<String, String>(xString, y.toString());
	    solutions.add(sol);
	}
	solutionsNumber = solutions.size();
	evaluationTime = (System.nanoTime() - start);
	return solutions;
    }

    public long getEvaluationTime() {
	return evaluationTime;
    }

    public int getSolutionsNumber() {
	return this.solutionsNumber;
    }
}
