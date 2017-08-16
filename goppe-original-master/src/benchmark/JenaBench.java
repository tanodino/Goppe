package benchmark;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import engines.evaluators.JenaEvaluator;
import propertypaths.Pair;

public class JenaBench extends Bench {

    public JenaBench(String databasePath, String queryFolder, String benchFile, int runs) throws IOException {
	super(databasePath, queryFolder, benchFile, runs);

	PrintWriter writer = new PrintWriter(benchFile, "UTF-8");
	writer.println("query #results Jena_evaluation_time");

	try {
	    JenaEvaluator je = new JenaEvaluator(databasePath);
	    while (!queries.isEmpty()) {
		String query = queries.pollFirst();
		System.out.println("Benching " + query);
		ArrayList<Pair<String, String>> solutionsJena = new ArrayList<>();

		LinkedList<Double> jenaEvaluationTime = new LinkedList<>();
		
		int solutionsNumber = 0;
		for (int i = 0; i < runs; i++) {
		    //System.out.println("> Run " + i);
		    //je = new JenaEvaluator();
		    solutionsJena = je.evaluate(databasePath, query);
		    double evaluationJena = round(je.getEvaluationTime() / 1000000.0, 3);
		    jenaEvaluationTime.add(evaluationJena);
		}
		solutionsNumber = solutionsJena.size();

		Double averageJenaEvaluation = robustMean(jenaEvaluationTime);
		writer.println(query + " " + solutionsNumber + " " + averageJenaEvaluation);
		writer.flush();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    writer.close();
	}
    }

}
