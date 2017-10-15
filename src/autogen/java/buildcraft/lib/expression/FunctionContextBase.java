/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.node.func.StringFunctionBi;
import buildcraft.lib.expression.node.func.StringFunctionTri;
import buildcraft.lib.expression.node.func.StringFunctionQuad;
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.func.StringFunctionHex;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong.IFuncLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong.IFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToLong.IFuncLongLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong.IFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToLong.IFuncBooleanToLong;
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
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble.IFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongToBoolean.IFuncLongToBoolean;
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
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject;
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

    public  NodeFuncLongToLong put_l_l(String name, IFuncLongToLong func) {
        return putFunction(name, new NodeFuncLongToLong(name, func));
    }

    public  NodeFuncLongToLong put_l_l(String name, IFuncLongToLong func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncLongToLong(func, stringFunction));
    }

    public  NodeFuncLongLongToLong put_ll_l(String name, IFuncLongLongToLong func) {
        return putFunction(name, new NodeFuncLongLongToLong(name, func));
    }

    public  NodeFuncLongLongToLong put_ll_l(String name, IFuncLongLongToLong func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncLongLongToLong(func, stringFunction));
    }

    public  NodeFuncLongLongLongToLong put_lll_l(String name, IFuncLongLongLongToLong func) {
        return putFunction(name, new NodeFuncLongLongLongToLong(name, func));
    }

    public  NodeFuncLongLongLongToLong put_lll_l(String name, IFuncLongLongLongToLong func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongToLong(func, stringFunction));
    }

    public  NodeFuncDoubleToLong put_d_l(String name, IFuncDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleToLong(name, func));
    }

    public  NodeFuncDoubleToLong put_d_l(String name, IFuncDoubleToLong func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncDoubleToLong(func, stringFunction));
    }

    public  NodeFuncBooleanToLong put_b_l(String name, IFuncBooleanToLong func) {
        return putFunction(name, new NodeFuncBooleanToLong(name, func));
    }

    public  NodeFuncBooleanToLong put_b_l(String name, IFuncBooleanToLong func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncBooleanToLong(func, stringFunction));
    }

    public <A> NodeFuncObjectToLong<A> put_o_l(String name, Class<A> argTypeA, IFuncObjectToLong<A> func) {
        return putFunction(name, new NodeFuncObjectToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectToLong<A> put_o_l(String name, Class<A> argTypeA, IFuncObjectToLong<A> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncObjectToLong<>(argTypeA, func, stringFunction));
    }

    public <A> NodeFuncObjectLongToLong<A> put_ol_l(String name, Class<A> argTypeA, IFuncObjectLongToLong<A> func) {
        return putFunction(name, new NodeFuncObjectLongToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongToLong<A> put_ol_l(String name, Class<A> argTypeA, IFuncObjectLongToLong<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectLongToLong<>(argTypeA, func, stringFunction));
    }

    public <A> NodeFuncObjectLongLongToLong<A> put_oll_l(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> func) {
        return putFunction(name, new NodeFuncObjectLongLongToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongLongToLong<A> put_oll_l(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongLongToLong<>(argTypeA, func, stringFunction));
    }

    public <A, B> NodeFuncObjectObjectToLong<A, B> put_oo_l(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToLong<A, B> func) {
        return putFunction(name, new NodeFuncObjectObjectToLong<>(name, argTypeA, argTypeB, func));
    }

    public <A, B> NodeFuncObjectObjectToLong<A, B> put_oo_l(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToLong<A, B> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectToLong<>(argTypeA, argTypeB, func, stringFunction));
    }

    public  NodeFuncLongToDouble put_l_d(String name, IFuncLongToDouble func) {
        return putFunction(name, new NodeFuncLongToDouble(name, func));
    }

    public  NodeFuncLongToDouble put_l_d(String name, IFuncLongToDouble func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncLongToDouble(func, stringFunction));
    }

    public  NodeFuncDoubleToDouble put_d_d(String name, IFuncDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleToDouble(name, func));
    }

    public  NodeFuncDoubleToDouble put_d_d(String name, IFuncDoubleToDouble func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncDoubleToDouble(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleToDouble put_dd_d(String name, IFuncDoubleDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleDoubleToDouble(name, func));
    }

    public  NodeFuncDoubleDoubleToDouble put_dd_d(String name, IFuncDoubleDoubleToDouble func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleToDouble(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleDoubleToDouble put_ddd_d(String name, IFuncDoubleDoubleDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToDouble(name, func));
    }

    public  NodeFuncDoubleDoubleDoubleToDouble put_ddd_d(String name, IFuncDoubleDoubleDoubleToDouble func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToDouble(func, stringFunction));
    }

    public <A> NodeFuncObjectToDouble<A> put_o_d(String name, Class<A> argTypeA, IFuncObjectToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectToDouble<A> put_o_d(String name, Class<A> argTypeA, IFuncObjectToDouble<A> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncObjectToDouble<>(argTypeA, func, stringFunction));
    }

    public <A, B> NodeFuncObjectObjectToDouble<A, B> put_oo_d(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToDouble<A, B> func) {
        return putFunction(name, new NodeFuncObjectObjectToDouble<>(name, argTypeA, argTypeB, func));
    }

    public <A, B> NodeFuncObjectObjectToDouble<A, B> put_oo_d(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToDouble<A, B> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectToDouble<>(argTypeA, argTypeB, func, stringFunction));
    }

    public  NodeFuncLongToBoolean put_l_b(String name, IFuncLongToBoolean func) {
        return putFunction(name, new NodeFuncLongToBoolean(name, func));
    }

    public  NodeFuncLongToBoolean put_l_b(String name, IFuncLongToBoolean func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncLongToBoolean(func, stringFunction));
    }

    public  NodeFuncLongLongToBoolean put_ll_b(String name, IFuncLongLongToBoolean func) {
        return putFunction(name, new NodeFuncLongLongToBoolean(name, func));
    }

    public  NodeFuncLongLongToBoolean put_ll_b(String name, IFuncLongLongToBoolean func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncLongLongToBoolean(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleToBoolean put_dd_b(String name, IFuncDoubleDoubleToBoolean func) {
        return putFunction(name, new NodeFuncDoubleDoubleToBoolean(name, func));
    }

    public  NodeFuncDoubleDoubleToBoolean put_dd_b(String name, IFuncDoubleDoubleToBoolean func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleToBoolean(func, stringFunction));
    }

    public  NodeFuncBooleanToBoolean put_b_b(String name, IFuncBooleanToBoolean func) {
        return putFunction(name, new NodeFuncBooleanToBoolean(name, func));
    }

    public  NodeFuncBooleanToBoolean put_b_b(String name, IFuncBooleanToBoolean func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncBooleanToBoolean(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanToBoolean put_bb_b(String name, IFuncBooleanBooleanToBoolean func) {
        return putFunction(name, new NodeFuncBooleanBooleanToBoolean(name, func));
    }

    public  NodeFuncBooleanBooleanToBoolean put_bb_b(String name, IFuncBooleanBooleanToBoolean func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanToBoolean(func, stringFunction));
    }

    public <A> NodeFuncObjectToBoolean<A> put_o_b(String name, Class<A> argTypeA, IFuncObjectToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectToBoolean<A> put_o_b(String name, Class<A> argTypeA, IFuncObjectToBoolean<A> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncObjectToBoolean<>(argTypeA, func, stringFunction));
    }

    public <A, B> NodeFuncObjectObjectToBoolean<A, B> put_oo_b(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToBoolean<A, B> func) {
        return putFunction(name, new NodeFuncObjectObjectToBoolean<>(name, argTypeA, argTypeB, func));
    }

    public <A, B> NodeFuncObjectObjectToBoolean<A, B> put_oo_b(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToBoolean<A, B> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectToBoolean<>(argTypeA, argTypeB, func, stringFunction));
    }

    public <R> NodeFuncLongToObject<R> put_l_o(String name, Class<R> returnType, IFuncLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongToObject<>(name, returnType, func));
    }

    public <R> NodeFuncLongToObject<R> put_l_o(String name, Class<R> returnType, IFuncLongToObject<R> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncLongToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncLongLongToObject<R> put_ll_o(String name, Class<R> returnType, IFuncLongLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongLongToObject<>(name, returnType, func));
    }

    public <R> NodeFuncLongLongToObject<R> put_ll_o(String name, Class<R> returnType, IFuncLongLongToObject<R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncLongLongToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncLongLongLongToObject<R> put_lll_o(String name, Class<R> returnType, IFuncLongLongLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongLongLongToObject<>(name, returnType, func));
    }

    public <R> NodeFuncLongLongLongToObject<R> put_lll_o(String name, Class<R> returnType, IFuncLongLongLongToObject<R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncLongLongLongLongToObject<R> put_llll_o(String name, Class<R> returnType, IFuncLongLongLongLongToObject<R> func) {
        return putFunction(name, new NodeFuncLongLongLongLongToObject<>(name, returnType, func));
    }

    public <R> NodeFuncLongLongLongLongToObject<R> put_llll_o(String name, Class<R> returnType, IFuncLongLongLongLongToObject<R> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongLongToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncDoubleToObject<R> put_d_o(String name, Class<R> returnType, IFuncDoubleToObject<R> func) {
        return putFunction(name, new NodeFuncDoubleToObject<>(name, returnType, func));
    }

    public <R> NodeFuncDoubleToObject<R> put_d_o(String name, Class<R> returnType, IFuncDoubleToObject<R> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncDoubleToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncDoubleDoubleToObject<R> put_dd_o(String name, Class<R> returnType, IFuncDoubleDoubleToObject<R> func) {
        return putFunction(name, new NodeFuncDoubleDoubleToObject<>(name, returnType, func));
    }

    public <R> NodeFuncDoubleDoubleToObject<R> put_dd_o(String name, Class<R> returnType, IFuncDoubleDoubleToObject<R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncDoubleDoubleDoubleToObject<R> put_ddd_o(String name, Class<R> returnType, IFuncDoubleDoubleDoubleToObject<R> func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToObject<>(name, returnType, func));
    }

    public <R> NodeFuncDoubleDoubleDoubleToObject<R> put_ddd_o(String name, Class<R> returnType, IFuncDoubleDoubleDoubleToObject<R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncDoubleDoubleDoubleDoubleToObject<R> put_dddd_o(String name, Class<R> returnType, IFuncDoubleDoubleDoubleDoubleToObject<R> func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToObject<>(name, returnType, func));
    }

    public <R> NodeFuncDoubleDoubleDoubleDoubleToObject<R> put_dddd_o(String name, Class<R> returnType, IFuncDoubleDoubleDoubleDoubleToObject<R> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncBooleanToObject<R> put_b_o(String name, Class<R> returnType, IFuncBooleanToObject<R> func) {
        return putFunction(name, new NodeFuncBooleanToObject<>(name, returnType, func));
    }

    public <R> NodeFuncBooleanToObject<R> put_b_o(String name, Class<R> returnType, IFuncBooleanToObject<R> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncBooleanToObject<>(returnType, func, stringFunction));
    }

    public <A, R> NodeFuncObjectToObject<A, R> put_o_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectToObject<A, R> put_o_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectToObject<A, R> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncObjectToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A, R> NodeFuncObjectLongToObject<A, R> put_ol_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectLongToObject<A, R> put_ol_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectLongToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A, R> NodeFuncObjectLongLongToObject<A, R> put_oll_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectLongLongToObject<A, R> put_oll_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongLongToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongLongToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A, B, R> NodeFuncObjectObjectToObject<A, B, R> put_oo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<A, B, R> func) {
        return putFunction(name, new NodeFuncObjectObjectToObject<>(name, argTypeA, argTypeB, returnType, func));
    }

    public <A, B, R> NodeFuncObjectObjectToObject<A, B, R> put_oo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<A, B, R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectToObject<>(argTypeA, argTypeB, returnType, func, stringFunction));
    }

    public <A, B, C, R> NodeFuncObjectObjectObjectToObject<A, B, C, R> put_ooo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, C, R> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectToObject<>(name, argTypeA, argTypeB, argTypeC, returnType, func));
    }

    public <A, B, C, R> NodeFuncObjectObjectObjectToObject<A, B, C, R> put_ooo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, C, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectToObject<>(argTypeA, argTypeB, argTypeC, returnType, func, stringFunction));
    }

    public <A, B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, R> put_oooo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToObject<>(name, argTypeA, argTypeB, argTypeC, argTypeD, returnType, func));
    }

    public <A, B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, R> put_oooo_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToObject<>(argTypeA, argTypeB, argTypeC, argTypeD, returnType, func, stringFunction));
    }

}
