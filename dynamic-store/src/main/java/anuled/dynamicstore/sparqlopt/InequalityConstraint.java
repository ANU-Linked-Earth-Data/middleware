package anuled.dynamicstore.sparqlopt;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
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

	private static boolean nodeValid(Node node) {
		return node.isLiteral() || node.isVariable();
	}

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}

	public ConstraintType getType() {
		return type;
	}

	public boolean leftIsVar() {
		return left instanceof Var;
	}

	public boolean rightIsVar() {
		return right instanceof Var;
	}

	public Var leftVar() {
		assert leftIsVar();
		return (Var) left;
	}

	public Var rightVar() {
		assert rightIsVar();
		return (Var) left;
	}

	private static Optional<Node> subExprToNode(Expr expr) {
		if (expr instanceof ExprVar) {
			return Optional.of(((ExprVar) expr).asVar());
		}
		if (expr instanceof NodeValue) {
			return Optional.of(((NodeValue) expr).asNode());
		}
		return Optional.empty();
	}

	private static Optional<InequalityConstraint> convertOp(Node left,
			Node right, ExprFunction2 func) {
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

	@Override
	public String toString() {
		return left + " " + type + " " + right;
	}

	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode() ^ type.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof InequalityConstraint)) {
			return false;
		}
		InequalityConstraint otherIC = (InequalityConstraint) other;
		return type.equals(otherIC.getType()) && left.equals(otherIC.getLeft())
				&& right.equals(otherIC.getRight());
	}

	public boolean sameAs(InequalityConstraint other) {
		// Test for Semantic equality. equals() doesn't do this because it's
		// REALLY HARD to make a hash code which reflects semantic equality of
		// Jena nodes (hence the existence of both Node.equals and
		// Node.sameValueAs)
		return type.equals(other.getType()) && left.sameValueAs(other.getLeft())
				&& right.sameValueAs(other.getRight());
	}
}
