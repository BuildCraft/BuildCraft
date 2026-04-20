/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.value;

import ct.buildcraft.lib.expression.DefaultContexts;
import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.GenericExpressionCompiler;
import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IVariableNode;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;

public class NodeStateful implements ITickableNode.Source {
    public final String name;
    public final IVariableNode getter, variable, last, rounderValue;
    final IExpressionNode getterReal;
    final Class<?> nodeType;
    private IExpressionNode source, rounder;

    public NodeStateful(String name, Class<?> nodeType, IGetterFunc func) throws InvalidExpressionException {
        this.name = name;
        this.nodeType = nodeType;
        this.variable = NodeTypes.makeVariableNode(nodeType, name);
        this.last = NodeTypes.makeVariableNode(nodeType, name);
        this.rounderValue = NodeTypes.makeVariableNode(nodeType, name);
        this.getter = NodeTypes.makeVariableNode(nodeType, name);
        this.getterReal = func.createGetter(variable, last);
    }

    @Override
    public void setSource(IExpressionNode source) {
        this.source = source;
    }

    public void setRounder(IExpressionNode rounder) throws InvalidExpressionException {
        this.rounder = NodeTypes.cast(rounder, nodeType);
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
            storedVar = NodeTypes.makeVariableNode(nodeType, name);
            storedLast = NodeTypes.makeVariableNode(nodeType, name);
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
            public IExpressionNode createGetter(IVariableNode variable, IVariableNode last)
                throws InvalidExpressionException {
                return variable;
            }
        },
        USE_LAST {
            @Override
            public IExpressionNode createGetter(IVariableNode variable, IVariableNode last)
                throws InvalidExpressionException {
                return last;
            }
        },
        INTERPOLATE_PARTIAL_TICKS {
            @Override
            public IExpressionNode createGetter(IVariableNode variable, IVariableNode last)
                throws InvalidExpressionException {
                Class<?> type = NodeTypes.getType(variable);
                if (type == double.class) {
                    INodeDouble v = (INodeDouble) variable;
                    INodeDouble l = (INodeDouble) last;
                    INodeDouble p = DefaultContexts.RENDER_PARTIAL_TICKS;
                    FunctionContext ctx = new FunctionContext();
                    ctx.putVariable("v", v);
                    ctx.putVariable("l", l);
                    ctx.putVariable("p", p);
                    return GenericExpressionCompiler.compileExpressionDouble("l * (1 - p) + (v * p)", ctx);
                } else if (type == long.class) {
                    INodeLong v = (INodeLong) variable;
                    INodeLong l = (INodeLong) last;
                    INodeDouble p = DefaultContexts.RENDER_PARTIAL_TICKS;

                    // return l + ( round( (v - l) * p ) )
                    FunctionContext ctx = new FunctionContext();
                    ctx.putVariable("v", v);
                    ctx.putVariable("l", l);
                    ctx.putVariable("p", p);
                    return GenericExpressionCompiler.compileExpressionLong("l + ( round( (v - l) * p ) )", ctx);
                } else {
                    throw new InvalidExpressionException("Cannot create an interpolated value for " + type);
                }
            }
        }
    }
}
