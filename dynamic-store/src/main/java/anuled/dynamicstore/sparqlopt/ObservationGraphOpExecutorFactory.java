package anuled.dynamicstore.sparqlopt;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.expr.ExprList;

public class ObservationGraphOpExecutorFactory extends OpExecutor {
	protected ObservationGraphOpExecutorFactory(ExecutionContext execCxt) {
		super(execCxt);
	}

	@Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input) {
		// hack to rewrite numeric constraints (<, >, etc.) as side channel information in a BGP
        Op base = opFilter.getSubOp();
        if (base instanceof OpBGP) {
        	OpBGP baseBGP = (OpBGP) base;
            ExprList exprs = opFilter.getExprs();
            BasicPattern newPattern = new FilteredBasicPattern(baseBGP.getPattern());
            OpBGP newBGP = new OpBGP(newPattern);
            OpFilter newFilter = OpFilter.filterDirect(exprs, newBGP);
            assert false; // TODO: Finish implementing this method using FilteredBasicPattern
            return super.execute(newFilter, input);
        }
        return super.execute(opFilter, input);
    }

}
