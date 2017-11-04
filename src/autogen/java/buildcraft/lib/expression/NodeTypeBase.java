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
import buildcraft.lib.expression.node.func.StringFunctionBi;
import buildcraft.lib.expression.node.func.StringFunctionTri;
import buildcraft.lib.expression.node.func.StringFunctionQuad;
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.func.StringFunctionHex;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong.IFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble.IFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble;
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
public abstract class NodeTypeBase<T> extends FunctionContext {

    public NodeTypeBase(String name) {
        super("Type: " + name);
    }

    protected abstract Class<T> getType();

    // put_o_l

    public  NodeFuncObjectToLong<T> put_t_l(String name, IFuncObjectToLong<T> func) {
        return put_o_l(name, getType(), func);
    }

    public  NodeFuncObjectToLong<T> put_t_l(String name, IFuncObjectToLong<T> func, StringFunctionBi stringFunction) {
        return put_o_l(name, getType(), func, stringFunction);
    }

    // put_ol_l

    public  NodeFuncObjectLongToLong<T> put_tl_l(String name, IFuncObjectLongToLong<T> func) {
        return put_ol_l(name, getType(), func);
    }

    public  NodeFuncObjectLongToLong<T> put_tl_l(String name, IFuncObjectLongToLong<T> func, StringFunctionTri stringFunction) {
        return put_ol_l(name, getType(), func, stringFunction);
    }

    // put_oll_l

    public  NodeFuncObjectLongLongToLong<T> put_tll_l(String name, IFuncObjectLongLongToLong<T> func) {
        return put_oll_l(name, getType(), func);
    }

    public  NodeFuncObjectLongLongToLong<T> put_tll_l(String name, IFuncObjectLongLongToLong<T> func, StringFunctionQuad stringFunction) {
        return put_oll_l(name, getType(), func, stringFunction);
    }

    // put_oo_l

