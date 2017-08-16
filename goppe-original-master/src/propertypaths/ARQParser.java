package propertypaths;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

import data.GraphDatabase;

public class ARQParser {

    private static ArrayList<String>	pps	       = null;
    private static ArrayList<QueryTree>	asts	       = null;
    private static QueryTree		ast	       = null;
    private static String		processedQuery = "";

    public static ArrayList<String> parse(String queryFile, boolean inverser) {
	String queryString = "";
	try {
	    queryString = readFile(queryFile, Charset.forName("UTF-8"));

	} catch (IOException e) {
	    System.err.println("Error while reading query file.");
	}

	// Chronophage
	Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

	if (inverser) {
	    //System.out.println(q.getProjectVars());
	    processedQuery = "SELECT ";
	    if (q.isDistinct())
		processedQuery += "DISTINCT ";
	    for (Var i : q.getProjectVars()) {
		processedQuery += i.toString() + " ";
	    }
	    processedQuery += " WHERE {";
	    ElementWalker.walk(q.getQueryPattern(),
		    // For each element...
		    new ElementVisitorBase() {
			// ...when it's a block of triples...
			public void visit(ElementPathBlock el) {
			    // ...go through all the triples...
			    Iterator<TriplePath> triples = el.patternElts();
			    while (triples.hasNext()) {
				ast = new QueryTree();
				TriplePath t = triples.next();
				Path path = t.getPath();
				Node sub = t.getSubject();
				String subString = sub.toString();
				if (!sub.isVariable())
				    subString = "<" + subString + ">";
				Node obj = t.getObject();
				//System.out.println(path.toString());
				processedQuery += subString + " " + new Inverser(path.toString()).getInversed() + " "
					+ obj.toString() + " . ";
				//System.out.println("processed : " + processedQuery);
			    }
			}
		    });
	    processedQuery += "}";
	    //System.out.println(processedQuery);
	    q = QueryFactory.create(processedQuery, Syntax.syntaxARQ);
	}

	pps = new ArrayList<>();
	asts = new ArrayList<>();
	ast = new QueryTree();
	ElementWalker.walk(q.getQueryPattern(),
		// For each element...
		new ElementVisitorBase() {
		    // ...when it's a block of triples...
		    public void visit(ElementPathBlock el) {
			// ...go through all the triples...
			Iterator<TriplePath> triples = el.patternElts();
			while (triples.hasNext()) {
			    ast = new QueryTree();
			    TriplePath t = triples.next();
			    Path path = t.getPath();
			    Node sub = t.getSubject();
			    Node obj = t.getObject();

			    if (!sub.isVariable()) {
				ast.setFixedLeft();
				int convertedURI = GraphDatabase.getDatabase()
					.convertStringToIntSO("<" + sub.getURI() + ">");
				ast.setFixedStart(convertedURI);
			    }
			    if (!obj.isVariable()) {
				System.err.println("Can't start with fixed node right.");
				return;
			    }

			    path.visit(new PathVisitor() {

				@Override
				public void visit(P_Seq arg0) {

				    TreeNode n = new TreeNode(OPERATOR.SEQ, arg0.getLeft().toString(),
					    arg0.getRight().toString(), ARQParser.ast);
				    ARQParser.ast.add(n);
				    //System.out.println(
				    //	    "SEQ : " + arg0.getLeft().toString() + " / " + arg0.getRight().toString());

				    arg0.getLeft().visit(this);
				    arg0.getRight().visit(this);
				}

				@Override
				public void visit(P_Alt arg0) {

				    TreeNode n = new TreeNode(OPERATOR.ALT, arg0.getLeft().toString(),
					    arg0.getRight().toString(), ARQParser.ast);
				    ARQParser.ast.add(n);

				    arg0.getLeft().visit(this);
				    arg0.getRight().visit(this);
				}

				@Override
				public void visit(P_OneOrMoreN arg0) {

				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_OneOrMore1 arg0) {

				    TreeLeaf l = new TreeLeaf(OPERATOR.OneOrMore, arg0.toString(), ARQParser.ast);
				    ARQParser.ast.add(l);
				}

				@Override
				public void visit(P_ZeroOrMoreN arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_ZeroOrMore1 arg0) {
				    TreeLeaf l = new TreeLeaf(OPERATOR.ZeroOrMore, arg0.toString(), ARQParser.ast);
				    ARQParser.ast.add(l);
				}

				@Override
				public void visit(P_ZeroOrOne arg0) {

				    TreeLeaf l = new TreeLeaf(OPERATOR.ZeroOrOne, arg0.toString(), ARQParser.ast);
				    ARQParser.ast.add(l);
				}

				@Override
				public void visit(P_Shortest arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_Multi arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_Distinct arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_FixedLength arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_Mod arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);
				}

				@Override
				public void visit(P_Inverse arg0) {
				    //System.out.println("On a un inverse : " + arg0.toString());
				    TreeNode n = new TreeNode(OPERATOR.INV, arg0.getSubPath().toString(), null,
					    ARQParser.ast);
				    ARQParser.ast.add(n);
				    arg0.getSubPath().visit(this);
				}

				@Override
				public void visit(P_NegPropSet arg0) {
				    //String sub = arg0.toString().substring(1, arg0.toString().length());
				    //System.out.println("sub : " + sub);

				    TreeLeaf l = new TreeLeaf(OPERATOR.NEG, arg0.toString(), ARQParser.ast);
				    ARQParser.ast.add(l);
				}

				@Override
				public void visit(P_ReverseLink arg0) {
				    System.err.println("Can't read this type of property path. ("
					    + arg0.getClass().getName() + ")" + "\n\t" + arg0);
				    System.exit(1);

				}

				@Override
				public void visit(P_Link arg0) {
				    TreeLeaf l = new TreeLeaf(OPERATOR.Link, arg0.toString(), ARQParser.ast);
				    ARQParser.ast.add(l);

				}
			    });
			    ARQParser.pps.add(path.toString());
			    asts.add(ast);
			}
		    }
		});
	if (q.isDistinct()) {
	    ast.setDistinct(true);
	}
	return pps;
    }

    static String readFile(String path, Charset encoding) throws IOException {
	byte[] encoded = Files.readAllBytes(Paths.get(path));
	return new String(encoded, encoding);
    }

    public static ArrayList<QueryTree> getAsts() {
	return asts;
    }

    public static String getProcessedQuery() {
	return processedQuery;
    }
}
