/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong.IFuncLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong.IFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong.IFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong.IFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongToDouble.IFuncLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToDouble.IFuncDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToBoolean.IFuncLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToBoolean.IFuncDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToBoolean.IFuncBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToBoolean.IFuncBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean.IFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject.IFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToObject.IFuncLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToObject.IFuncLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject.IFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToObject.IFuncBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject.IFuncObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject.IFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject.IFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject;


// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public abstract class FunctionContextBase {

    protected abstract <F extends INodeFunc> F putFunction(String name, F function);

    public  INodeFuncLong put_l_l(String name, IFuncLongToLong func) {
        return putFunction(name, new NodeFuncLongToLong(name, func));
    }

    public  INodeFuncLong put_ll_l(String name, IFuncLongLongToLong func) {
        return putFunction(name, new NodeFuncLongLongToLong(name, func));
    }

    public  INodeFuncLong put_d_l(String name, IFuncDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleToLong(name, func));
    }

    public <A> INodeFuncLong put_o_l(String name, Class<A> argTypeA, IFuncObjectToLong<A> func) {
        return putFunction(name, new NodeFuncObjectToLong<>(name, argTypeA, func));
    }

    public <A> INodeFuncLong put_ol_l(String name, Class<A> argTypeA, IFuncObjectLongToLong<A> func) {
        return putFunction(name, new NodeFuncObjectLongToLong<>(name, argTypeA, func));
    }

    public <A> INodeFuncLong put_oll_l(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> func) {
        return putFunction(name, new NodeFuncObjectLongLongToLong<>(name, argTypeA, func));
    }

    public <A, B> INodeFuncLong put_oo_l(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToLong<A, B> func) {
        return putFunction(name, new NodeFuncObjectObjectToLong<>(name, argTypeA, argTypeB, func));
    }

    public  INodeFuncDouble put_l_d(String name, IFuncLongToDouble func) {
        return putFunction(name, new NodeFuncLongToDouble(name, func));
    }

    public  INodeFuncDouble put_d_d(String name, IFuncDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleToDouble(name, func));
    }

    public  INodeFuncDouble put_dd_d(String name, IFuncDoubleDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleDoubleToDouble(name, func));
    }

    public  INodeFuncBoolean put_ll_b(String name, IFuncLongLongToBoolean func) {
        return putFunction(name, new NodeFuncLongLongToBoolean(name, func));
    }

    public  INodeFuncBoolean put_dd_b(String name, IFuncDoubleDoubleToBoolean func) {
        return putFunction(name, new NodeFuncDoubleDoubleToBoolean(name, func));
    }

    public  INodeFuncBoolean put_b_b(String name, IFuncBooleanToBoolean func) {
        return putFunction(name, new NodeFuncBooleanToBoolean(name, func));
    }

    public  INodeFuncBoolean put_bb_b(String name, IFuncBooleanBooleanToBoolean func) {
        return putFunction(name, new NodeFuncBooleanBooleanToBoolean(name, func));
    }

    public <A> INodeFuncBoolean put_o_b(String name, Class<A> argTypeA, IFuncObjectToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectToBoolean<>(name, argTypeA, func));
    }

    public <A, B> INodeFuncBoolean put_oo_b(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToBoolean<A, B> func) {
        return putFunction(name, new NodeFuncObjectObjectToBoolean<>(name, argTypeA, argTypeB, func));
    }

    public <R> INodeFuncObject<R> put_l_o(String name, Class<R> returnType, IFuncLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongToObject<>(name, returnType, func));
    }

    public <R> INodeFuncObject<R> put_ll_o(String name, Class<R> returnType, IFuncLongLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongLongToObject<>(name, returnType, func));
    }

    public <R> INodeFuncObject<R> put_lll_o(String name, Class<R> returnType, IFuncLongLongLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongLongLongToObject<>(name, returnType, func));
    }

    public <R> INodeFuncObject<R> put_llll_o(String name, Class<R> returnType, IFuncLongLongLongLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongLongLongLongToObject<>(name, returnType, func));
    }

    public <R> INodeFuncObject<R> put_d_o(String name, Class<R> returnType, IFuncDoubleToObject<R> func) {
        return putFunction(name, new NodeFuncDoubleToObject<>(name, returnType, func));
    }

    public <R> INodeFuncObject<R> put_b_o(String name, Class<R> returnType, IFuncBooleanToObject<R> func) {
        return putFunction(name, new NodeFuncBooleanToObject<>(name, returnType, func));
    }

    public <A, R> INodeFuncObject<R> put_o_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> INodeFuncObject<R> put_ol_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> INodeFuncObject<R> put_oll_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, B, R> INodeFuncObject<R> put_oo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<A, B, R> func) {
        return putFunction(name, new NodeFuncObjectObjectToObject<>(name, argTypeA, argTypeB, returnType, func));
    }

    public <A, B, C, R> INodeFuncObject<R> put_ooo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, C, R> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectToObject<>(name, argTypeA, argTypeB, argTypeC, returnType, func));
    }

    public <A, B, C, D, R> INodeFuncObject<R> put_oooo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToObject<>(name, argTypeA, argTypeB, argTypeC, argTypeD, returnType, func));
    }

}
