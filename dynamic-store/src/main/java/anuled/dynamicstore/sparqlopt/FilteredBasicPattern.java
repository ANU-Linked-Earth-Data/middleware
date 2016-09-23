package anuled.dynamicstore.sparqlopt;

import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

public class FilteredBasicPattern extends BasicPattern {
	Map<Var, Set<InequalityConstraint>> exprsForVar;
	public FilteredBasicPattern(BasicPattern original) {
		super(original);
	}
	
	public void addExprList(ExprList list) {
		for (Expr expr : list) {
			InequalityConstraint.fromExpr(expr);
			// TODO
		}
	}
}
