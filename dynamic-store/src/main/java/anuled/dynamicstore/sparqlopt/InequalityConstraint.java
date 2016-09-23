package anuled.dynamicstore.sparqlopt;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Class to represent (presumably numeric) inequality constraints found in
 * filters. Constraint can be < or <=. Compared values can be literals or
 * variables.
 */
public class InequalityConstraint {
	Node left, right;
	ConstraintType type;

	public static enum ConstraintType {
		// Either left < right or left <= right
		LESS, LESS_EQ
	}

	private static boolean nodeValid(Node node) {
		return node.isLiteral() || node.isVariable();
	}
	
	private static Optional<Node> subExprToNode(Expr expr) {
		if (expr instanceof ExprVar) {
			return Optional.of(((ExprVar)expr).asVar());
		}
		if (expr instanceof NodeValue) {
			return Optional.of(((NodeValue)expr).asNode());
		}
		return Optional.empty();
	}
	
	private static Optional<InequalityConstraint> convertOp(Node left, Node right, ExprFunction2 func) {
		InequalityConstraint rv = null;
		if (func instanceof E_GreaterThan) {
			rv = new InequalityConstraint(right, left, ConstraintType.LESS);
		} else if (func instanceof E_GreaterThanOrEqual) {
			rv = new InequalityConstraint(right, left, ConstraintType.LESS_EQ);
		} else if (func instanceof E_LessThan) {
			rv = new InequalityConstraint(left, right, ConstraintType.LESS);
		} else if (func instanceof E_LessThanOrEqual) {
			rv = new InequalityConstraint(left, right, ConstraintType.LESS_EQ);
		}
		return Optional.ofNullable(rv);
	}

	/**
	 * Try to convert an expr to an InequalityConstraint. Return value will be
	 * empty if conversion failed.
	 */
	public static Optional<InequalityConstraint> fromExpr(Expr expr) {
		if (!(expr instanceof ExprFunction2)) {
			return Optional.empty();
		}
		ExprFunction2 func2 = (ExprFunction2) expr;
		return subExprToNode(func2.getArg1()).flatMap(left -> {
			return subExprToNode(func2.getArg2()).flatMap(right -> {
				return convertOp(left, right, func2);
			});
		});
	}

	public InequalityConstraint(Node left, Node right, ConstraintType type) {
		assert nodeValid(left);
		assert nodeValid(right);
		this.left = left;
		this.right = right;
		this.type = type;
	}
}
