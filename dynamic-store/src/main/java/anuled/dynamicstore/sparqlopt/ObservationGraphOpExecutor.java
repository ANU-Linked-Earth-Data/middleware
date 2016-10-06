package anuled.dynamicstore.sparqlopt;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.expr.ExprList;

public class ObservationGraphOpExecutor extends OpExecutor {
	public ObservationGraphOpExecutor(ExecutionContext execCxt) {
		super(execCxt);
	}

	@Override
    public QueryIterator execute(OpFilter opFilter, QueryIterator input) {
		// hack to rewrite numeric constraints (<, >, etc.) as side channel information in a BGP
        Op base = opFilter.getSubOp();
        if (base instanceof OpBGP) {
        	OpBGP baseBGP = (OpBGP) base;
            ExprList exprs = opFilter.getExprs();
            FilteredBasicPattern newPattern = new FilteredBasicPattern(baseBGP.getPattern());
            newPattern.addExprList(exprs);
            OpBGP newBGP = new OpBGP(newPattern);
            OpFilter newFilter = OpFilter.filterDirect(exprs, newBGP);
            return super.execute(newFilter, input);
        }
        return super.execute(opFilter, input);
    }

}
