/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import java.util.function.BiFunction;
import java.util.function.Function;

import ct.buildcraft.lib.expression.api.IConstantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode;

public class NodeInliningHelper {
    /** Attempts to inline a specified node with only 1 input (a unary node)
     * 
     * @param node The node that is attempting to inline itself
     * @param subNode The sub-node that should be inlined
     * @param changer A function that will take in the inlined sub node and produce an inlined version of the node
     *            attempting to inline itself.
     * @param inlinedChanger A changer that should return an immutable node from the inlined version.
     * @return A new node that has been inlined fully. */
    public static <F extends IExpressionNode, T extends IExpressionNode> T tryInline(T node, F subNode,
        Function<F, T> changer, Function<F, T> inlinedChanger) {
        {
            T inlined = (T) OptimizingInliningHelper.tryOptimizedInline(node);
            if (inlined != null) {
                return inlined;
            }
        }
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

    public static <L extends IExpressionNode, R extends IExpressionNode, T extends IExpressionNode> T tryInline(T node,
        L subNodeLeft, R subNodeRight, BiFunction<L, R, T> changer, BiFunction<L, R, T> inlinedChanger) {
        {
            T inlined = (T) OptimizingInliningHelper.tryOptimizedInline(node);
            if (inlined != null) {
                return inlined;
            }
        }
        ExpressionDebugManager.debugStart("Inlining " + node);
        // Nothing we can do about these unchecked warnings without making IExpressionNode generic
        L leftInlined = (L) subNodeLeft.inline();
        R rightInlined = (R) subNodeRight.inline();
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

    public static <A extends IExpressionNode, B extends IExpressionNode, C extends IExpressionNode,
        R extends IExpressionNode> R tryInline(R node, A nodeA, B nodeB, C nodeC, TriFunction<A, B, C, R> changer,
            TriFunction<A, B, C, R> inlinedChanger) {
        {
            R inlined = (R) OptimizingInliningHelper.tryOptimizedInline(node);
            if (inlined != null) {
                return inlined;
            }
        }
        ExpressionDebugManager.debugStart("Inlining " + node);
        // Nothing we can do about these unchecked warnings without making IExpressionNode generic
        A inlinedA = (A) nodeA.inline();
        B inlinedB = (B) nodeB.inline();
        C inlinedC = (C) nodeC.inline();
        if (inlinedA instanceof IConstantNode && inlinedB instanceof IConstantNode
            && inlinedC instanceof IConstantNode) {
            R to = inlinedChanger.apply(inlinedA, inlinedB, inlinedC);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
        } else if (inlinedA == nodeA && inlinedB == nodeB && inlinedC == nodeC) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
        } else {
            R to = changer.apply(inlinedA, inlinedB, inlinedC);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
        }
    }

    public static <A extends IExpressionNode, B extends IExpressionNode, C extends IExpressionNode,
        D extends IExpressionNode, R extends IExpressionNode> R tryInline(R node, A nodeA, B nodeB, C nodeC, D nodeD,
            QuadFunction<A, B, C, D, R> changer, QuadFunction<A, B, C, D, R> inlinedChanger) {
        {
            R inlined = (R) OptimizingInliningHelper.tryOptimizedInline(node);
            if (inlined != null) {
                return inlined;
            }
        }
        ExpressionDebugManager.debugStart("Inlining " + node);
        // Nothing we can do about these unchecked warnings without making IExpressionNode generic
        A inlinedA = (A) nodeA.inline();
        B inlinedB = (B) nodeB.inline();
        C inlinedC = (C) nodeC.inline();
        D inlinedD = (D) nodeD.inline();
        if (inlinedA instanceof IConstantNode && inlinedB instanceof IConstantNode && inlinedC instanceof IConstantNode
            && inlinedD instanceof IConstantNode) {
            R to = inlinedChanger.apply(inlinedA, inlinedB, inlinedC, inlinedD);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
        } else if (inlinedA == nodeA && inlinedB == nodeB && inlinedC == nodeC && inlinedD == nodeD) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
        } else {
            R to = changer.apply(inlinedA, inlinedB, inlinedC, inlinedD);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
        }
    }

    public static <A extends IExpressionNode, B extends IExpressionNode, C extends IExpressionNode,
        D extends IExpressionNode, E extends IExpressionNode, R extends IExpressionNode> R tryInline(R node, A nodeA,
            B nodeB, C nodeC, D nodeD, E nodeE, PentaFunction<A, B, C, D, E, R> changer,
            PentaFunction<A, B, C, D, E, R> inlinedChanger) {
        {
            R inlined = (R) OptimizingInliningHelper.tryOptimizedInline(node);
            if (inlined != null) {
                return inlined;
            }
        }
        ExpressionDebugManager.debugStart("Inlining " + node);
        // Nothing we can do about these unchecked warnings without making IExpressionNode generic
        A inlinedA = (A) nodeA.inline();
        B inlinedB = (B) nodeB.inline();
        C inlinedC = (C) nodeC.inline();
        D inlinedD = (D) nodeD.inline();
        E inlinedE = (E) nodeE.inline();
        if (inlinedA instanceof IConstantNode && inlinedB instanceof IConstantNode && inlinedC instanceof IConstantNode
            && inlinedD instanceof IConstantNode && inlinedE instanceof IConstantNode) {
            R to = inlinedChanger.apply(inlinedA, inlinedB, inlinedC, inlinedD, inlinedE);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
        } else if (inlinedA == nodeA && inlinedB == nodeB && inlinedC == nodeC && inlinedD == nodeD
            && inlinedE == nodeE) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
        } else {
            R to = changer.apply(inlinedA, inlinedB, inlinedC, inlinedD, inlinedE);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
        }
    }

    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    public interface PentaFunction<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }
}
