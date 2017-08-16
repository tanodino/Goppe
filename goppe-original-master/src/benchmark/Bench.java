package benchmark;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import org.apache.jena.ext.com.google.common.math.DoubleMath;

public class Bench {

    protected File[]	      files   = null;
    protected TreeSet<String> queries = null;

    protected Bench(String databasePath, String queryFolder, String benchFile, int runs)
	    throws IOException {
	System.gc();
	queries = new TreeSet<String>();

	files = new File(queryFolder).listFiles();
	if (files == null)
	    throw new IOException(queryFolder + " is not a valid directory.");

	for (File file : files) {
	    if (file.isFile()) {
		queries.add(file.getPath());
	    }
	}
    }

    protected static double robustMean(LinkedList<Double> m) {
	if (m.size() < 3) {
	    //System.err.println("Cannot compute robust mean on array of size " + m.size());
	    double sum = 0;
	    for (double i : m) {
		DoubleMath.roundToLong(i, RoundingMode.HALF_UP);
		sum += i;
	    }
	    return (double) sum / (double) m.size();
	} else {
	    Collections.sort(m);
	    int k = m.size() / 10;

	    for (int i = 0; i < k; i++) {
		m.pollFirst();
		m.pollLast();
	    }

	    double sum = 0;
	    for (double i : m) {
		DoubleMath.roundToLong(i, RoundingMode.HALF_UP);
		sum += i;
	    }
	    return (double) sum / (double) m.size();
	}
    }

    protected static double round(double value, int places) {
	if (places < 0)
	    throw new IllegalArgumentException();

	BigDecimal bd = new BigDecimal(value);
	bd = bd.setScale(places, RoundingMode.HALF_UP);
	return bd.doubleValue();
    }
}
