package buildcraft.lib.expression.generic.node;

import buildcraft.lib.expression.generic.Arguments;
import buildcraft.lib.expression.generic.Arguments.ArgumentCounts;
import buildcraft.lib.expression.generic.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.generic.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.generic.IExpressionNode.INodeLong;
import buildcraft.lib.expression.generic.IExpressionNode.INodeString;

public class EvaluatableArguments {
    private final Arguments args;
    private final INodeLong[] longs;
    private final INodeDouble[] doubles;
    private final INodeBoolean[] booleans;
    private final INodeString[] strings;

    public EvaluatableArguments(ArgumentCounts counts, INodeLong[] longs, INodeDouble[] doubles, INodeBoolean[] booleans, INodeString[] strings) {
        this.longs = longs;
        this.doubles = doubles;
        this.booleans = booleans;
        this.strings = strings;
        this.args = counts.createArgs();
    }

    public Arguments evaluate(Arguments from) {
        for (int i = 0; i < longs.length; i++) {
            INodeLong node = longs[i];
            args.longs[i] = node.evaluate(from);
        }
        for (int i = 0; i < doubles.length; i++) {
            INodeDouble node = doubles[i];
            args.doubles[i] = node.evaluate(from);
        }
        for (int i = 0; i < booleans.length; i++) {
            INodeBoolean node = booleans[i];
            args.booleans[i] = node.evaluate(from);
        }
        for (int i = 0; i < strings.length; i++) {
            INodeString node = strings[i];
            args.strings[i] = node.evaluate(from);
        }
        return args;
    }
}
