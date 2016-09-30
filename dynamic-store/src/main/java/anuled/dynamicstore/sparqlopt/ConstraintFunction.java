package anuled.dynamicstore.sparqlopt;

import java.util.Set;
import java.util.function.Function;

import org.apache.jena.sparql.core.Var;

/**
 * typedef for function to map from variables to sets of inequality constraints
 * on those variables
 */
interface ConstraintFunction extends Function<Var, Set<InequalityConstraint>> {
}