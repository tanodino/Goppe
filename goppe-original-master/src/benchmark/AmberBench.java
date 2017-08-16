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

import data.GraphDatabase;
import propertypaths.Pair;
import propertypaths.PairInt;
import propertypaths.PropertyPathEvaluator;

public class AmberBench extends Bench {

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
	    ArrayList<PairInt> solutionsGraph = new ArrayList<>();
	    int solutionsNumber = 0;
	    LinkedList<Double> astGenerationTime = new LinkedList<>();
	    LinkedList<Double> astFusionTime = new LinkedList<>();
	    LinkedList<Double> amberEvaluationTime = new LinkedList<>();
	    PropertyPathEvaluator ppe = new PropertyPathEvaluator(this.query, this.database);
	    double generation = round(ppe.getAstGenerationTime() / 1000000.0, 3);
	    double fusion = round(ppe.getAstFusionTime() / 1000000.0, 3);
	    double evaluation = round(ppe.getEvaluationTime() / 1000000.0, 3);
	    solutionsGraph = ppe.getSolutionsIntegers(ppe.getCurrentAst());

	    astGenerationTime.add(generation);
	    astFusionTime.add(fusion);
	    amberEvaluationTime.add(evaluation);
	    solutionsNumber = solutionsGraph.size();
	    solutionsGraph.clear();

	    Double averageAstGenerationTime = robustMean(astGenerationTime);
	    Double averageAstFusionTime = robustMean(astFusionTime);
	    Double averageAmberEvaluation = robustMean(amberEvaluationTime);

	    writer.println(query + " " + solutionsNumber + " " + averageAstGenerationTime + " " + averageAstFusionTime
		    + " " + averageAmberEvaluation);
	    writer.flush();
	}
    }

    public AmberBench(String databasePath, String queryFolder, String benchFile, int runs, int timeout)
	    throws IOException {

	super(databasePath, queryFolder, benchFile, runs);

	GraphDatabase.getDatabase(databasePath);

	PrintWriter writer = new PrintWriter(benchFile + runs + ".csv", "UTF-8");
	writer.println("query #results AST_generation AST_fusion AMbER_evaluation");
	try {
	    while (!queries.isEmpty()) {
		System.gc();
		String query = queries.pollFirst();
		System.out.println("Benching " + query);

		for (int i = 0; i < runs; i++) {
		    //System.out.println("> Run " + i);

		    final Runnable queryRunner = new BenchThread(databasePath, query, writer);

		    final ExecutorService executor = Executors.newSingleThreadExecutor();
		    final Future future = executor.submit(queryRunner);
		    executor.shutdown(); // This does not cancel the already-scheduled task.

		    try {
			future.get(timeout, TimeUnit.SECONDS);
		    } catch (InterruptedException ie) {
			/* Handle the interruption. Or ignore it. */
		    } catch (ExecutionException ee) {
			/* Handle the error. Or ignore it. */
		    } catch (TimeoutException te) {
			System.out.println("Timeout !");
			writer.println(query + " " + -1 + " " + -1 + " " + -1 + " " + -1);
			writer.flush();
		    }
		    if (!executor.isTerminated())
			executor.shutdownNow(); // If you want to stop the code that hasn't finished
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    writer.close();
	}
    }
}
