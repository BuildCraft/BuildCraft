/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.NodeStack;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.binary.BiNodeType;
import buildcraft.lib.expression.node.cast.NodeCastLongToDouble;

/* TODO: Use this for the models so that models can increment a variable every tick */
public class NodeStateful implements ITickableNode.Source {
    public final String name;
    public final IVariableNode getter, variable, last, rounderValue;
    final IExpressionNode getterReal;
    final NodeType nodeType;
    private IExpressionNode source, rounder;

    public NodeStateful(String name, NodeType nodeType, IGetterFunc func) throws InvalidExpressionException {
        this.name = name;
        this.nodeType = nodeType;
        this.variable = nodeType.makeVariableNode(name);
        this.last = nodeType.makeVariableNode(name);
        this.rounderValue = nodeType.makeVariableNode(name);
        this.getter = nodeType.makeVariableNode(name);
        this.getterReal = func.createGetter(variable, last);
    }

    @Override
    public void setSource(IExpressionNode source) {
        this.source = source;
    }

    public void setRounder(IExpressionNode rounder) throws InvalidExpressionException {
        this.rounder = nodeType.cast(rounder);
    }

    @Override
    public Instance createTickable() {
        if (source == null) {
            throw new IllegalStateException("source has not been set yet!");
        }
        return new Instance();
    }

    public interface IGetterFunc {
        IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException;
    }

    public class Instance implements ITickableNode {
        public final IVariableNode storedVar, storedLast;

        private Instance() {
            storedVar = nodeType.makeVariableNode(name);
            storedLast = nodeType.makeVariableNode(name);
        }

        public NodeStateful getContainer() {
            return NodeStateful.this;
        }

        @Override
        public void refresh() {
            // Make sure this instance's variables are the ones visible to the getter for the tick
            last.set(storedLast);
            variable.set(storedVar);
            getter.set(getterReal);
        }

        @Override
        public void tick() {
            refresh();
            // Push var down to last
            storedLast.set(storedVar);
            // Use the getter to set the new var
            storedVar.set(source);

            if (rounder != null) {
                last.set(storedLast);
                variable.set(storedVar);

                rounderValue.set(last);
                storedLast.set(rounder);

                rounderValue.set(variable);
                storedVar.set(rounder);
            }
        }
    }

    public enum GetterType implements IGetterFunc {
        USE_VAR {
            @Override
            public IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException {
                return variable;
            }
        },
        USE_LAST {
            @Override
            public IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException {
                return last;
            }
        },
        INTERPOLATE_PARTIAL_TICKS {
            @Override
            public IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException {
                NodeType type = NodeType.getType(variable);
                switch (type) {
                    case DOUBLE: {
                        INodeDouble v = (INodeDouble) variable;
                        INodeDouble l = (INodeDouble) last;
                        INodeDouble p = DefaultContexts.RENDER_PARTIAL_TICKS;
                        // return (l * (1 - p)) + (v * p)
                        INodeDouble _1_minus_p = BiNodeType.SUB.createDoubleNode(NodeConstantDouble.ONE, p);
                        INodeDouble l_times_1_minus_p = BiNodeType.MUL.createDoubleNode(l, _1_minus_p);
                        INodeDouble v_times_p = BiNodeType.MUL.createDoubleNode(v, p);
                        return BiNodeType.ADD.createDoubleNode(l_times_1_minus_p, v_times_p);
                    }
                    case LONG: {
                        INodeLong v = (INodeLong) variable;
                        INodeLong l = (INodeLong) last;
                        INodeDouble p = DefaultContexts.RENDER_PARTIAL_TICKS;

                        // return l + ( round( (v - l) * p ) )

                        INodeLong d = BiNodeType.SUB.createLongNode(l, v);
                        INodeDouble d_as_double = new NodeCastLongToDouble(d);
                        INodeDouble d_times_p = BiNodeType.MUL.createDoubleNode(d_as_double, p);
                        NodeStack stack = new NodeStack(d_times_p);
                        INodeLong round_d_times_p = DefaultContexts.MATH_SCALAR_FUNC_ROUND.getNode(stack);
                        return BiNodeType.ADD.createLongNode(l, round_d_times_p);
                    }
                    default: {
                        throw new InvalidExpressionException("Cannot create an interpolated value for " + type);
                    }
                }
            }
        }
    }
}
