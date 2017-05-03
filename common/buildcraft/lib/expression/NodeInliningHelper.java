package buildcraft.lib.expression;

import java.util.function.BiFunction;
import java.util.function.Function;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public class NodeInliningHelper {
    /** Attempts to inline a specified node with only 1 input (a unary node)
     * 
     * @param node The node that is attempting to inline itself
     * @param subNode The sub-node that should be inlined
     * @param changer A function that will take in the inlined sub node and produce an inlined version of the node
     *            attempting to inline itself.
     * @param inlinedChanger A changer that should return an immutable node from the inlined version.
     * @return A new node that has been inlined fully. */
    public static <F extends IExpressionNode, T extends IExpressionNode> T tryInline(T node, F subNode, Function<F, T> changer, Function<F, T> inlinedChanger) {
        ExpressionDebugManager.debugStart("Inlining " + node);
        // Nothing we can do about these unchecked warnings without making IExpressionNode generic
        @SuppressWarnings("unchecked")
        F subInlined = (F) subNode.inline();
        if (subInlined instanceof IConstantNode) {
            T to = inlinedChanger.apply(subInlined);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
        } else if (subInlined == subNode) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
        } else {
            T to = changer.apply(subInlined);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
        }
    }

    public static <F extends IExpressionNode, T extends IExpressionNode> T tryInline(T node, F subNodeLeft, F subNodeRight, BiFunction<F, F, T> changer, BiFunction<F, F, T> inlinedChanger) {
        ExpressionDebugManager.debugStart("Inlining " + node);
        // Nothing we can do about these unchecked warnings without making IExpressionNode generic
        @SuppressWarnings("unchecked")
        F leftInlined = (F) subNodeLeft.inline();
        @SuppressWarnings("unchecked")
        F rightInlined = (F) subNodeRight.inline();
        if (leftInlined instanceof IConstantNode && rightInlined instanceof IConstantNode) {
            T to = inlinedChanger.apply(leftInlined, rightInlined);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
        } else if (leftInlined == subNodeLeft && rightInlined == subNodeRight) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
        } else {
            T to = changer.apply(leftInlined, rightInlined);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
        }
    }
}
