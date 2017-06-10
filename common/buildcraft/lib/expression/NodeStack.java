/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.cast.NodeCasting;

public class NodeStack implements INodeStack {
    private final Deque<IExpressionNode> stack = new ArrayDeque<>();

    private INodeFunc currentlyPopping;
    private List<NodeType> recordingTypes;
    private int index = 0;

    public NodeStack() {}

    public NodeStack(IExpressionNode... nodes) {
        for (IExpressionNode node : nodes) {
            push(node);
        }
    }

    public <T extends IExpressionNode> T push(T node) {
        stack.push(node);
        ExpressionDebugManager.debugPrintln("Pushed " + node);
        return node;
    }

    public IExpressionNode pop() throws InvalidExpressionException {
        if (stack.isEmpty()) {
            throw new InvalidExpressionException("No more nodes to pop!");
        } else {
            ExpressionDebugManager.debugPrintln("Popped " + stack.peek());
            return stack.pop();
        }
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void setRecorder(List<NodeType> expected, INodeFunc toTest) throws InvalidExpressionException {
        checkAndRemoveRecorder();
        ExpressionDebugManager.debugStart("Recording " + toTest + ", expecting " + expected);
        recordingTypes = new ArrayList<>(expected);
        currentlyPopping = toTest;
        index = 0;
    }

    public void checkAndRemoveRecorder() throws InvalidExpressionException {
        if (recordingTypes == null) {
            return;
        }
        if (index != recordingTypes.size()) {
            throw new InvalidExpressionException("Only removed " + recordingTypes.subList(0, index) + ", expected to remove " + recordingTypes + " for " + currentlyPopping);
        }
        ExpressionDebugManager.debugEnd("Record was correct");
        recordingTypes = null;
        currentlyPopping = null;
        index = 0;
    }

    /** Used to ensure that the {@link INodeFunc} instance behavous the second time and doesn't try to pop off nodes
     * that it wasn't meant to. */
    private void checkTypeMatch(NodeType type) throws InvalidExpressionException {
        if (recordingTypes == null) {
            return;
        }
        if (index >= recordingTypes.size()) {
            throw new InvalidExpressionException("Attempted to pop off " + type + ", but the function was not allowed to!");
        }
        NodeType said = recordingTypes.get(index);
        if (said != type) {
            throw new InvalidExpressionException("Attempted to pop off " + type + ", but the function previously popped off !");
        }
        index++;
    }

    @Override
    public String toString() {
        return stack.toString();
    }

    @Override
    public INodeLong popLong() throws InvalidExpressionException {
        checkTypeMatch(NodeType.LONG);
        IExpressionNode node = pop();
        if (node instanceof INodeLong) {
            return (INodeLong) node;
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to a long!");
        }
    }

    @Override
    public INodeDouble popDouble() throws InvalidExpressionException {
        checkTypeMatch(NodeType.DOUBLE);
        return NodeCasting.castToDouble(pop());
    }

    @Override
    public INodeBoolean popBoolean() throws InvalidExpressionException {
        checkTypeMatch(NodeType.BOOLEAN);
        IExpressionNode node = pop();
        if (node instanceof INodeBoolean) {
            return (INodeBoolean) node;
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to a boolean!");
        }
    }

    @Override
    public INodeString popString() throws InvalidExpressionException {
        checkTypeMatch(NodeType.STRING);
        return NodeCasting.castToString(pop());
    }
}
