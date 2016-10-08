package buildcraft.lib.expression;

import java.util.function.BiFunction;
import java.util.function.Function;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.IImmutableNode;

public class NodeInliningHelper {
    /** Attempts to inline a specified node with only 1 input (a unary node)
     * 
     * @param node The node that is attempting to inline itself
     * @param args The arguments for inlining
     * @param subNode The sub-node that should be inlined
     * @param changer A function that will take in the inlined sub node and produce an inlined version of the node
     *            attempting to inline itself.
     * @param inlinedChanger A changer that should return an immutable node from the inlined version.
     * @return A new node that has been inlined fully. */
    public static <F extends IExpressionNode, T extends IExpressionNode> T tryInline(T node, Arguments args, F subNode, Function<F, T> changer, Function<F, T> inlinedChanger) {
        GenericExpressionCompiler.debugStart("Inlining " + node);
        F subInlined = (F) subNode.inline(args);
        if (subInlined instanceof IImmutableNode) {
            T to = inlinedChanger.apply(subInlined);
            GenericExpressionCompiler.debugEnd("Fully inlined to " + to);
            return to;
        } else if (subInlined == subNode) {
            GenericExpressionCompiler.debugEnd("Unable to inline at all!");
            return node;
        } else {
            T to = changer.apply(subInlined);
            GenericExpressionCompiler.debugEnd("Partially inlined to " + to);
            return to;
        }
    }

    public static <F extends IExpressionNode, T extends IExpressionNode> T tryInline(T node, Arguments args, F subNodeLeft, F subNodeRight, BiFunction<F, F, T> changer, BiFunction<F, F, T> inlinedChanger) {
        GenericExpressionCompiler.debugStart("Inlining " + node);
        F leftInlined = (F) subNodeLeft.inline(args);
        F rightInlined = (F) subNodeRight.inline(args);
        if (leftInlined instanceof IImmutableNode && rightInlined instanceof IImmutableNode) {
            T to = inlinedChanger.apply(leftInlined, rightInlined);
            GenericExpressionCompiler.debugEnd("Fully inlined to " + to);
            return to;
        } else if (leftInlined == subNodeLeft && rightInlined == subNodeRight) {
            GenericExpressionCompiler.debugEnd("Unable to inline at all!");
            return node;
        } else {
            T to = changer.apply(leftInlined, rightInlined);
            GenericExpressionCompiler.debugEnd("Partially inlined to " + to);
            return to;
        }
    }
}
