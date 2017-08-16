package engines.evaluators;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.franz.agraph.repository.AGTupleQuery;

import propertypaths.Pair;

public class AllegroEvaluator extends Evaluator {

    public static String SERVER_URL	     = "http://localhost:10035";
    public static String CATALOG_ID	     = "/";
    public static String USERNAME	     = "minitapir";
    public static String PASSWORD	     = "8er9msjc";
    public static String TEMPORARY_DIRECTORY = "";

    /**
     * Creating a Repository test
     */
    public static AGRepositoryConnection load(String repository, boolean close) throws Exception {
	// Tests getting the repository up. 
	println("\nStarting example1().");
	AGServer server = new AGServer(SERVER_URL, USERNAME, PASSWORD);
	//println("Server version: " + server.getVersion());
	//println("Server build date: " + server.getBuildDate());
	//println("Server revision: " + server.getRevision());
	println("Available catalogs: " + server.listCatalogs());
	AGCatalog catalog = server.getCatalog(CATALOG_ID); // open catalog
	//println("Available repositories in catalog " + (catalog.getCatalogName()) + ": " + catalog.listRepositories());
	closeAll();
	repository = repository.substring(repository.indexOf('/') + 1, repository.lastIndexOf('.'));
	println("trying to load " + repository);
	AGRepository myRepository = catalog.createRepository(repository);
	myRepository.initialize();
	//println("Initialized repository.");
	//println("Repository is writable? " + myRepository.isWritable());
	AGRepositoryConnection conn = myRepository.getConnection();
	closeBeforeExit(conn);
	//println("Got a connection.");
	println("Repository " + (myRepository.getRepositoryID()) + " is up! It contains " + (conn.size())
		+ " statements.");

	if (close) {
	    // tidy up
	    conn.close();
	    myRepository.shutDown();
	    return null;
	}
	return conn;
    }

    public ArrayList<Pair<String, String>> evaluate(String database, String queryPath) throws Exception {
	AGRepositoryConnection conn = load(database, false);
	try {
	    String queryString = new String(Files.readAllBytes(Paths.get(queryPath)));
	    println("> Allegro query : " + queryString);
	    AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
	    long start = System.nanoTime();
	    TupleQueryResult result = tupleQuery.evaluate();
	    try {
		while (result.hasNext()) {
		    BindingSet bindingSet = result.next();
		    Value x = bindingSet.getValue("x");
		    Value y = bindingSet.getValue("y");
		    //System.out.format("(%s, %s) \n", x, y);
		    this.solutions.add(new Pair<String, String>(x.stringValue(), y.stringValue()));
		}
		long end = System.nanoTime();
		this.evaluationTime = end - start;
		System.out.println("AllegroGraph time : " + (end - start) + " ms");
	    } finally {
		result.close();
	    }
	} finally {
	    conn.close();
	}
	return this.solutions;
    }

    // PRIVATE METHODS
    private static void println(Object x) {
	System.out.println(x);
    }

    static void close(RepositoryConnection conn) {
	try {
	    conn.close();
	} catch (Exception e) {
	    System.err.println("Error closing repository connection: " + e);
	    e.printStackTrace();
	}
    }

    private static List<RepositoryConnection> toClose = new ArrayList<RepositoryConnection>();

    /**
     * This is just a quick mechanism to make sure all connections get closed.
     */
    private static void closeBeforeExit(RepositoryConnection conn) {
	toClose.add(conn);
    }

    private static void closeAll() {
	while (toClose.isEmpty() == false) {
	    RepositoryConnection conn = toClose.get(0);
	    close(conn);
	    while (toClose.remove(conn)) {
	    }
	}
    }
}