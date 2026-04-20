/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.api;

public interface IVariableNode extends IExpressionNode {
    /** Sets the current value of this node to be the value returned by the given node. Note that this does require a
     * cast. This should throw an {@link IllegalArgumentException} or a {@link ClassCastException} if the given node is
     * not of the correct type. */
    void set(IExpressionNode from);

    /** Sets the return value (and subsequent behaviour) of {@link #isConstant()}. */
    void setConstant(boolean isConst);

    /** If this is true, then calls to {@link #inline()} will return an {@link IConstantNode} (which is independent to
     * this node), but if false then {@link #inline()} will return this variable. */
    boolean isConstant();

    public interface IVariableNodeLong extends IVariableNode, INodeLong {
        void set(long value);

        @Override
        default void set(IExpressionNode from) {
            set(((INodeLong) from).evaluate());
        }
    }

    public interface IVariableNodeDouble extends IVariableNode, INodeDouble {
        void set(double value);

        @Override
        default void set(IExpressionNode from) {
            set(((INodeDouble) from).evaluate());
        }
    }

    public interface IVariableNodeBoolean extends IVariableNode, INodeBoolean {
        void set(boolean value);

        @Override
        default void set(IExpressionNode from) {
            set(((INodeBoolean) from).evaluate());
        }
    }

    public interface IVariableNodeObject<T> extends IVariableNode, INodeObject<T> {
        void set(T value);

        default void setUnchecked(Object to) {
            if (to.getClass() != getType()) {
                throw new ClassCastException(to.getClass() + " cannot be cast to " + getType());
            }
            set((T) to);
        }

        @Override
        default void set(IExpressionNode from) {
            setUnchecked(((INodeObject<?>) from).evaluate());
        }
    }
}
