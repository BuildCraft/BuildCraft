/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.INodeFunc;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import ct.buildcraft.lib.expression.node.cast.NodeCasting;

public class NodeStack implements INodeStack {
    private final List<IExpressionNode> stack = new ArrayList<>();

    private INodeFunc currentlyPopping;
    private List<Class<?>> recordingTypes;
    private int index = 0;

    public NodeStack() {}

    public NodeStack(IExpressionNode... nodes) {
        for (IExpressionNode node : nodes) {
            push(node);
        }
    }

    public <T extends IExpressionNode> T push(T node) {
        stack.add(node);
        ExpressionDebugManager.debugPrintln("Pushed " + node);
        return node;
    }

    public IExpressionNode pop() throws InvalidExpressionException {
        if (stack.isEmpty()) {
            throw new InvalidExpressionException("No more nodes to pop!");
        } else {
            IExpressionNode node = stack.remove(stack.size() - 1);
            ExpressionDebugManager.debugPrintln("Popped " + node);
            return node;
        }
    }

    public IExpressionNode peek() throws InvalidExpressionException {
        if (stack.isEmpty()) {
            throw new InvalidExpressionException("No more nodes to peek!");
        } else {
            return stack.get(stack.size() - 1);
        }
    }

    /** @return A list of the nodes, in the order that they would be in if popped. (SO the first node in the list will
     *         be the first node returned by {@link #peek()} or {@link #pop()} */
    public List<IExpressionNode> peek(int count) throws InvalidExpressionException {
        if (stack.size() < count) {
            throw new InvalidExpressionException("Not enough nodes to peek!");
        }
        List<IExpressionNode> nodes = new ArrayList<>(count);
        int i2 = stack.size() - 1;
        for (int i = count; i > 0; i--) {
            nodes.add(stack.get(i2));
            i2--;
        }
        return nodes;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void setRecorder(List<Class<?>> expected, INodeFunc toTest) throws InvalidExpressionException {
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
            throw new InvalidExpressionException("Only removed " + recordingTypes.subList(0, index)
                + ", expected to remove " + recordingTypes + " for " + currentlyPopping);
        }
        ExpressionDebugManager.debugEnd("Record was correct");
        recordingTypes = null;
        currentlyPopping = null;
        index = 0;
    }

    /** Used to ensure that the {@link INodeFunc} instance behaves the second time and doesn't try to pop off nodes
     * that it wasn't meant to. */
    private void checkTypeMatch(Class<?> type) throws InvalidExpressionException {
        if (recordingTypes == null) {
            return;
        }
        if (index >= recordingTypes.size()) {
            throw new InvalidExpressionException(
                "Attempted to pop off " + type + ", but the function was not allowed to!");
        }
        Class<?> said = recordingTypes.get(index);
        if (said != type) {
            throw new InvalidExpressionException(
                "Attempted to pop off " + type + ", but the function previously popped off !");
        }
        index++;
    }

    @Override
    public String toString() {
        return stack.toString();
    }

    @Override
    public INodeLong popLong() throws InvalidExpressionException {
        checkTypeMatch(long.class);
        IExpressionNode node = pop();
        if (node instanceof INodeLong) {
            return (INodeLong) node;
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to a long!");
        }
    }

    @Override
    public INodeDouble popDouble() throws InvalidExpressionException {
        checkTypeMatch(double.class);
        return NodeCasting.castToDouble(pop());
    }

    @Override
    public INodeBoolean popBoolean() throws InvalidExpressionException {
        checkTypeMatch(boolean.class);
        IExpressionNode node = pop();
        if (node instanceof INodeBoolean) {
            return (INodeBoolean) node;
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to a boolean!");
        }
    }

    @Override
    public <T> INodeObject<T> popObject(Class<T> type) throws InvalidExpressionException {
        checkTypeMatch(type);
        IExpressionNode node = pop();
        if (node instanceof INodeObject) {
            INodeObject<?> nodeObj = (INodeObject<?>) node;
            if (nodeObj.getType() == type) {
                return (INodeObject<T>) nodeObj;
            } else {
                throw new InvalidExpressionException(
                    "Cannot cast " + nodeObj.getType().getSimpleName() + " to " + type.getSimpleName() + "!");
            }
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to " + type.getSimpleName() + "!");
        }
    }
}
