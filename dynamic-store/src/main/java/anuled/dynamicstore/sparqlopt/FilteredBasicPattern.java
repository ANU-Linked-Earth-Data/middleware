package anuled.dynamicstore.sparqlopt;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

public class FilteredBasicPattern extends BasicPattern {
	Map<Var, Set<InequalityConstraint>> exprsForVar;
	
	public FilteredBasicPattern(BasicPattern original) {
		super(original);
	}
	
	private void insertConstraint(Var var, InequalityConstraint constraint) {
		Set<InequalityConstraint> constraints;
		if (!exprsForVar.containsKey(var)) {
			constraints = new TreeSet<>();
			exprsForVar.put(var, constraints);
		} else {
			constraints = exprsForVar.get(var);
		}
		constraints.add(constraint);
	}
	
	public void addExprList(ExprList list) {
		for (Expr expr : list) {
			InequalityConstraint.fromExpr(expr).ifPresent(constraint -> {
				if (constraint.leftIsVar()) {
					insertConstraint(constraint.leftVar(), constraint);
				}
				if (constraint.rightIsVar()) {
					insertConstraint(constraint.rightVar(), constraint);
				}
			});
		}
	}
}
