package benchmark;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import benchmark.AmberBench.BenchThread;
import engines.evaluators.VirtuosoEvaluator;
import propertypaths.Pair;
import propertypaths.PropertyPathEvaluator;

public class VirtuosoBench extends Bench {
    private String pwd = "password";

    public class BenchThread extends Thread {
	protected String      database = "";
	protected String      query    = "";
	protected PrintWriter writer   = null;

	public BenchThread(String database, String query, PrintWriter writer2) {
	    this.database = database;
	    this.query = query;
	    this.writer = writer2;
	}

	@Override
	public void run() {
	    try {
		/*
		Process pb = new ProcessBuilder("/bin/bash", "-c",
			"echo " + pwd
				+ " | sudo -S /usr/local/virtuoso-opensource/bin/virtuoso-t -f -c /usr/local/virtuoso-opensource/var/lib/virtuoso/db/virtuoso.ini")
					.start();
		VirtuosoEvaluator ve = null;
		boolean keepTrying = true;
		
		while (keepTrying) {
		*/
		try {
		    VirtuosoEvaluator ve = new VirtuosoEvaluator("http://d4test.org");
		    ve.evaluate(database, query);
		    int number = ve.getSolutionsNumber();
		    double evaluationVirtuoso = round(ve.getEvaluationTime() / 1000000.0, 3);
		    writer.println(query + " " + number + " " + evaluationVirtuoso);
		    writer.flush();
		    System.out.println("Done, writing results.");
		    //keepTrying = false;
		} catch (Exception e) {

		    /*if (e.getMessage().contains("Connection")) {
		    Thread.sleep(2000);
		    
		    } else {
		    */
		    System.out.println(e.getMessage());
		    //  keepTrying = false;
		    writer.println(query + " " + -1 + " " + -1);
		    writer.flush();
		    // }
		}
		//}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

    }

    public VirtuosoBench(String databasePath, String queryFolder, String benchFile, int runs, int timeout)
	    throws IOException, InterruptedException {

	super(databasePath, queryFolder, benchFile, runs);

	PrintWriter writer = new PrintWriter(benchFile, "UTF-8");
	writer.println("query #results Virtuoso_evaluation_time");
	while (!queries.isEmpty()) {
	    String query = queries.pollFirst();
	    System.out.println("Benching " + query);

	    for (int i = 0; i < runs; i++) {
		final Runnable queryRunner = new BenchThread(databasePath, query, writer);

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future future = executor.submit(queryRunner);
		executor.shutdown(); // This does not cancel the already-scheduled task.

		try {
		    future.get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
		    ie.printStackTrace();
		} catch (ExecutionException ee) {
		    ee.printStackTrace();
		} catch (TimeoutException te) {
		    System.out.println("Timeout !");
		    writer.println(query + " " + -1 + " " + -1);
		    writer.flush();
		}
		if (!executor.isTerminated())
		    executor.shutdownNow(); // If you want to stop the code that hasn't finished
		/*
		Process stop = new ProcessBuilder("/bin/bash", "-c",
			"echo " + pwd
				+ " | sudo -S kill -2 $(ps -ejH | grep -P \"([0-9])+\\s.*virt\" | grep -oP \"^[0-9]+\")")
					.start();
			
		stop.waitFor();
		Thread.sleep(5000);
		*/
	    }
	}

	writer.close();

    }
}
