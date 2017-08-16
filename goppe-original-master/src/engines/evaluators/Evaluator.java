package engines.evaluators;

import java.util.ArrayList;

import propertypaths.Pair;

public abstract class Evaluator {

    protected long			      evaluationTime = 0l;
    protected ArrayList<Pair<String, String>> solutions	     = null;

    public long getEvaluationTime() {
	return evaluationTime;
    }

    public ArrayList<Pair<String, String>> getSolutions() {
	return solutions;
    }

    public abstract ArrayList<Pair<String, String>> evaluate(String database, String query) throws Exception;

    public Evaluator() {
	this.evaluationTime = Long.MAX_VALUE;
	this.solutions = new ArrayList<>();
    }
}