    public <A> NodeFuncObjectObjectToLong<A, T> put_to_l(String name, Class<A> argTypeA, IFuncObjectObjectToLong<A, T> func) {
        return put_oo_l(name, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectObjectToLong<A, T> put_to_l(String name, Class<A> argTypeA, IFuncObjectObjectToLong<A, T> func, StringFunctionTri stringFunction) {
        return put_oo_l(name, argTypeA, getType(), func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToLong<T, B> put_ot_l(String name, Class<B> argTypeB, IFuncObjectObjectToLong<T, B> func) {
        return put_oo_l(name, getType(), argTypeB, func);
    }

    public <B> NodeFuncObjectObjectToLong<T, B> put_ot_l(String name, Class<B> argTypeB, IFuncObjectObjectToLong<T, B> func, StringFunctionTri stringFunction) {
        return put_oo_l(name, getType(), argTypeB, func, stringFunction);
    }

    public  NodeFuncObjectObjectToLong<T, T> put_tt_l(String name, IFuncObjectObjectToLong<T, T> func) {
        return put_oo_l(name, getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToLong<T, T> put_tt_l(String name, IFuncObjectObjectToLong<T, T> func, StringFunctionTri stringFunction) {
        return put_oo_l(name, getType(), getType(), func, stringFunction);
    }

    // put_o_d

    public  NodeFuncObjectToDouble<T> put_t_d(String name, IFuncObjectToDouble<T> func) {
        return put_o_d(name, getType(), func);
    }

    public  NodeFuncObjectToDouble<T> put_t_d(String name, IFuncObjectToDouble<T> func, StringFunctionBi stringFunction) {
        return put_o_d(name, getType(), func, stringFunction);
    }

    // put_oo_d

    public <A> NodeFuncObjectObjectToDouble<A, T> put_to_d(String name, Class<A> argTypeA, IFuncObjectObjectToDouble<A, T> func) {
        return put_oo_d(name, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectObjectToDouble<A, T> put_to_d(String name, Class<A> argTypeA, IFuncObjectObjectToDouble<A, T> func, StringFunctionTri stringFunction) {
        return put_oo_d(name, argTypeA, getType(), func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToDouble<T, B> put_ot_d(String name, Class<B> argTypeB, IFuncObjectObjectToDouble<T, B> func) {
        return put_oo_d(name, getType(), argTypeB, func);
    }

    public <B> NodeFuncObjectObjectToDouble<T, B> put_ot_d(String name, Class<B> argTypeB, IFuncObjectObjectToDouble<T, B> func, StringFunctionTri stringFunction) {
        return put_oo_d(name, getType(), argTypeB, func, stringFunction);
    }

    public  NodeFuncObjectObjectToDouble<T, T> put_tt_d(String name, IFuncObjectObjectToDouble<T, T> func) {
        return put_oo_d(name, getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToDouble<T, T> put_tt_d(String name, IFuncObjectObjectToDouble<T, T> func, StringFunctionTri stringFunction) {
        return put_oo_d(name, getType(), getType(), func, stringFunction);
    }

    // put_o_b

    public  NodeFuncObjectToBoolean<T> put_t_b(String name, IFuncObjectToBoolean<T> func) {
        return put_o_b(name, getType(), func);
    }

    public  NodeFuncObjectToBoolean<T> put_t_b(String name, IFuncObjectToBoolean<T> func, StringFunctionBi stringFunction) {
        return put_o_b(name, getType(), func, stringFunction);
    }

    // put_oo_b

    public <A> NodeFuncObjectObjectToBoolean<A, T> put_to_b(String name, Class<A> argTypeA, IFuncObjectObjectToBoolean<A, T> func) {
        return put_oo_b(name, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectObjectToBoolean<A, T> put_to_b(String name, Class<A> argTypeA, IFuncObjectObjectToBoolean<A, T> func, StringFunctionTri stringFunction) {
        return put_oo_b(name, argTypeA, getType(), func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToBoolean<T, B> put_ot_b(String name, Class<B> argTypeB, IFuncObjectObjectToBoolean<T, B> func) {
        return put_oo_b(name, getType(), argTypeB, func);
    }

    public <B> NodeFuncObjectObjectToBoolean<T, B> put_ot_b(String name, Class<B> argTypeB, IFuncObjectObjectToBoolean<T, B> func, StringFunctionTri stringFunction) {
        return put_oo_b(name, getType(), argTypeB, func, stringFunction);
    }

    public  NodeFuncObjectObjectToBoolean<T, T> put_tt_b(String name, IFuncObjectObjectToBoolean<T, T> func) {
        return put_oo_b(name, getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToBoolean<T, T> put_tt_b(String name, IFuncObjectObjectToBoolean<T, T> func, StringFunctionTri stringFunction) {
        return put_oo_b(name, getType(), getType(), func, stringFunction);
    }

    // put_l_o

    public  NodeFuncLongToObject<T> put_l_t(String name, IFuncLongToObject<T> func) {
        return put_l_o(name, getType(), func);
    }

    public  NodeFuncLongToObject<T> put_l_t(String name, IFuncLongToObject<T> func, StringFunctionBi stringFunction) {
        return put_l_o(name, getType(), func, stringFunction);
    }

    // put_ll_o

    public  NodeFuncLongLongToObject<T> put_ll_t(String name, IFuncLongLongToObject<T> func) {
        return put_ll_o(name, getType(), func);
    }

    public  NodeFuncLongLongToObject<T> put_ll_t(String name, IFuncLongLongToObject<T> func, StringFunctionTri stringFunction) {
        return put_ll_o(name, getType(), func, stringFunction);
    }

    // put_lll_o

    public  NodeFuncLongLongLongToObject<T> put_lll_t(String name, IFuncLongLongLongToObject<T> func) {
        return put_lll_o(name, getType(), func);
    }

    public  NodeFuncLongLongLongToObject<T> put_lll_t(String name, IFuncLongLongLongToObject<T> func, StringFunctionQuad stringFunction) {
        return put_lll_o(name, getType(), func, stringFunction);
    }

    // put_llll_o

    public  NodeFuncLongLongLongLongToObject<T> put_llll_t(String name, IFuncLongLongLongLongToObject<T> func) {
        return put_llll_o(name, getType(), func);
    }

    public  NodeFuncLongLongLongLongToObject<T> put_llll_t(String name, IFuncLongLongLongLongToObject<T> func, StringFunctionPenta stringFunction) {
        return put_llll_o(name, getType(), func, stringFunction);
    }

    // put_d_o

    public  NodeFuncDoubleToObject<T> put_d_t(String name, IFuncDoubleToObject<T> func) {
        return put_d_o(name, getType(), func);
    }

    public  NodeFuncDoubleToObject<T> put_d_t(String name, IFuncDoubleToObject<T> func, StringFunctionBi stringFunction) {
        return put_d_o(name, getType(), func, stringFunction);
    }

    // put_dd_o

    public  NodeFuncDoubleDoubleToObject<T> put_dd_t(String name, IFuncDoubleDoubleToObject<T> func) {
        return put_dd_o(name, getType(), func);
    }

    public  NodeFuncDoubleDoubleToObject<T> put_dd_t(String name, IFuncDoubleDoubleToObject<T> func, StringFunctionTri stringFunction) {
        return put_dd_o(name, getType(), func, stringFunction);
    }

    // put_ddd_o

    public  NodeFuncDoubleDoubleDoubleToObject<T> put_ddd_t(String name, IFuncDoubleDoubleDoubleToObject<T> func) {
        return put_ddd_o(name, getType(), func);
    }

    public  NodeFuncDoubleDoubleDoubleToObject<T> put_ddd_t(String name, IFuncDoubleDoubleDoubleToObject<T> func, StringFunctionQuad stringFunction) {
        return put_ddd_o(name, getType(), func, stringFunction);
    }

    // put_dddd_o

    public  NodeFuncDoubleDoubleDoubleDoubleToObject<T> put_dddd_t(String name, IFuncDoubleDoubleDoubleDoubleToObject<T> func) {
        return put_dddd_o(name, getType(), func);
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToObject<T> put_dddd_t(String name, IFuncDoubleDoubleDoubleDoubleToObject<T> func, StringFunctionPenta stringFunction) {
        return put_dddd_o(name, getType(), func, stringFunction);
    }

    // put_b_o

    public  NodeFuncBooleanToObject<T> put_b_t(String name, IFuncBooleanToObject<T> func) {
        return put_b_o(name, getType(), func);
    }

    public  NodeFuncBooleanToObject<T> put_b_t(String name, IFuncBooleanToObject<T> func, StringFunctionBi stringFunction) {
        return put_b_o(name, getType(), func, stringFunction);
    }

    // put_o_o

    public <A> NodeFuncObjectToObject<A, T> put_o_t(String name, Class<A> argTypeA, IFuncObjectToObject<A, T> func) {
        return put_o_o(name, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectToObject<A, T> put_o_t(String name, Class<A> argTypeA, IFuncObjectToObject<A, T> func, StringFunctionBi stringFunction) {
        return put_o_o(name, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectToObject<T, R> put_t_o(String name, Class<R> returnType, IFuncObjectToObject<T, R> func) {
        return put_o_o(name, getType(), returnType, func);
    }

    public <R> NodeFuncObjectToObject<T, R> put_t_o(String name, Class<R> returnType, IFuncObjectToObject<T, R> func, StringFunctionBi stringFunction) {
        return put_o_o(name, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectToObject<T, T> put_t_t(String name, IFuncObjectToObject<T, T> func) {
        return put_o_o(name, getType(), getType(), func);
    }

    public  NodeFuncObjectToObject<T, T> put_t_t(String name, IFuncObjectToObject<T, T> func, StringFunctionBi stringFunction) {
        return put_o_o(name, getType(), getType(), func, stringFunction);
    }

    // put_ol_o

    public <A> NodeFuncObjectLongToObject<A, T> put_ol_t(String name, Class<A> argTypeA, IFuncObjectLongToObject<A, T> func) {
        return put_ol_o(name, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectLongToObject<A, T> put_ol_t(String name, Class<A> argTypeA, IFuncObjectLongToObject<A, T> func, StringFunctionTri stringFunction) {
        return put_ol_o(name, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongToObject<T, R> put_tl_o(String name, Class<R> returnType, IFuncObjectLongToObject<T, R> func) {
        return put_ol_o(name, getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongToObject<T, R> put_tl_o(String name, Class<R> returnType, IFuncObjectLongToObject<T, R> func, StringFunctionTri stringFunction) {
        return put_ol_o(name, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongToObject<T, T> put_tl_t(String name, IFuncObjectLongToObject<T, T> func) {
        return put_ol_o(name, getType(), getType(), func);
    }

    public  NodeFuncObjectLongToObject<T, T> put_tl_t(String name, IFuncObjectLongToObject<T, T> func, StringFunctionTri stringFunction) {
        return put_ol_o(name, getType(), getType(), func, stringFunction);
    }

    // put_oll_o

    public <A> NodeFuncObjectLongLongToObject<A, T> put_oll_t(String name, Class<A> argTypeA, IFuncObjectLongLongToObject<A, T> func) {
        return put_oll_o(name, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectLongLongToObject<A, T> put_oll_t(String name, Class<A> argTypeA, IFuncObjectLongLongToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_oll_o(name, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongLongToObject<T, R> put_tll_o(String name, Class<R> returnType, IFuncObjectLongLongToObject<T, R> func) {
        return put_oll_o(name, getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongLongToObject<T, R> put_tll_o(String name, Class<R> returnType, IFuncObjectLongLongToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_oll_o(name, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongLongToObject<T, T> put_tll_t(String name, IFuncObjectLongLongToObject<T, T> func) {
        return put_oll_o(name, getType(), getType(), func);
    }

    public  NodeFuncObjectLongLongToObject<T, T> put_tll_t(String name, IFuncObjectLongLongToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_oll_o(name, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oo_o
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectToObject<A, B, T> put_oo_t(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToObject<A, B, T> func) {
        return put_oo_o(name, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectToObject<A, B, T> put_oo_t(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToObject<A, B, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectToObject<T, B, R> put_to_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<T, B, R> func) {
        return put_oo_o(name, getType(), argTypeB, returnType, func);
    }

    public <B, R> NodeFuncObjectObjectToObject<T, B, R> put_to_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<T, B, R> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, getType(), argTypeB, returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToObject<T, B, T> put_to_t(String name, Class<B> argTypeB, IFuncObjectObjectToObject<T, B, T> func) {
        return put_oo_o(name, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectToObject<T, B, T> put_to_t(String name, Class<B> argTypeB, IFuncObjectObjectToObject<T, B, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectToObject<A, T, R> put_ot_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectToObject<A, T, R> func) {
        return put_oo_o(name, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectToObject<A, T, R> put_ot_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectToObject<A, T, R> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectToObject<A, T, T> put_ot_t(String name, Class<A> argTypeA, IFuncObjectObjectToObject<A, T, T> func) {
        return put_oo_o(name, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectToObject<A, T, T> put_ot_t(String name, Class<A> argTypeA, IFuncObjectObjectToObject<A, T, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectToObject<T, T, R> put_tt_o(String name, Class<R> returnType, IFuncObjectObjectToObject<T, T, R> func) {
        return put_oo_o(name, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectToObject<T, T, R> put_tt_o(String name, Class<R> returnType, IFuncObjectObjectToObject<T, T, R> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectToObject<T, T, T> put_tt_t(String name, IFuncObjectObjectToObject<T, T, T> func) {
        return put_oo_o(name, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToObject<T, T, T> put_tt_t(String name, IFuncObjectObjectToObject<T, T, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(name, getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ooo_o
    //
    /////////////////////////

    public <A, B, C> NodeFuncObjectObjectObjectToObject<A, B, C, T> put_ooo_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, B, C, T> func) {
        return put_ooo_o(name, argTypeA, argTypeB, argTypeC, getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectToObject<A, B, C, T> put_ooo_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, B, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <B, C, R> NodeFuncObjectObjectObjectToObject<T, B, C, R> put_too_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, C, R> func) {
        return put_ooo_o(name, getType(), argTypeB, argTypeC, returnType, func);
    }

    public <B, C, R> NodeFuncObjectObjectObjectToObject<T, B, C, R> put_too_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, C, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), argTypeB, argTypeC, returnType, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectToObject<T, B, C, T> put_too_t(String name, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, B, C, T> func) {
        return put_ooo_o(name, getType(), argTypeB, argTypeC, getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectToObject<T, B, C, T> put_too_t(String name, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, B, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <A, C, R> NodeFuncObjectObjectObjectToObject<A, T, C, R> put_oto_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, C, R> func) {
        return put_ooo_o(name, argTypeA, getType(), argTypeC, returnType, func);
    }

    public <A, C, R> NodeFuncObjectObjectObjectToObject<A, T, C, R> put_oto_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, C, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, getType(), argTypeC, returnType, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectToObject<A, T, C, T> put_oto_t(String name, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, T, C, T> func) {
        return put_ooo_o(name, argTypeA, getType(), argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectToObject<A, T, C, T> put_oto_t(String name, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, T, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectObjectObjectToObject<T, T, C, R> put_tto_o(String name, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, C, R> func) {
        return put_ooo_o(name, getType(), getType(), argTypeC, returnType, func);
    }

    public <C, R> NodeFuncObjectObjectObjectToObject<T, T, C, R> put_tto_o(String name, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, C, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), getType(), argTypeC, returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectToObject<T, T, C, T> put_tto_t(String name, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, T, C, T> func) {
        return put_ooo_o(name, getType(), getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectToObject<T, T, C, T> put_tto_t(String name, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, T, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, B, R> NodeFuncObjectObjectObjectToObject<A, B, T, R> put_oot_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, T, R> func) {
        return put_ooo_o(name, argTypeA, argTypeB, getType(), returnType, func);
    }

    public <A, B, R> NodeFuncObjectObjectObjectToObject<A, B, T, R> put_oot_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, argTypeB, getType(), returnType, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectToObject<A, B, T, T> put_oot_t(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToObject<A, B, T, T> func) {
        return put_ooo_o(name, argTypeA, argTypeB, getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectToObject<A, B, T, T> put_oot_t(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToObject<A, B, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, argTypeB, getType(), getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectObjectToObject<T, B, T, R> put_tot_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, T, R> func) {
        return put_ooo_o(name, getType(), argTypeB, getType(), returnType, func);
    }

    public <B, R> NodeFuncObjectObjectObjectToObject<T, B, T, R> put_tot_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), argTypeB, getType(), returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectToObject<T, B, T, T> put_tot_t(String name, Class<B> argTypeB, IFuncObjectObjectObjectToObject<T, B, T, T> func) {
        return put_ooo_o(name, getType(), argTypeB, getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectToObject<T, B, T, T> put_tot_t(String name, Class<B> argTypeB, IFuncObjectObjectObjectToObject<T, B, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), argTypeB, getType(), getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectObjectToObject<A, T, T, R> put_ott_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, T, R> func) {
        return put_ooo_o(name, argTypeA, getType(), getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectObjectToObject<A, T, T, R> put_ott_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, getType(), getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectToObject<A, T, T, T> put_ott_t(String name, Class<A> argTypeA, IFuncObjectObjectObjectToObject<A, T, T, T> func) {
        return put_ooo_o(name, argTypeA, getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectToObject<A, T, T, T> put_ott_t(String name, Class<A> argTypeA, IFuncObjectObjectObjectToObject<A, T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, argTypeA, getType(), getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectObjectToObject<T, T, T, R> put_ttt_o(String name, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, T, R> func) {
        return put_ooo_o(name, getType(), getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectObjectToObject<T, T, T, R> put_ttt_o(String name, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectToObject<T, T, T, T> put_ttt_t(String name, IFuncObjectObjectObjectToObject<T, T, T, T> func) {
        return put_ooo_o(name, getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectToObject<T, T, T, T> put_ttt_t(String name, IFuncObjectObjectObjectToObject<T, T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(name, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oooo_o
    //
    /////////////////////////

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, T> put_oooo_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, C, D, T> func) {
        return put_oooo_o(name, argTypeA, argTypeB, argTypeC, argTypeD, getType(), func);
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, T> put_oooo_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, R> put_tooo_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, argTypeD, returnType, func);
    }

    public <B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, R> put_tooo_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, argTypeD, returnType, func, stringFunction);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, T> put_tooo_t(String name, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, argTypeD, getType(), func);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, T> put_tooo_t(String name, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <A, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, R> put_otoo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, D, R> func) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, argTypeD, returnType, func);
    }

    public <A, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, R> put_otoo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, argTypeD, returnType, func, stringFunction);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, T> put_otoo_t(String name, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, C, D, T> func) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, argTypeD, getType(), func);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, T> put_otoo_t(String name, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, R> put_ttoo_o(String name, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, D, R> func) {
        return put_oooo_o(name, getType(), getType(), argTypeC, argTypeD, returnType, func);
    }

    public <C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, R> put_ttoo_o(String name, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), argTypeC, argTypeD, returnType, func, stringFunction);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, T> put_ttoo_t(String name, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func) {
        return put_oooo_o(name, getType(), getType(), argTypeC, argTypeD, getType(), func);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, T> put_ttoo_t(String name, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <A, B, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, R> put_ooto_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, D, R> func) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), argTypeD, returnType, func);
    }

    public <A, B, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, R> put_ooto_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), argTypeD, returnType, func, stringFunction);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, T> put_ooto_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, T, D, T> func) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), argTypeD, getType(), func);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, T> put_ooto_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), argTypeD, getType(), func, stringFunction);
    }

    public <B, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, R> put_toto_o(String name, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, D, R> func) {
        return put_oooo_o(name, getType(), argTypeB, getType(), argTypeD, returnType, func);
    }

    public <B, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, R> put_toto_o(String name, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, getType(), argTypeD, returnType, func, stringFunction);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, T> put_toto_t(String name, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, T, D, T> func) {
        return put_oooo_o(name, getType(), argTypeB, getType(), argTypeD, getType(), func);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, T> put_toto_t(String name, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, getType(), argTypeD, getType(), func, stringFunction);
    }

    public <A, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, R> put_otto_o(String name, Class<A> argTypeA, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, D, R> func) {
        return put_oooo_o(name, argTypeA, getType(), getType(), argTypeD, returnType, func);
    }

    public <A, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, R> put_otto_o(String name, Class<A> argTypeA, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), getType(), argTypeD, returnType, func, stringFunction);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, T> put_otto_t(String name, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, T, D, T> func) {
        return put_oooo_o(name, argTypeA, getType(), getType(), argTypeD, getType(), func);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, T> put_otto_t(String name, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), getType(), argTypeD, getType(), func, stringFunction);
    }

    public <D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, R> put_ttto_o(String name, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, D, R> func) {
        return put_oooo_o(name, getType(), getType(), getType(), argTypeD, returnType, func);
    }

    public <D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, R> put_ttto_o(String name, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), getType(), argTypeD, returnType, func, stringFunction);
    }

    public <D> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, T> put_ttto_t(String name, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func) {
        return put_oooo_o(name, getType(), getType(), getType(), argTypeD, getType(), func);
    }

    public <D> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, T> put_ttto_t(String name, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), getType(), argTypeD, getType(), func, stringFunction);
    }

    public <A, B, C, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, R> put_ooot_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, T, R> func) {
        return put_oooo_o(name, argTypeA, argTypeB, argTypeC, getType(), returnType, func);
    }

    public <A, B, C, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, R> put_ooot_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, argTypeC, getType(), returnType, func, stringFunction);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, T> put_ooot_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, B, C, T, T> func) {
        return put_oooo_o(name, argTypeA, argTypeB, argTypeC, getType(), getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, T> put_ooot_t(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, B, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, argTypeC, getType(), getType(), func, stringFunction);
    }

    public <B, C, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, R> put_toot_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, T, R> func) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, getType(), returnType, func);
    }

    public <B, C, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, R> put_toot_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, getType(), returnType, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, T> put_toot_t(String name, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, B, C, T, T> func) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, getType(), getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, T> put_toot_t(String name, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, B, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, getType(), getType(), func, stringFunction);
    }

    public <A, C, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, R> put_otot_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, T, R> func) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, getType(), returnType, func);
    }

    public <A, C, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, R> put_otot_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, getType(), returnType, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, T> put_otot_t(String name, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, T, C, T, T> func) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, getType(), getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, T> put_otot_t(String name, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, T, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), argTypeC, getType(), getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, R> put_ttot_o(String name, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, T, R> func) {
        return put_oooo_o(name, getType(), getType(), argTypeC, getType(), returnType, func);
    }

    public <C, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, R> put_ttot_o(String name, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), argTypeC, getType(), returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, T> put_ttot_t(String name, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, T, C, T, T> func) {
        return put_oooo_o(name, getType(), getType(), argTypeC, getType(), getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, T> put_ttot_t(String name, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, T, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), argTypeC, getType(), getType(), func, stringFunction);
    }

    public <A, B, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, R> put_oott_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, T, R> func) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), getType(), returnType, func);
    }

    public <A, B, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, R> put_oott_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), getType(), returnType, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, T> put_oott_t(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<A, B, T, T, T> func) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, T> put_oott_t(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<A, B, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, argTypeB, getType(), getType(), getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, R> put_tott_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, T, R> func) {
        return put_oooo_o(name, getType(), argTypeB, getType(), getType(), returnType, func);
    }

    public <B, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, R> put_tott_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, getType(), getType(), returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, T> put_tott_t(String name, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<T, B, T, T, T> func) {
        return put_oooo_o(name, getType(), argTypeB, getType(), getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, T> put_tott_t(String name, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<T, B, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), argTypeB, getType(), getType(), getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, R> put_ottt_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, T, R> func) {
        return put_oooo_o(name, argTypeA, getType(), getType(), getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, R> put_ottt_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), getType(), getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, T> put_ottt_t(String name, Class<A> argTypeA, IFuncObjectObjectObjectObjectToObject<A, T, T, T, T> func) {
        return put_oooo_o(name, argTypeA, getType(), getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, T> put_ottt_t(String name, Class<A> argTypeA, IFuncObjectObjectObjectObjectToObject<A, T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, argTypeA, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, R> put_tttt_o(String name, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, T, R> func) {
        return put_oooo_o(name, getType(), getType(), getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, R> put_tttt_o(String name, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, T> put_tttt_t(String name, IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func) {
        return put_oooo_o(name, getType(), getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, T> put_tttt_t(String name, IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(name, getType(), getType(), getType(), getType(), getType(), func, stringFunction);
    }

}
