package buildcraft.lib.expression.api;

import java.util.Arrays;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.cast.NodeCasting;

public class Arguments {
    public static final Arguments NO_ARGS = ArgumentCounts.NO_ARGS.createArgs();

    private final ArgumentCounts counts;
    public final INodeLong[] longs;
    public final INodeDouble[] doubles;
    public final INodeBoolean[] bools;
    public final INodeString[] strings;
    private int li, di, bi, si;

    public Arguments(ArgumentCounts counts) {
        this.counts = counts;
        this.longs = new INodeLong[counts.longs];
        this.doubles = new INodeDouble[counts.doubles];
        this.bools = new INodeBoolean[counts.booleans];
        this.strings = new INodeString[counts.strings];
    }

    public void appendNode(IExpressionNode node) {
        if (node instanceof INodeLong) longs[li++] = (INodeLong) node;
        if (node instanceof INodeDouble) doubles[di++] = (INodeDouble) node;
        if (node instanceof INodeBoolean) bools[bi++] = (INodeBoolean) node;
        if (node instanceof INodeString) strings[si++] = (INodeString) node;
    }

    public Arguments castTo(ArgumentCounts counts) throws InvalidExpressionException {
        li = di = bi = si = 0;
        Arguments to = counts.createArgs();
        for (int i = 0; i < counts.order.size(); i++) {
            ArgType oldType = this.counts.order.get(i);
            ArgType newType = counts.order.get(i);
            IExpressionNode node;
            if (oldType == ArgType.LONG) node = longs[li++];
            else if (oldType == ArgType.DOUBLE) node = doubles[di++];
            else if (oldType == ArgType.BOOL) node = bools[bi++];
            else/* if (old == ArgType.STRING) */ node = strings[si++];
            if (newType == ArgType.STRING) {
                node = NodeCasting.castToString(node);
            } else if (newType == ArgType.DOUBLE) {
                node = NodeCasting.castToDouble(node);
            } else if (newType != oldType) {
                throw new InvalidExpressionException("Cannot cast from " + oldType + " to " + newType);
            }
            to.appendNode(node);
        }
        return to;
    }

    @Override
    public String toString() {
        return Arrays.toString(longs) + ", " + Arrays.toString(doubles) + ", " + Arrays.toString(bools) + ", " + Arrays.toString(strings);
    }
}
