package anuled.dynamicstore.sparqlopt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

class FilteredBasicPattern extends BasicPattern {
	private Map<Var, Set<InequalityConstraint>> exprsForVar = new HashMap<>();

	public FilteredBasicPattern(BasicPattern original) {
		super(original);
	}

	private void insertConstraint(Var var, InequalityConstraint constraint) {
		Set<InequalityConstraint> constraints;
		if (!exprsForVar.containsKey(var)) {
			constraints = new HashSet<>();
			exprsForVar.put(var, constraints);
		} else {
			constraints = exprsForVar.get(var);
		}
		constraints.add(constraint);
	}

	public void addExprList(ExprList list) {
		for (Expr expr : list) {
			InequalityConstraint.fromExpr(expr).ifPresent(constraint -> {
				// Ensure that we can look up constraints on specific variables.
				// Sometimes a constraint will be inserted twice because of this
				// approach.
				if (constraint.leftIsVar()) {
					insertConstraint(constraint.leftVar(), constraint);
				}
				if (constraint.rightIsVar()) {
					insertConstraint(constraint.rightVar(), constraint);
				}
			});
		}
	}

	public Set<InequalityConstraint> constraintsOn(Var var) {
		return exprsForVar.compute(var,
				(keyVar, set) -> set == null ? new HashSet<>() : set);
	}
}
