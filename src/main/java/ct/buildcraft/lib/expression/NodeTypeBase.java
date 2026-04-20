/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import ct.buildcraft.lib.expression.node.func.StringFunctionBi;
import ct.buildcraft.lib.expression.node.func.StringFunctionPenta;
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.StringFunctionTri;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanToObject.IFuncBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToObject.IFuncDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongToObject.IFuncLongLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToObject.IFuncLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToObject.IFuncLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToDouble.IFuncObjectBooleanBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToBoolean.IFuncObjectBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToBoolean.IFuncObjectLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToDouble.IFuncObjectLongLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToDouble.IFuncObjectLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToObject.IFuncObjectLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToLong.IFuncObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToObject.IFuncObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToBoolean.IFuncObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToDouble.IFuncObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToLong.IFuncObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToObject.IFuncObjectToObject;


// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public abstract class NodeTypeBase<T> extends FunctionContext {

    public NodeTypeBase(String name) {
        super("Type: " + name);
    }

    protected abstract Class<T> getType();

    // put_ol_l

    public  NodeFuncObjectLongToLong<T> put_tl_l(String fname, IFuncObjectLongToLong<T> func) {
        return put_ol_l(fname, getType(), func);
    }

    public  NodeFuncObjectLongToLong<T> put_tl_l(String fname, IFuncObjectLongToLong<T> func, StringFunctionTri stringFunction) {
        return put_ol_l(fname, getType(), func, stringFunction);
    }

    // put_oll_o

    public <A> NodeFuncObjectLongLongToObject<A, T> put_oll_t(String fname, Class<A> argTypeA, IFuncObjectLongLongToObject<A, T> func) {
        return put_oll_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectLongLongToObject<A, T> put_oll_t(String fname, Class<A> argTypeA, IFuncObjectLongLongToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_oll_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongLongToObject<T, R> put_tll_o(String fname, Class<R> returnType, IFuncObjectLongLongToObject<T, R> func) {
        return put_oll_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongLongToObject<T, R> put_tll_o(String fname, Class<R> returnType, IFuncObjectLongLongToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_oll_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongLongToObject<T, T> put_tll_t(String fname, IFuncObjectLongLongToObject<T, T> func) {
        return put_oll_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectLongLongToObject<T, T> put_tll_t(String fname, IFuncObjectLongLongToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_oll_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_oll_l

    public  NodeFuncObjectLongLongToLong<T> put_tll_l(String fname, IFuncObjectLongLongToLong<T> func) {
        return put_oll_l(fname, getType(), func);
    }

    public  NodeFuncObjectLongLongToLong<T> put_tll_l(String fname, IFuncObjectLongLongToLong<T> func, StringFunctionQuad stringFunction) {
        return put_oll_l(fname, getType(), func, stringFunction);
    }

    // put_od_l

    public  NodeFuncObjectDoubleToLong<T> put_td_l(String fname, IFuncObjectDoubleToLong<T> func) {
        return put_od_l(fname, getType(), func);
    }

    public  NodeFuncObjectDoubleToLong<T> put_td_l(String fname, IFuncObjectDoubleToLong<T> func, StringFunctionTri stringFunction) {
        return put_od_l(fname, getType(), func, stringFunction);
    }

    // put_old_o

    public <A> NodeFuncObjectLongDoubleToObject<A, T> put_old_t(String fname, Class<A> argTypeA, IFuncObjectLongDoubleToObject<A, T> func) {
        return put_old_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectLongDoubleToObject<A, T> put_old_t(String fname, Class<A> argTypeA, IFuncObjectLongDoubleToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_old_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongDoubleToObject<T, R> put_tld_o(String fname, Class<R> returnType, IFuncObjectLongDoubleToObject<T, R> func) {
        return put_old_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongDoubleToObject<T, R> put_tld_o(String fname, Class<R> returnType, IFuncObjectLongDoubleToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_old_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongDoubleToObject<T, T> put_tld_t(String fname, IFuncObjectLongDoubleToObject<T, T> func) {
        return put_old_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectLongDoubleToObject<T, T> put_tld_t(String fname, IFuncObjectLongDoubleToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_old_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_odd_l

    public  NodeFuncObjectDoubleDoubleToLong<T> put_tdd_l(String fname, IFuncObjectDoubleDoubleToLong<T> func) {
        return put_odd_l(fname, getType(), func);
    }

    public  NodeFuncObjectDoubleDoubleToLong<T> put_tdd_l(String fname, IFuncObjectDoubleDoubleToLong<T> func, StringFunctionQuad stringFunction) {
        return put_odd_l(fname, getType(), func, stringFunction);
    }

    // put_ob_l

    public  NodeFuncObjectBooleanToLong<T> put_tb_l(String fname, IFuncObjectBooleanToLong<T> func) {
        return put_ob_l(fname, getType(), func);
    }

    public  NodeFuncObjectBooleanToLong<T> put_tb_l(String fname, IFuncObjectBooleanToLong<T> func, StringFunctionTri stringFunction) {
        return put_ob_l(fname, getType(), func, stringFunction);
    }

    // put_olb_o

    public <A> NodeFuncObjectLongBooleanToObject<A, T> put_olb_t(String fname, Class<A> argTypeA, IFuncObjectLongBooleanToObject<A, T> func) {
        return put_olb_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectLongBooleanToObject<A, T> put_olb_t(String fname, Class<A> argTypeA, IFuncObjectLongBooleanToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_olb_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongBooleanToObject<T, R> put_tlb_o(String fname, Class<R> returnType, IFuncObjectLongBooleanToObject<T, R> func) {
        return put_olb_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongBooleanToObject<T, R> put_tlb_o(String fname, Class<R> returnType, IFuncObjectLongBooleanToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_olb_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongBooleanToObject<T, T> put_tlb_t(String fname, IFuncObjectLongBooleanToObject<T, T> func) {
        return put_olb_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectLongBooleanToObject<T, T> put_tlb_t(String fname, IFuncObjectLongBooleanToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_olb_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_obb_l

    public  NodeFuncObjectBooleanBooleanToLong<T> put_tbb_l(String fname, IFuncObjectBooleanBooleanToLong<T> func) {
        return put_obb_l(fname, getType(), func);
    }

    public  NodeFuncObjectBooleanBooleanToLong<T> put_tbb_l(String fname, IFuncObjectBooleanBooleanToLong<T> func, StringFunctionQuad stringFunction) {
        return put_obb_l(fname, getType(), func, stringFunction);
    }

    // put_o_l

    public  NodeFuncObjectToLong<T> put_t_l(String fname, IFuncObjectToLong<T> func) {
        return put_o_l(fname, getType(), func);
    }

    public  NodeFuncObjectToLong<T> put_t_l(String fname, IFuncObjectToLong<T> func, StringFunctionBi stringFunction) {
        return put_o_l(fname, getType(), func, stringFunction);
    }

    // put_oo_l

    public <A> NodeFuncObjectObjectToLong<A, T> put_to_l(String fname, Class<A> argTypeA, IFuncObjectObjectToLong<A, T> func) {
        return put_oo_l(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectObjectToLong<A, T> put_to_l(String fname, Class<A> argTypeA, IFuncObjectObjectToLong<A, T> func, StringFunctionTri stringFunction) {
        return put_oo_l(fname, argTypeA, getType(), func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToLong<T, B> put_ot_l(String fname, Class<B> argTypeB, IFuncObjectObjectToLong<T, B> func) {
        return put_oo_l(fname, getType(), argTypeB, func);
    }

    public <B> NodeFuncObjectObjectToLong<T, B> put_ot_l(String fname, Class<B> argTypeB, IFuncObjectObjectToLong<T, B> func, StringFunctionTri stringFunction) {
        return put_oo_l(fname, getType(), argTypeB, func, stringFunction);
    }

    public  NodeFuncObjectObjectToLong<T, T> put_tt_l(String fname, IFuncObjectObjectToLong<T, T> func) {
        return put_oo_l(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToLong<T, T> put_tt_l(String fname, IFuncObjectObjectToLong<T, T> func, StringFunctionTri stringFunction) {
        return put_oo_l(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ooo_l
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectObjectToLong<A, B, T> put_too_l(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToLong<A, B, T> func) {
        return put_ooo_l(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectToLong<A, B, T> put_too_l(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToLong<A, B, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectToLong<T, B, C> put_oto_l(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToLong<T, B, C> func) {
        return put_ooo_l(fname, getType(), argTypeB, argTypeC, func);
    }

    public <B, C> NodeFuncObjectObjectObjectToLong<T, B, C> put_oto_l(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToLong<T, B, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, getType(), argTypeB, argTypeC, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectToLong<T, B, T> put_tto_l(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToLong<T, B, T> func) {
        return put_ooo_l(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectToLong<T, B, T> put_tto_l(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToLong<T, B, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectToLong<A, T, C> put_oot_l(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToLong<A, T, C> func) {
        return put_ooo_l(fname, argTypeA, getType(), argTypeC, func);
    }

    public <A, C> NodeFuncObjectObjectObjectToLong<A, T, C> put_oot_l(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToLong<A, T, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, argTypeA, getType(), argTypeC, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectToLong<A, T, T> put_tot_l(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToLong<A, T, T> func) {
        return put_ooo_l(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectToLong<A, T, T> put_tot_l(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToLong<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectToLong<T, T, C> put_ott_l(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToLong<T, T, C> func) {
        return put_ooo_l(fname, getType(), getType(), argTypeC, func);
    }

    public <C> NodeFuncObjectObjectObjectToLong<T, T, C> put_ott_l(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToLong<T, T, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, getType(), getType(), argTypeC, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectToLong<T, T, T> put_ttt_l(String fname, IFuncObjectObjectObjectToLong<T, T, T> func) {
        return put_ooo_l(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectToLong<T, T, T> put_ttt_l(String fname, IFuncObjectObjectObjectToLong<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_l(fname, getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oooo_l
    //
    /////////////////////////

    public <A, B, C> NodeFuncObjectObjectObjectObjectToLong<A, B, C, T> put_tooo_l(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<A, B, C, T> func) {
        return put_oooo_l(fname, argTypeA, argTypeB, argTypeC, getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToLong<A, B, C, T> put_tooo_l(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<A, B, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToLong<T, B, C, D> put_otoo_l(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, B, C, D> func) {
        return put_oooo_l(fname, getType(), argTypeB, argTypeC, argTypeD, func);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToLong<T, B, C, D> put_otoo_l(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, B, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), argTypeB, argTypeC, argTypeD, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToLong<T, B, C, T> put_ttoo_l(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<T, B, C, T> func) {
        return put_oooo_l(fname, getType(), argTypeB, argTypeC, getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToLong<T, B, C, T> put_ttoo_l(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<T, B, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToLong<A, T, C, D> put_ooto_l(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, T, C, D> func) {
        return put_oooo_l(fname, argTypeA, getType(), argTypeC, argTypeD, func);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToLong<A, T, C, D> put_ooto_l(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, T, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, getType(), argTypeC, argTypeD, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToLong<A, T, C, T> put_toto_l(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<A, T, C, T> func) {
        return put_oooo_l(fname, argTypeA, getType(), argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToLong<A, T, C, T> put_toto_l(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<A, T, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToLong<T, T, C, D> put_otto_l(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, T, C, D> func) {
        return put_oooo_l(fname, getType(), getType(), argTypeC, argTypeD, func);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToLong<T, T, C, D> put_otto_l(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, T, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), getType(), argTypeC, argTypeD, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectObjectToLong<T, T, C, T> put_ttto_l(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<T, T, C, T> func) {
        return put_oooo_l(fname, getType(), getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectObjectToLong<T, T, C, T> put_ttto_l(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToLong<T, T, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToLong<A, B, T, D> put_ooot_l(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, B, T, D> func) {
        return put_oooo_l(fname, argTypeA, argTypeB, getType(), argTypeD, func);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToLong<A, B, T, D> put_ooot_l(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, B, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, argTypeB, getType(), argTypeD, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToLong<A, B, T, T> put_toot_l(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToLong<A, B, T, T> func) {
        return put_oooo_l(fname, argTypeA, argTypeB, getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToLong<A, B, T, T> put_toot_l(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToLong<A, B, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, argTypeB, getType(), getType(), func, stringFunction);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToLong<T, B, T, D> put_otot_l(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, B, T, D> func) {
        return put_oooo_l(fname, getType(), argTypeB, getType(), argTypeD, func);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToLong<T, B, T, D> put_otot_l(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, B, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), argTypeB, getType(), argTypeD, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectObjectToLong<T, B, T, T> put_ttot_l(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToLong<T, B, T, T> func) {
        return put_oooo_l(fname, getType(), argTypeB, getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectObjectToLong<T, B, T, T> put_ttot_l(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToLong<T, B, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), argTypeB, getType(), getType(), func, stringFunction);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToLong<A, T, T, D> put_oott_l(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, T, T, D> func) {
        return put_oooo_l(fname, argTypeA, getType(), getType(), argTypeD, func);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToLong<A, T, T, D> put_oott_l(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, T, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, getType(), getType(), argTypeD, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectObjectToLong<A, T, T, T> put_tott_l(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToLong<A, T, T, T> func) {
        return put_oooo_l(fname, argTypeA, getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectObjectToLong<A, T, T, T> put_tott_l(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToLong<A, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, argTypeA, getType(), getType(), getType(), func, stringFunction);
    }

    public <D> NodeFuncObjectObjectObjectObjectToLong<T, T, T, D> put_ottt_l(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, T, T, D> func) {
        return put_oooo_l(fname, getType(), getType(), getType(), argTypeD, func);
    }

    public <D> NodeFuncObjectObjectObjectObjectToLong<T, T, T, D> put_ottt_l(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<T, T, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), getType(), getType(), argTypeD, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectObjectToLong<T, T, T, T> put_tttt_l(String fname, IFuncObjectObjectObjectObjectToLong<T, T, T, T> func) {
        return put_oooo_l(fname, getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectObjectToLong<T, T, T, T> put_tttt_l(String fname, IFuncObjectObjectObjectObjectToLong<T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_l(fname, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_olo_o
    //
    /////////////////////////

    public <A, C> NodeFuncObjectLongObjectToObject<A, C, T> put_olo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectLongObjectToObject<A, C, T> func) {
        return put_olo_o(fname, argTypeA, argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectLongObjectToObject<A, C, T> put_olo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectLongObjectToObject<A, C, T> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, argTypeA, argTypeC, getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectLongObjectToObject<T, C, R> put_tlo_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectLongObjectToObject<T, C, R> func) {
        return put_olo_o(fname, getType(), argTypeC, returnType, func);
    }

    public <C, R> NodeFuncObjectLongObjectToObject<T, C, R> put_tlo_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectLongObjectToObject<T, C, R> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, getType(), argTypeC, returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectLongObjectToObject<T, C, T> put_tlo_t(String fname, Class<C> argTypeC, IFuncObjectLongObjectToObject<T, C, T> func) {
        return put_olo_o(fname, getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectLongObjectToObject<T, C, T> put_tlo_t(String fname, Class<C> argTypeC, IFuncObjectLongObjectToObject<T, C, T> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectLongObjectToObject<A, T, R> put_olt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongObjectToObject<A, T, R> func) {
        return put_olo_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectLongObjectToObject<A, T, R> put_olt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongObjectToObject<A, T, R> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectLongObjectToObject<A, T, T> put_olt_t(String fname, Class<A> argTypeA, IFuncObjectLongObjectToObject<A, T, T> func) {
        return put_olo_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectLongObjectToObject<A, T, T> put_olt_t(String fname, Class<A> argTypeA, IFuncObjectLongObjectToObject<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongObjectToObject<T, T, R> put_tlt_o(String fname, Class<R> returnType, IFuncObjectLongObjectToObject<T, T, R> func) {
        return put_olo_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongObjectToObject<T, T, R> put_tlt_o(String fname, Class<R> returnType, IFuncObjectLongObjectToObject<T, T, R> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongObjectToObject<T, T, T> put_tlt_t(String fname, IFuncObjectLongObjectToObject<T, T, T> func) {
        return put_olo_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectLongObjectToObject<T, T, T> put_tlt_t(String fname, IFuncObjectLongObjectToObject<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_olo_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    // put_ol_d

    public  NodeFuncObjectLongToDouble<T> put_tl_d(String fname, IFuncObjectLongToDouble<T> func) {
        return put_ol_d(fname, getType(), func);
    }

    public  NodeFuncObjectLongToDouble<T> put_tl_d(String fname, IFuncObjectLongToDouble<T> func, StringFunctionTri stringFunction) {
        return put_ol_d(fname, getType(), func, stringFunction);
    }

    // put_odl_o

    public <A> NodeFuncObjectDoubleLongToObject<A, T> put_odl_t(String fname, Class<A> argTypeA, IFuncObjectDoubleLongToObject<A, T> func) {
        return put_odl_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectDoubleLongToObject<A, T> put_odl_t(String fname, Class<A> argTypeA, IFuncObjectDoubleLongToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_odl_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectDoubleLongToObject<T, R> put_tdl_o(String fname, Class<R> returnType, IFuncObjectDoubleLongToObject<T, R> func) {
        return put_odl_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectDoubleLongToObject<T, R> put_tdl_o(String fname, Class<R> returnType, IFuncObjectDoubleLongToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_odl_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectDoubleLongToObject<T, T> put_tdl_t(String fname, IFuncObjectDoubleLongToObject<T, T> func) {
        return put_odl_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectDoubleLongToObject<T, T> put_tdl_t(String fname, IFuncObjectDoubleLongToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_odl_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_oll_d

    public  NodeFuncObjectLongLongToDouble<T> put_tll_d(String fname, IFuncObjectLongLongToDouble<T> func) {
        return put_oll_d(fname, getType(), func);
    }

    public  NodeFuncObjectLongLongToDouble<T> put_tll_d(String fname, IFuncObjectLongLongToDouble<T> func, StringFunctionQuad stringFunction) {
        return put_oll_d(fname, getType(), func, stringFunction);
    }

    // put_od_d

    public  NodeFuncObjectDoubleToDouble<T> put_td_d(String fname, IFuncObjectDoubleToDouble<T> func) {
        return put_od_d(fname, getType(), func);
    }

    public  NodeFuncObjectDoubleToDouble<T> put_td_d(String fname, IFuncObjectDoubleToDouble<T> func, StringFunctionTri stringFunction) {
        return put_od_d(fname, getType(), func, stringFunction);
    }

    // put_odd_o

    public <A> NodeFuncObjectDoubleDoubleToObject<A, T> put_odd_t(String fname, Class<A> argTypeA, IFuncObjectDoubleDoubleToObject<A, T> func) {
        return put_odd_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectDoubleDoubleToObject<A, T> put_odd_t(String fname, Class<A> argTypeA, IFuncObjectDoubleDoubleToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_odd_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectDoubleDoubleToObject<T, R> put_tdd_o(String fname, Class<R> returnType, IFuncObjectDoubleDoubleToObject<T, R> func) {
        return put_odd_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectDoubleDoubleToObject<T, R> put_tdd_o(String fname, Class<R> returnType, IFuncObjectDoubleDoubleToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_odd_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectDoubleDoubleToObject<T, T> put_tdd_t(String fname, IFuncObjectDoubleDoubleToObject<T, T> func) {
        return put_odd_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectDoubleDoubleToObject<T, T> put_tdd_t(String fname, IFuncObjectDoubleDoubleToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_odd_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_odd_d

    public  NodeFuncObjectDoubleDoubleToDouble<T> put_tdd_d(String fname, IFuncObjectDoubleDoubleToDouble<T> func) {
        return put_odd_d(fname, getType(), func);
    }

    public  NodeFuncObjectDoubleDoubleToDouble<T> put_tdd_d(String fname, IFuncObjectDoubleDoubleToDouble<T> func, StringFunctionQuad stringFunction) {
        return put_odd_d(fname, getType(), func, stringFunction);
    }

    // put_ob_d

    public  NodeFuncObjectBooleanToDouble<T> put_tb_d(String fname, IFuncObjectBooleanToDouble<T> func) {
        return put_ob_d(fname, getType(), func);
    }

    public  NodeFuncObjectBooleanToDouble<T> put_tb_d(String fname, IFuncObjectBooleanToDouble<T> func, StringFunctionTri stringFunction) {
        return put_ob_d(fname, getType(), func, stringFunction);
    }

    // put_odb_o

    public <A> NodeFuncObjectDoubleBooleanToObject<A, T> put_odb_t(String fname, Class<A> argTypeA, IFuncObjectDoubleBooleanToObject<A, T> func) {
        return put_odb_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectDoubleBooleanToObject<A, T> put_odb_t(String fname, Class<A> argTypeA, IFuncObjectDoubleBooleanToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_odb_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectDoubleBooleanToObject<T, R> put_tdb_o(String fname, Class<R> returnType, IFuncObjectDoubleBooleanToObject<T, R> func) {
        return put_odb_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectDoubleBooleanToObject<T, R> put_tdb_o(String fname, Class<R> returnType, IFuncObjectDoubleBooleanToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_odb_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectDoubleBooleanToObject<T, T> put_tdb_t(String fname, IFuncObjectDoubleBooleanToObject<T, T> func) {
        return put_odb_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectDoubleBooleanToObject<T, T> put_tdb_t(String fname, IFuncObjectDoubleBooleanToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_odb_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_obb_d

    public  NodeFuncObjectBooleanBooleanToDouble<T> put_tbb_d(String fname, IFuncObjectBooleanBooleanToDouble<T> func) {
        return put_obb_d(fname, getType(), func);
    }

    public  NodeFuncObjectBooleanBooleanToDouble<T> put_tbb_d(String fname, IFuncObjectBooleanBooleanToDouble<T> func, StringFunctionQuad stringFunction) {
        return put_obb_d(fname, getType(), func, stringFunction);
    }

    // put_o_d

    public  NodeFuncObjectToDouble<T> put_t_d(String fname, IFuncObjectToDouble<T> func) {
        return put_o_d(fname, getType(), func);
    }

    public  NodeFuncObjectToDouble<T> put_t_d(String fname, IFuncObjectToDouble<T> func, StringFunctionBi stringFunction) {
        return put_o_d(fname, getType(), func, stringFunction);
    }

    // put_oo_d

    public <A> NodeFuncObjectObjectToDouble<A, T> put_to_d(String fname, Class<A> argTypeA, IFuncObjectObjectToDouble<A, T> func) {
        return put_oo_d(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectObjectToDouble<A, T> put_to_d(String fname, Class<A> argTypeA, IFuncObjectObjectToDouble<A, T> func, StringFunctionTri stringFunction) {
        return put_oo_d(fname, argTypeA, getType(), func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToDouble<T, B> put_ot_d(String fname, Class<B> argTypeB, IFuncObjectObjectToDouble<T, B> func) {
        return put_oo_d(fname, getType(), argTypeB, func);
    }

    public <B> NodeFuncObjectObjectToDouble<T, B> put_ot_d(String fname, Class<B> argTypeB, IFuncObjectObjectToDouble<T, B> func, StringFunctionTri stringFunction) {
        return put_oo_d(fname, getType(), argTypeB, func, stringFunction);
    }

    public  NodeFuncObjectObjectToDouble<T, T> put_tt_d(String fname, IFuncObjectObjectToDouble<T, T> func) {
        return put_oo_d(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToDouble<T, T> put_tt_d(String fname, IFuncObjectObjectToDouble<T, T> func, StringFunctionTri stringFunction) {
        return put_oo_d(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ooo_d
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectObjectToDouble<A, B, T> put_too_d(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToDouble<A, B, T> func) {
        return put_ooo_d(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectToDouble<A, B, T> put_too_d(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToDouble<A, B, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectToDouble<T, B, C> put_oto_d(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<T, B, C> func) {
        return put_ooo_d(fname, getType(), argTypeB, argTypeC, func);
    }

    public <B, C> NodeFuncObjectObjectObjectToDouble<T, B, C> put_oto_d(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<T, B, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, getType(), argTypeB, argTypeC, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectToDouble<T, B, T> put_tto_d(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToDouble<T, B, T> func) {
        return put_ooo_d(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectToDouble<T, B, T> put_tto_d(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToDouble<T, B, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectToDouble<A, T, C> put_oot_d(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<A, T, C> func) {
        return put_ooo_d(fname, argTypeA, getType(), argTypeC, func);
    }

    public <A, C> NodeFuncObjectObjectObjectToDouble<A, T, C> put_oot_d(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<A, T, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, argTypeA, getType(), argTypeC, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectToDouble<A, T, T> put_tot_d(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToDouble<A, T, T> func) {
        return put_ooo_d(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectToDouble<A, T, T> put_tot_d(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToDouble<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectToDouble<T, T, C> put_ott_d(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<T, T, C> func) {
        return put_ooo_d(fname, getType(), getType(), argTypeC, func);
    }

    public <C> NodeFuncObjectObjectObjectToDouble<T, T, C> put_ott_d(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<T, T, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, getType(), getType(), argTypeC, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectToDouble<T, T, T> put_ttt_d(String fname, IFuncObjectObjectObjectToDouble<T, T, T> func) {
        return put_ooo_d(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectToDouble<T, T, T> put_ttt_d(String fname, IFuncObjectObjectObjectToDouble<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_d(fname, getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oooo_d
    //
    /////////////////////////

    public <A, B, C> NodeFuncObjectObjectObjectObjectToDouble<A, B, C, T> put_tooo_d(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<A, B, C, T> func) {
        return put_oooo_d(fname, argTypeA, argTypeB, argTypeC, getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToDouble<A, B, C, T> put_tooo_d(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<A, B, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, D> put_otoo_d(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, B, C, D> func) {
        return put_oooo_d(fname, getType(), argTypeB, argTypeC, argTypeD, func);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, D> put_otoo_d(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, B, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), argTypeB, argTypeC, argTypeD, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, T> put_ttoo_d(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<T, B, C, T> func) {
        return put_oooo_d(fname, getType(), argTypeB, argTypeC, getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, T> put_ttoo_d(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<T, B, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, D> put_ooto_d(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, T, C, D> func) {
        return put_oooo_d(fname, argTypeA, getType(), argTypeC, argTypeD, func);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, D> put_ooto_d(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, T, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, getType(), argTypeC, argTypeD, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, T> put_toto_d(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<A, T, C, T> func) {
        return put_oooo_d(fname, argTypeA, getType(), argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, T> put_toto_d(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<A, T, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, D> put_otto_d(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, T, C, D> func) {
        return put_oooo_d(fname, getType(), getType(), argTypeC, argTypeD, func);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, D> put_otto_d(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, T, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), getType(), argTypeC, argTypeD, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, T> put_ttto_d(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<T, T, C, T> func) {
        return put_oooo_d(fname, getType(), getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, T> put_ttto_d(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToDouble<T, T, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, D> put_ooot_d(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, B, T, D> func) {
        return put_oooo_d(fname, argTypeA, argTypeB, getType(), argTypeD, func);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, D> put_ooot_d(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, B, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, argTypeB, getType(), argTypeD, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, T> put_toot_d(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToDouble<A, B, T, T> func) {
        return put_oooo_d(fname, argTypeA, argTypeB, getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, T> put_toot_d(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToDouble<A, B, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, argTypeB, getType(), getType(), func, stringFunction);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, D> put_otot_d(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, B, T, D> func) {
        return put_oooo_d(fname, getType(), argTypeB, getType(), argTypeD, func);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, D> put_otot_d(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, B, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), argTypeB, getType(), argTypeD, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, T> put_ttot_d(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToDouble<T, B, T, T> func) {
        return put_oooo_d(fname, getType(), argTypeB, getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, T> put_ttot_d(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToDouble<T, B, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), argTypeB, getType(), getType(), func, stringFunction);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, D> put_oott_d(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, T, T, D> func) {
        return put_oooo_d(fname, argTypeA, getType(), getType(), argTypeD, func);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, D> put_oott_d(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, T, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, getType(), getType(), argTypeD, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, T> put_tott_d(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToDouble<A, T, T, T> func) {
        return put_oooo_d(fname, argTypeA, getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, T> put_tott_d(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToDouble<A, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, argTypeA, getType(), getType(), getType(), func, stringFunction);
    }

    public <D> NodeFuncObjectObjectObjectObjectToDouble<T, T, T, D> put_ottt_d(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, T, T, D> func) {
        return put_oooo_d(fname, getType(), getType(), getType(), argTypeD, func);
    }

    public <D> NodeFuncObjectObjectObjectObjectToDouble<T, T, T, D> put_ottt_d(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<T, T, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), getType(), getType(), argTypeD, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectObjectToDouble<T, T, T, T> put_tttt_d(String fname, IFuncObjectObjectObjectObjectToDouble<T, T, T, T> func) {
        return put_oooo_d(fname, getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectObjectToDouble<T, T, T, T> put_tttt_d(String fname, IFuncObjectObjectObjectObjectToDouble<T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_d(fname, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_odo_o
    //
    /////////////////////////

    public <A, C> NodeFuncObjectDoubleObjectToObject<A, C, T> put_odo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectDoubleObjectToObject<A, C, T> func) {
        return put_odo_o(fname, argTypeA, argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectDoubleObjectToObject<A, C, T> put_odo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectDoubleObjectToObject<A, C, T> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, argTypeA, argTypeC, getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectDoubleObjectToObject<T, C, R> put_tdo_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectDoubleObjectToObject<T, C, R> func) {
        return put_odo_o(fname, getType(), argTypeC, returnType, func);
    }

    public <C, R> NodeFuncObjectDoubleObjectToObject<T, C, R> put_tdo_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectDoubleObjectToObject<T, C, R> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, getType(), argTypeC, returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectDoubleObjectToObject<T, C, T> put_tdo_t(String fname, Class<C> argTypeC, IFuncObjectDoubleObjectToObject<T, C, T> func) {
        return put_odo_o(fname, getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectDoubleObjectToObject<T, C, T> put_tdo_t(String fname, Class<C> argTypeC, IFuncObjectDoubleObjectToObject<T, C, T> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectDoubleObjectToObject<A, T, R> put_odt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleObjectToObject<A, T, R> func) {
        return put_odo_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectDoubleObjectToObject<A, T, R> put_odt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleObjectToObject<A, T, R> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectDoubleObjectToObject<A, T, T> put_odt_t(String fname, Class<A> argTypeA, IFuncObjectDoubleObjectToObject<A, T, T> func) {
        return put_odo_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectDoubleObjectToObject<A, T, T> put_odt_t(String fname, Class<A> argTypeA, IFuncObjectDoubleObjectToObject<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectDoubleObjectToObject<T, T, R> put_tdt_o(String fname, Class<R> returnType, IFuncObjectDoubleObjectToObject<T, T, R> func) {
        return put_odo_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectDoubleObjectToObject<T, T, R> put_tdt_o(String fname, Class<R> returnType, IFuncObjectDoubleObjectToObject<T, T, R> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectDoubleObjectToObject<T, T, T> put_tdt_t(String fname, IFuncObjectDoubleObjectToObject<T, T, T> func) {
        return put_odo_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectDoubleObjectToObject<T, T, T> put_tdt_t(String fname, IFuncObjectDoubleObjectToObject<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_odo_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    // put_ol_b

    public  NodeFuncObjectLongToBoolean<T> put_tl_b(String fname, IFuncObjectLongToBoolean<T> func) {
        return put_ol_b(fname, getType(), func);
    }

    public  NodeFuncObjectLongToBoolean<T> put_tl_b(String fname, IFuncObjectLongToBoolean<T> func, StringFunctionTri stringFunction) {
        return put_ol_b(fname, getType(), func, stringFunction);
    }

    // put_obl_o

    public <A> NodeFuncObjectBooleanLongToObject<A, T> put_obl_t(String fname, Class<A> argTypeA, IFuncObjectBooleanLongToObject<A, T> func) {
        return put_obl_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectBooleanLongToObject<A, T> put_obl_t(String fname, Class<A> argTypeA, IFuncObjectBooleanLongToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_obl_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectBooleanLongToObject<T, R> put_tbl_o(String fname, Class<R> returnType, IFuncObjectBooleanLongToObject<T, R> func) {
        return put_obl_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectBooleanLongToObject<T, R> put_tbl_o(String fname, Class<R> returnType, IFuncObjectBooleanLongToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_obl_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectBooleanLongToObject<T, T> put_tbl_t(String fname, IFuncObjectBooleanLongToObject<T, T> func) {
        return put_obl_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectBooleanLongToObject<T, T> put_tbl_t(String fname, IFuncObjectBooleanLongToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_obl_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_oll_b

    public  NodeFuncObjectLongLongToBoolean<T> put_tll_b(String fname, IFuncObjectLongLongToBoolean<T> func) {
        return put_oll_b(fname, getType(), func);
    }

    public  NodeFuncObjectLongLongToBoolean<T> put_tll_b(String fname, IFuncObjectLongLongToBoolean<T> func, StringFunctionQuad stringFunction) {
        return put_oll_b(fname, getType(), func, stringFunction);
    }

    // put_od_b

    public  NodeFuncObjectDoubleToBoolean<T> put_td_b(String fname, IFuncObjectDoubleToBoolean<T> func) {
        return put_od_b(fname, getType(), func);
    }

    public  NodeFuncObjectDoubleToBoolean<T> put_td_b(String fname, IFuncObjectDoubleToBoolean<T> func, StringFunctionTri stringFunction) {
        return put_od_b(fname, getType(), func, stringFunction);
    }

    // put_obd_o

    public <A> NodeFuncObjectBooleanDoubleToObject<A, T> put_obd_t(String fname, Class<A> argTypeA, IFuncObjectBooleanDoubleToObject<A, T> func) {
        return put_obd_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectBooleanDoubleToObject<A, T> put_obd_t(String fname, Class<A> argTypeA, IFuncObjectBooleanDoubleToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_obd_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectBooleanDoubleToObject<T, R> put_tbd_o(String fname, Class<R> returnType, IFuncObjectBooleanDoubleToObject<T, R> func) {
        return put_obd_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectBooleanDoubleToObject<T, R> put_tbd_o(String fname, Class<R> returnType, IFuncObjectBooleanDoubleToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_obd_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectBooleanDoubleToObject<T, T> put_tbd_t(String fname, IFuncObjectBooleanDoubleToObject<T, T> func) {
        return put_obd_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectBooleanDoubleToObject<T, T> put_tbd_t(String fname, IFuncObjectBooleanDoubleToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_obd_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_odd_b

    public  NodeFuncObjectDoubleDoubleToBoolean<T> put_tdd_b(String fname, IFuncObjectDoubleDoubleToBoolean<T> func) {
        return put_odd_b(fname, getType(), func);
    }

    public  NodeFuncObjectDoubleDoubleToBoolean<T> put_tdd_b(String fname, IFuncObjectDoubleDoubleToBoolean<T> func, StringFunctionQuad stringFunction) {
        return put_odd_b(fname, getType(), func, stringFunction);
    }

    // put_ob_b

    public  NodeFuncObjectBooleanToBoolean<T> put_tb_b(String fname, IFuncObjectBooleanToBoolean<T> func) {
        return put_ob_b(fname, getType(), func);
    }

    public  NodeFuncObjectBooleanToBoolean<T> put_tb_b(String fname, IFuncObjectBooleanToBoolean<T> func, StringFunctionTri stringFunction) {
        return put_ob_b(fname, getType(), func, stringFunction);
    }

    // put_obb_o

    public <A> NodeFuncObjectBooleanBooleanToObject<A, T> put_obb_t(String fname, Class<A> argTypeA, IFuncObjectBooleanBooleanToObject<A, T> func) {
        return put_obb_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectBooleanBooleanToObject<A, T> put_obb_t(String fname, Class<A> argTypeA, IFuncObjectBooleanBooleanToObject<A, T> func, StringFunctionQuad stringFunction) {
        return put_obb_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectBooleanBooleanToObject<T, R> put_tbb_o(String fname, Class<R> returnType, IFuncObjectBooleanBooleanToObject<T, R> func) {
        return put_obb_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectBooleanBooleanToObject<T, R> put_tbb_o(String fname, Class<R> returnType, IFuncObjectBooleanBooleanToObject<T, R> func, StringFunctionQuad stringFunction) {
        return put_obb_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectBooleanBooleanToObject<T, T> put_tbb_t(String fname, IFuncObjectBooleanBooleanToObject<T, T> func) {
        return put_obb_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectBooleanBooleanToObject<T, T> put_tbb_t(String fname, IFuncObjectBooleanBooleanToObject<T, T> func, StringFunctionQuad stringFunction) {
        return put_obb_o(fname, getType(), getType(), func, stringFunction);
    }

    // put_obb_b

    public  NodeFuncObjectBooleanBooleanToBoolean<T> put_tbb_b(String fname, IFuncObjectBooleanBooleanToBoolean<T> func) {
        return put_obb_b(fname, getType(), func);
    }

    public  NodeFuncObjectBooleanBooleanToBoolean<T> put_tbb_b(String fname, IFuncObjectBooleanBooleanToBoolean<T> func, StringFunctionQuad stringFunction) {
        return put_obb_b(fname, getType(), func, stringFunction);
    }

    // put_o_b

    public  NodeFuncObjectToBoolean<T> put_t_b(String fname, IFuncObjectToBoolean<T> func) {
        return put_o_b(fname, getType(), func);
    }

    public  NodeFuncObjectToBoolean<T> put_t_b(String fname, IFuncObjectToBoolean<T> func, StringFunctionBi stringFunction) {
        return put_o_b(fname, getType(), func, stringFunction);
    }

    // put_oo_b

    public <A> NodeFuncObjectObjectToBoolean<A, T> put_to_b(String fname, Class<A> argTypeA, IFuncObjectObjectToBoolean<A, T> func) {
        return put_oo_b(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectObjectToBoolean<A, T> put_to_b(String fname, Class<A> argTypeA, IFuncObjectObjectToBoolean<A, T> func, StringFunctionTri stringFunction) {
        return put_oo_b(fname, argTypeA, getType(), func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToBoolean<T, B> put_ot_b(String fname, Class<B> argTypeB, IFuncObjectObjectToBoolean<T, B> func) {
        return put_oo_b(fname, getType(), argTypeB, func);
    }

    public <B> NodeFuncObjectObjectToBoolean<T, B> put_ot_b(String fname, Class<B> argTypeB, IFuncObjectObjectToBoolean<T, B> func, StringFunctionTri stringFunction) {
        return put_oo_b(fname, getType(), argTypeB, func, stringFunction);
    }

    public  NodeFuncObjectObjectToBoolean<T, T> put_tt_b(String fname, IFuncObjectObjectToBoolean<T, T> func) {
        return put_oo_b(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToBoolean<T, T> put_tt_b(String fname, IFuncObjectObjectToBoolean<T, T> func, StringFunctionTri stringFunction) {
        return put_oo_b(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ooo_b
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectObjectToBoolean<A, B, T> put_too_b(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToBoolean<A, B, T> func) {
        return put_ooo_b(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectToBoolean<A, B, T> put_too_b(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToBoolean<A, B, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectToBoolean<T, B, C> put_oto_b(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<T, B, C> func) {
        return put_ooo_b(fname, getType(), argTypeB, argTypeC, func);
    }

    public <B, C> NodeFuncObjectObjectObjectToBoolean<T, B, C> put_oto_b(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<T, B, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, getType(), argTypeB, argTypeC, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectToBoolean<T, B, T> put_tto_b(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToBoolean<T, B, T> func) {
        return put_ooo_b(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectToBoolean<T, B, T> put_tto_b(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToBoolean<T, B, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectToBoolean<A, T, C> put_oot_b(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<A, T, C> func) {
        return put_ooo_b(fname, argTypeA, getType(), argTypeC, func);
    }

    public <A, C> NodeFuncObjectObjectObjectToBoolean<A, T, C> put_oot_b(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<A, T, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, argTypeA, getType(), argTypeC, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectToBoolean<A, T, T> put_tot_b(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToBoolean<A, T, T> func) {
        return put_ooo_b(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectToBoolean<A, T, T> put_tot_b(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToBoolean<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectToBoolean<T, T, C> put_ott_b(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<T, T, C> func) {
        return put_ooo_b(fname, getType(), getType(), argTypeC, func);
    }

    public <C> NodeFuncObjectObjectObjectToBoolean<T, T, C> put_ott_b(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<T, T, C> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, getType(), getType(), argTypeC, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectToBoolean<T, T, T> put_ttt_b(String fname, IFuncObjectObjectObjectToBoolean<T, T, T> func) {
        return put_ooo_b(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectToBoolean<T, T, T> put_ttt_b(String fname, IFuncObjectObjectObjectToBoolean<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_b(fname, getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oooo_b
    //
    /////////////////////////

    public <A, B, C> NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, T> put_tooo_b(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<A, B, C, T> func) {
        return put_oooo_b(fname, argTypeA, argTypeB, argTypeC, getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, T> put_tooo_b(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<A, B, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, D> put_otoo_b(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, B, C, D> func) {
        return put_oooo_b(fname, getType(), argTypeB, argTypeC, argTypeD, func);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, D> put_otoo_b(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, B, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), argTypeB, argTypeC, argTypeD, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, T> put_ttoo_b(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<T, B, C, T> func) {
        return put_oooo_b(fname, getType(), argTypeB, argTypeC, getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, T> put_ttoo_b(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<T, B, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, D> put_ooto_b(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, T, C, D> func) {
        return put_oooo_b(fname, argTypeA, getType(), argTypeC, argTypeD, func);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, D> put_ooto_b(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, T, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, getType(), argTypeC, argTypeD, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, T> put_toto_b(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<A, T, C, T> func) {
        return put_oooo_b(fname, argTypeA, getType(), argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, T> put_toto_b(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<A, T, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, D> put_otto_b(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, T, C, D> func) {
        return put_oooo_b(fname, getType(), getType(), argTypeC, argTypeD, func);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, D> put_otto_b(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, T, C, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), getType(), argTypeC, argTypeD, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, T> put_ttto_b(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<T, T, C, T> func) {
        return put_oooo_b(fname, getType(), getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, T> put_ttto_b(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToBoolean<T, T, C, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, D> put_ooot_b(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, B, T, D> func) {
        return put_oooo_b(fname, argTypeA, argTypeB, getType(), argTypeD, func);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, D> put_ooot_b(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, B, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, argTypeB, getType(), argTypeD, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, T> put_toot_b(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToBoolean<A, B, T, T> func) {
        return put_oooo_b(fname, argTypeA, argTypeB, getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, T> put_toot_b(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToBoolean<A, B, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, argTypeB, getType(), getType(), func, stringFunction);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, D> put_otot_b(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, B, T, D> func) {
        return put_oooo_b(fname, getType(), argTypeB, getType(), argTypeD, func);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, D> put_otot_b(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, B, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), argTypeB, getType(), argTypeD, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, T> put_ttot_b(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToBoolean<T, B, T, T> func) {
        return put_oooo_b(fname, getType(), argTypeB, getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, T> put_ttot_b(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToBoolean<T, B, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), argTypeB, getType(), getType(), func, stringFunction);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, D> put_oott_b(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, T, T, D> func) {
        return put_oooo_b(fname, argTypeA, getType(), getType(), argTypeD, func);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, D> put_oott_b(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, T, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, getType(), getType(), argTypeD, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, T> put_tott_b(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToBoolean<A, T, T, T> func) {
        return put_oooo_b(fname, argTypeA, getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, T> put_tott_b(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToBoolean<A, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, argTypeA, getType(), getType(), getType(), func, stringFunction);
    }

    public <D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, D> put_ottt_b(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, T, T, D> func) {
        return put_oooo_b(fname, getType(), getType(), getType(), argTypeD, func);
    }

    public <D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, D> put_ottt_b(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<T, T, T, D> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), getType(), getType(), argTypeD, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, T> put_tttt_b(String fname, IFuncObjectObjectObjectObjectToBoolean<T, T, T, T> func) {
        return put_oooo_b(fname, getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, T> put_tttt_b(String fname, IFuncObjectObjectObjectObjectToBoolean<T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_b(fname, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_obo_o
    //
    /////////////////////////

    public <A, C> NodeFuncObjectBooleanObjectToObject<A, C, T> put_obo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectBooleanObjectToObject<A, C, T> func) {
        return put_obo_o(fname, argTypeA, argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectBooleanObjectToObject<A, C, T> put_obo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectBooleanObjectToObject<A, C, T> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, argTypeA, argTypeC, getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectBooleanObjectToObject<T, C, R> put_tbo_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectBooleanObjectToObject<T, C, R> func) {
        return put_obo_o(fname, getType(), argTypeC, returnType, func);
    }

    public <C, R> NodeFuncObjectBooleanObjectToObject<T, C, R> put_tbo_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectBooleanObjectToObject<T, C, R> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, getType(), argTypeC, returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectBooleanObjectToObject<T, C, T> put_tbo_t(String fname, Class<C> argTypeC, IFuncObjectBooleanObjectToObject<T, C, T> func) {
        return put_obo_o(fname, getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectBooleanObjectToObject<T, C, T> put_tbo_t(String fname, Class<C> argTypeC, IFuncObjectBooleanObjectToObject<T, C, T> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectBooleanObjectToObject<A, T, R> put_obt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanObjectToObject<A, T, R> func) {
        return put_obo_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectBooleanObjectToObject<A, T, R> put_obt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanObjectToObject<A, T, R> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectBooleanObjectToObject<A, T, T> put_obt_t(String fname, Class<A> argTypeA, IFuncObjectBooleanObjectToObject<A, T, T> func) {
        return put_obo_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectBooleanObjectToObject<A, T, T> put_obt_t(String fname, Class<A> argTypeA, IFuncObjectBooleanObjectToObject<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectBooleanObjectToObject<T, T, R> put_tbt_o(String fname, Class<R> returnType, IFuncObjectBooleanObjectToObject<T, T, R> func) {
        return put_obo_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectBooleanObjectToObject<T, T, R> put_tbt_o(String fname, Class<R> returnType, IFuncObjectBooleanObjectToObject<T, T, R> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectBooleanObjectToObject<T, T, T> put_tbt_t(String fname, IFuncObjectBooleanObjectToObject<T, T, T> func) {
        return put_obo_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectBooleanObjectToObject<T, T, T> put_tbt_t(String fname, IFuncObjectBooleanObjectToObject<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_obo_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    // put_l_o

    public  NodeFuncLongToObject<T> put_l_t(String fname, IFuncLongToObject<T> func) {
        return put_l_o(fname, getType(), func);
    }

    public  NodeFuncLongToObject<T> put_l_t(String fname, IFuncLongToObject<T> func, StringFunctionBi stringFunction) {
        return put_l_o(fname, getType(), func, stringFunction);
    }

    // put_ll_o

    public  NodeFuncLongLongToObject<T> put_ll_t(String fname, IFuncLongLongToObject<T> func) {
        return put_ll_o(fname, getType(), func);
    }

    public  NodeFuncLongLongToObject<T> put_ll_t(String fname, IFuncLongLongToObject<T> func, StringFunctionTri stringFunction) {
        return put_ll_o(fname, getType(), func, stringFunction);
    }

    // put_lll_o

    public  NodeFuncLongLongLongToObject<T> put_lll_t(String fname, IFuncLongLongLongToObject<T> func) {
        return put_lll_o(fname, getType(), func);
    }

    public  NodeFuncLongLongLongToObject<T> put_lll_t(String fname, IFuncLongLongLongToObject<T> func, StringFunctionQuad stringFunction) {
        return put_lll_o(fname, getType(), func, stringFunction);
    }

    // put_llll_o

    public  NodeFuncLongLongLongLongToObject<T> put_llll_t(String fname, IFuncLongLongLongLongToObject<T> func) {
        return put_llll_o(fname, getType(), func);
    }

    public  NodeFuncLongLongLongLongToObject<T> put_llll_t(String fname, IFuncLongLongLongLongToObject<T> func, StringFunctionPenta stringFunction) {
        return put_llll_o(fname, getType(), func, stringFunction);
    }

    // put_ol_o

    public <A> NodeFuncObjectLongToObject<A, T> put_ol_t(String fname, Class<A> argTypeA, IFuncObjectLongToObject<A, T> func) {
        return put_ol_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectLongToObject<A, T> put_ol_t(String fname, Class<A> argTypeA, IFuncObjectLongToObject<A, T> func, StringFunctionTri stringFunction) {
        return put_ol_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectLongToObject<T, R> put_tl_o(String fname, Class<R> returnType, IFuncObjectLongToObject<T, R> func) {
        return put_ol_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectLongToObject<T, R> put_tl_o(String fname, Class<R> returnType, IFuncObjectLongToObject<T, R> func, StringFunctionTri stringFunction) {
        return put_ol_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectLongToObject<T, T> put_tl_t(String fname, IFuncObjectLongToObject<T, T> func) {
        return put_ol_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectLongToObject<T, T> put_tl_t(String fname, IFuncObjectLongToObject<T, T> func, StringFunctionTri stringFunction) {
        return put_ol_o(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ool_o
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectLongToObject<A, B, T> put_ool_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectLongToObject<A, B, T> func) {
        return put_ool_o(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectLongToObject<A, B, T> put_ool_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectLongToObject<A, B, T> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectLongToObject<T, B, R> put_tol_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectLongToObject<T, B, R> func) {
        return put_ool_o(fname, getType(), argTypeB, returnType, func);
    }

    public <B, R> NodeFuncObjectObjectLongToObject<T, B, R> put_tol_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectLongToObject<T, B, R> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, getType(), argTypeB, returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectLongToObject<T, B, T> put_tol_t(String fname, Class<B> argTypeB, IFuncObjectObjectLongToObject<T, B, T> func) {
        return put_ool_o(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectLongToObject<T, B, T> put_tol_t(String fname, Class<B> argTypeB, IFuncObjectObjectLongToObject<T, B, T> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectLongToObject<A, T, R> put_otl_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectLongToObject<A, T, R> func) {
        return put_ool_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectLongToObject<A, T, R> put_otl_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectLongToObject<A, T, R> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectLongToObject<A, T, T> put_otl_t(String fname, Class<A> argTypeA, IFuncObjectObjectLongToObject<A, T, T> func) {
        return put_ool_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectLongToObject<A, T, T> put_otl_t(String fname, Class<A> argTypeA, IFuncObjectObjectLongToObject<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectLongToObject<T, T, R> put_ttl_o(String fname, Class<R> returnType, IFuncObjectObjectLongToObject<T, T, R> func) {
        return put_ool_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectLongToObject<T, T, R> put_ttl_o(String fname, Class<R> returnType, IFuncObjectObjectLongToObject<T, T, R> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectLongToObject<T, T, T> put_ttl_t(String fname, IFuncObjectObjectLongToObject<T, T, T> func) {
        return put_ool_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectLongToObject<T, T, T> put_ttl_t(String fname, IFuncObjectObjectLongToObject<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ool_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    // put_d_o

    public  NodeFuncDoubleToObject<T> put_d_t(String fname, IFuncDoubleToObject<T> func) {
        return put_d_o(fname, getType(), func);
    }

    public  NodeFuncDoubleToObject<T> put_d_t(String fname, IFuncDoubleToObject<T> func, StringFunctionBi stringFunction) {
        return put_d_o(fname, getType(), func, stringFunction);
    }

    // put_dd_o

    public  NodeFuncDoubleDoubleToObject<T> put_dd_t(String fname, IFuncDoubleDoubleToObject<T> func) {
        return put_dd_o(fname, getType(), func);
    }

    public  NodeFuncDoubleDoubleToObject<T> put_dd_t(String fname, IFuncDoubleDoubleToObject<T> func, StringFunctionTri stringFunction) {
        return put_dd_o(fname, getType(), func, stringFunction);
    }

    // put_ddd_o

    public  NodeFuncDoubleDoubleDoubleToObject<T> put_ddd_t(String fname, IFuncDoubleDoubleDoubleToObject<T> func) {
        return put_ddd_o(fname, getType(), func);
    }

    public  NodeFuncDoubleDoubleDoubleToObject<T> put_ddd_t(String fname, IFuncDoubleDoubleDoubleToObject<T> func, StringFunctionQuad stringFunction) {
        return put_ddd_o(fname, getType(), func, stringFunction);
    }

    // put_dddd_o

    public  NodeFuncDoubleDoubleDoubleDoubleToObject<T> put_dddd_t(String fname, IFuncDoubleDoubleDoubleDoubleToObject<T> func) {
        return put_dddd_o(fname, getType(), func);
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToObject<T> put_dddd_t(String fname, IFuncDoubleDoubleDoubleDoubleToObject<T> func, StringFunctionPenta stringFunction) {
        return put_dddd_o(fname, getType(), func, stringFunction);
    }

    // put_od_o

    public <A> NodeFuncObjectDoubleToObject<A, T> put_od_t(String fname, Class<A> argTypeA, IFuncObjectDoubleToObject<A, T> func) {
        return put_od_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectDoubleToObject<A, T> put_od_t(String fname, Class<A> argTypeA, IFuncObjectDoubleToObject<A, T> func, StringFunctionTri stringFunction) {
        return put_od_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectDoubleToObject<T, R> put_td_o(String fname, Class<R> returnType, IFuncObjectDoubleToObject<T, R> func) {
        return put_od_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectDoubleToObject<T, R> put_td_o(String fname, Class<R> returnType, IFuncObjectDoubleToObject<T, R> func, StringFunctionTri stringFunction) {
        return put_od_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectDoubleToObject<T, T> put_td_t(String fname, IFuncObjectDoubleToObject<T, T> func) {
        return put_od_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectDoubleToObject<T, T> put_td_t(String fname, IFuncObjectDoubleToObject<T, T> func, StringFunctionTri stringFunction) {
        return put_od_o(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ood_o
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectDoubleToObject<A, B, T> put_ood_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectDoubleToObject<A, B, T> func) {
        return put_ood_o(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectDoubleToObject<A, B, T> put_ood_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectDoubleToObject<A, B, T> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectDoubleToObject<T, B, R> put_tod_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectDoubleToObject<T, B, R> func) {
        return put_ood_o(fname, getType(), argTypeB, returnType, func);
    }

    public <B, R> NodeFuncObjectObjectDoubleToObject<T, B, R> put_tod_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectDoubleToObject<T, B, R> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, getType(), argTypeB, returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectDoubleToObject<T, B, T> put_tod_t(String fname, Class<B> argTypeB, IFuncObjectObjectDoubleToObject<T, B, T> func) {
        return put_ood_o(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectDoubleToObject<T, B, T> put_tod_t(String fname, Class<B> argTypeB, IFuncObjectObjectDoubleToObject<T, B, T> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectDoubleToObject<A, T, R> put_otd_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectDoubleToObject<A, T, R> func) {
        return put_ood_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectDoubleToObject<A, T, R> put_otd_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectDoubleToObject<A, T, R> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectDoubleToObject<A, T, T> put_otd_t(String fname, Class<A> argTypeA, IFuncObjectObjectDoubleToObject<A, T, T> func) {
        return put_ood_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectDoubleToObject<A, T, T> put_otd_t(String fname, Class<A> argTypeA, IFuncObjectObjectDoubleToObject<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectDoubleToObject<T, T, R> put_ttd_o(String fname, Class<R> returnType, IFuncObjectObjectDoubleToObject<T, T, R> func) {
        return put_ood_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectDoubleToObject<T, T, R> put_ttd_o(String fname, Class<R> returnType, IFuncObjectObjectDoubleToObject<T, T, R> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectDoubleToObject<T, T, T> put_ttd_t(String fname, IFuncObjectObjectDoubleToObject<T, T, T> func) {
        return put_ood_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectDoubleToObject<T, T, T> put_ttd_t(String fname, IFuncObjectObjectDoubleToObject<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ood_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    // put_b_o

    public  NodeFuncBooleanToObject<T> put_b_t(String fname, IFuncBooleanToObject<T> func) {
        return put_b_o(fname, getType(), func);
    }

    public  NodeFuncBooleanToObject<T> put_b_t(String fname, IFuncBooleanToObject<T> func, StringFunctionBi stringFunction) {
        return put_b_o(fname, getType(), func, stringFunction);
    }

    // put_bb_o

    public  NodeFuncBooleanBooleanToObject<T> put_bb_t(String fname, IFuncBooleanBooleanToObject<T> func) {
        return put_bb_o(fname, getType(), func);
    }

    public  NodeFuncBooleanBooleanToObject<T> put_bb_t(String fname, IFuncBooleanBooleanToObject<T> func, StringFunctionTri stringFunction) {
        return put_bb_o(fname, getType(), func, stringFunction);
    }

    // put_bbb_o

    public  NodeFuncBooleanBooleanBooleanToObject<T> put_bbb_t(String fname, IFuncBooleanBooleanBooleanToObject<T> func) {
        return put_bbb_o(fname, getType(), func);
    }

    public  NodeFuncBooleanBooleanBooleanToObject<T> put_bbb_t(String fname, IFuncBooleanBooleanBooleanToObject<T> func, StringFunctionQuad stringFunction) {
        return put_bbb_o(fname, getType(), func, stringFunction);
    }

    // put_bbbb_o

    public  NodeFuncBooleanBooleanBooleanBooleanToObject<T> put_bbbb_t(String fname, IFuncBooleanBooleanBooleanBooleanToObject<T> func) {
        return put_bbbb_o(fname, getType(), func);
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToObject<T> put_bbbb_t(String fname, IFuncBooleanBooleanBooleanBooleanToObject<T> func, StringFunctionPenta stringFunction) {
        return put_bbbb_o(fname, getType(), func, stringFunction);
    }

    // put_ob_o

    public <A> NodeFuncObjectBooleanToObject<A, T> put_ob_t(String fname, Class<A> argTypeA, IFuncObjectBooleanToObject<A, T> func) {
        return put_ob_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectBooleanToObject<A, T> put_ob_t(String fname, Class<A> argTypeA, IFuncObjectBooleanToObject<A, T> func, StringFunctionTri stringFunction) {
        return put_ob_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectBooleanToObject<T, R> put_tb_o(String fname, Class<R> returnType, IFuncObjectBooleanToObject<T, R> func) {
        return put_ob_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectBooleanToObject<T, R> put_tb_o(String fname, Class<R> returnType, IFuncObjectBooleanToObject<T, R> func, StringFunctionTri stringFunction) {
        return put_ob_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectBooleanToObject<T, T> put_tb_t(String fname, IFuncObjectBooleanToObject<T, T> func) {
        return put_ob_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectBooleanToObject<T, T> put_tb_t(String fname, IFuncObjectBooleanToObject<T, T> func, StringFunctionTri stringFunction) {
        return put_ob_o(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oob_o
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectBooleanToObject<A, B, T> put_oob_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectBooleanToObject<A, B, T> func) {
        return put_oob_o(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectBooleanToObject<A, B, T> put_oob_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectBooleanToObject<A, B, T> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectBooleanToObject<T, B, R> put_tob_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectBooleanToObject<T, B, R> func) {
        return put_oob_o(fname, getType(), argTypeB, returnType, func);
    }

    public <B, R> NodeFuncObjectObjectBooleanToObject<T, B, R> put_tob_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectBooleanToObject<T, B, R> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, getType(), argTypeB, returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectBooleanToObject<T, B, T> put_tob_t(String fname, Class<B> argTypeB, IFuncObjectObjectBooleanToObject<T, B, T> func) {
        return put_oob_o(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectBooleanToObject<T, B, T> put_tob_t(String fname, Class<B> argTypeB, IFuncObjectObjectBooleanToObject<T, B, T> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectBooleanToObject<A, T, R> put_otb_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectBooleanToObject<A, T, R> func) {
        return put_oob_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectBooleanToObject<A, T, R> put_otb_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectBooleanToObject<A, T, R> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectBooleanToObject<A, T, T> put_otb_t(String fname, Class<A> argTypeA, IFuncObjectObjectBooleanToObject<A, T, T> func) {
        return put_oob_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectBooleanToObject<A, T, T> put_otb_t(String fname, Class<A> argTypeA, IFuncObjectObjectBooleanToObject<A, T, T> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectBooleanToObject<T, T, R> put_ttb_o(String fname, Class<R> returnType, IFuncObjectObjectBooleanToObject<T, T, R> func) {
        return put_oob_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectBooleanToObject<T, T, R> put_ttb_o(String fname, Class<R> returnType, IFuncObjectObjectBooleanToObject<T, T, R> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectBooleanToObject<T, T, T> put_ttb_t(String fname, IFuncObjectObjectBooleanToObject<T, T, T> func) {
        return put_oob_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectBooleanToObject<T, T, T> put_ttb_t(String fname, IFuncObjectObjectBooleanToObject<T, T, T> func, StringFunctionQuad stringFunction) {
        return put_oob_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    // put_o_o

    public <A> NodeFuncObjectToObject<A, T> put_o_t(String fname, Class<A> argTypeA, IFuncObjectToObject<A, T> func) {
        return put_o_o(fname, argTypeA, getType(), func);
    }

    public <A> NodeFuncObjectToObject<A, T> put_o_t(String fname, Class<A> argTypeA, IFuncObjectToObject<A, T> func, StringFunctionBi stringFunction) {
        return put_o_o(fname, argTypeA, getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectToObject<T, R> put_t_o(String fname, Class<R> returnType, IFuncObjectToObject<T, R> func) {
        return put_o_o(fname, getType(), returnType, func);
    }

    public <R> NodeFuncObjectToObject<T, R> put_t_o(String fname, Class<R> returnType, IFuncObjectToObject<T, R> func, StringFunctionBi stringFunction) {
        return put_o_o(fname, getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectToObject<T, T> put_t_t(String fname, IFuncObjectToObject<T, T> func) {
        return put_o_o(fname, getType(), getType(), func);
    }

    public  NodeFuncObjectToObject<T, T> put_t_t(String fname, IFuncObjectToObject<T, T> func, StringFunctionBi stringFunction) {
        return put_o_o(fname, getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oo_o
    //
    /////////////////////////

    public <A, B> NodeFuncObjectObjectToObject<A, B, T> put_oo_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToObject<A, B, T> func) {
        return put_oo_o(fname, argTypeA, argTypeB, getType(), func);
    }

    public <A, B> NodeFuncObjectObjectToObject<A, B, T> put_oo_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToObject<A, B, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, argTypeA, argTypeB, getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectToObject<T, B, R> put_to_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<T, B, R> func) {
        return put_oo_o(fname, getType(), argTypeB, returnType, func);
    }

    public <B, R> NodeFuncObjectObjectToObject<T, B, R> put_to_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<T, B, R> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, getType(), argTypeB, returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectToObject<T, B, T> put_to_t(String fname, Class<B> argTypeB, IFuncObjectObjectToObject<T, B, T> func) {
        return put_oo_o(fname, getType(), argTypeB, getType(), func);
    }

    public <B> NodeFuncObjectObjectToObject<T, B, T> put_to_t(String fname, Class<B> argTypeB, IFuncObjectObjectToObject<T, B, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, getType(), argTypeB, getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectToObject<A, T, R> put_ot_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectToObject<A, T, R> func) {
        return put_oo_o(fname, argTypeA, getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectToObject<A, T, R> put_ot_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectToObject<A, T, R> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, argTypeA, getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectToObject<A, T, T> put_ot_t(String fname, Class<A> argTypeA, IFuncObjectObjectToObject<A, T, T> func) {
        return put_oo_o(fname, argTypeA, getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectToObject<A, T, T> put_ot_t(String fname, Class<A> argTypeA, IFuncObjectObjectToObject<A, T, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, argTypeA, getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectToObject<T, T, R> put_tt_o(String fname, Class<R> returnType, IFuncObjectObjectToObject<T, T, R> func) {
        return put_oo_o(fname, getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectToObject<T, T, R> put_tt_o(String fname, Class<R> returnType, IFuncObjectObjectToObject<T, T, R> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectToObject<T, T, T> put_tt_t(String fname, IFuncObjectObjectToObject<T, T, T> func) {
        return put_oo_o(fname, getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectToObject<T, T, T> put_tt_t(String fname, IFuncObjectObjectToObject<T, T, T> func, StringFunctionTri stringFunction) {
        return put_oo_o(fname, getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_ooo_o
    //
    /////////////////////////

    public <A, B, C> NodeFuncObjectObjectObjectToObject<A, B, C, T> put_ooo_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, B, C, T> func) {
        return put_ooo_o(fname, argTypeA, argTypeB, argTypeC, getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectToObject<A, B, C, T> put_ooo_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, B, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <B, C, R> NodeFuncObjectObjectObjectToObject<T, B, C, R> put_too_o(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, C, R> func) {
        return put_ooo_o(fname, getType(), argTypeB, argTypeC, returnType, func);
    }

    public <B, C, R> NodeFuncObjectObjectObjectToObject<T, B, C, R> put_too_o(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, C, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), argTypeB, argTypeC, returnType, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectToObject<T, B, C, T> put_too_t(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, B, C, T> func) {
        return put_ooo_o(fname, getType(), argTypeB, argTypeC, getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectToObject<T, B, C, T> put_too_t(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, B, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), argTypeB, argTypeC, getType(), func, stringFunction);
    }

    public <A, C, R> NodeFuncObjectObjectObjectToObject<A, T, C, R> put_oto_o(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, C, R> func) {
        return put_ooo_o(fname, argTypeA, getType(), argTypeC, returnType, func);
    }

    public <A, C, R> NodeFuncObjectObjectObjectToObject<A, T, C, R> put_oto_o(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, C, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, getType(), argTypeC, returnType, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectToObject<A, T, C, T> put_oto_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, T, C, T> func) {
        return put_ooo_o(fname, argTypeA, getType(), argTypeC, getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectToObject<A, T, C, T> put_oto_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectToObject<A, T, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, getType(), argTypeC, getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectObjectObjectToObject<T, T, C, R> put_tto_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, C, R> func) {
        return put_ooo_o(fname, getType(), getType(), argTypeC, returnType, func);
    }

    public <C, R> NodeFuncObjectObjectObjectToObject<T, T, C, R> put_tto_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, C, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), getType(), argTypeC, returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectToObject<T, T, C, T> put_tto_t(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, T, C, T> func) {
        return put_ooo_o(fname, getType(), getType(), argTypeC, getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectToObject<T, T, C, T> put_tto_t(String fname, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, T, C, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), getType(), argTypeC, getType(), func, stringFunction);
    }

    public <A, B, R> NodeFuncObjectObjectObjectToObject<A, B, T, R> put_oot_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, T, R> func) {
        return put_ooo_o(fname, argTypeA, argTypeB, getType(), returnType, func);
    }

    public <A, B, R> NodeFuncObjectObjectObjectToObject<A, B, T, R> put_oot_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<A, B, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, argTypeB, getType(), returnType, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectToObject<A, B, T, T> put_oot_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToObject<A, B, T, T> func) {
        return put_ooo_o(fname, argTypeA, argTypeB, getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectToObject<A, B, T, T> put_oot_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectToObject<A, B, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, argTypeB, getType(), getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectObjectToObject<T, B, T, R> put_tot_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, T, R> func) {
        return put_ooo_o(fname, getType(), argTypeB, getType(), returnType, func);
    }

    public <B, R> NodeFuncObjectObjectObjectToObject<T, B, T, R> put_tot_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), argTypeB, getType(), returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectToObject<T, B, T, T> put_tot_t(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToObject<T, B, T, T> func) {
        return put_ooo_o(fname, getType(), argTypeB, getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectToObject<T, B, T, T> put_tot_t(String fname, Class<B> argTypeB, IFuncObjectObjectObjectToObject<T, B, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), argTypeB, getType(), getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectObjectToObject<A, T, T, R> put_ott_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, T, R> func) {
        return put_ooo_o(fname, argTypeA, getType(), getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectObjectToObject<A, T, T, R> put_ott_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectToObject<A, T, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, getType(), getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectToObject<A, T, T, T> put_ott_t(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToObject<A, T, T, T> func) {
        return put_ooo_o(fname, argTypeA, getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectToObject<A, T, T, T> put_ott_t(String fname, Class<A> argTypeA, IFuncObjectObjectObjectToObject<A, T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, argTypeA, getType(), getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectObjectToObject<T, T, T, R> put_ttt_o(String fname, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, T, R> func) {
        return put_ooo_o(fname, getType(), getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectObjectToObject<T, T, T, R> put_ttt_o(String fname, Class<R> returnType, IFuncObjectObjectObjectToObject<T, T, T, R> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectToObject<T, T, T, T> put_ttt_t(String fname, IFuncObjectObjectObjectToObject<T, T, T, T> func) {
        return put_ooo_o(fname, getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectToObject<T, T, T, T> put_ttt_t(String fname, IFuncObjectObjectObjectToObject<T, T, T, T> func, StringFunctionQuad stringFunction) {
        return put_ooo_o(fname, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    /////////////////////////
    //
    // put_oooo_o
    //
    /////////////////////////

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, T> put_oooo_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, C, D, T> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, argTypeC, argTypeD, getType(), func);
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, T> put_oooo_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, R> put_tooo_o(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, argTypeD, returnType, func);
    }

    public <B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, R> put_tooo_o(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, argTypeD, returnType, func, stringFunction);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, T> put_tooo_t(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, argTypeD, getType(), func);
    }

    public <B, C, D> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, T> put_tooo_t(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <A, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, R> put_otoo_o(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, D, R> func) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, argTypeD, returnType, func);
    }

    public <A, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, R> put_otoo_o(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, argTypeD, returnType, func, stringFunction);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, T> put_otoo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, C, D, T> func) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, argTypeD, getType(), func);
    }

    public <A, C, D> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, T> put_otoo_t(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, R> put_ttoo_o(String fname, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, D, R> func) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, argTypeD, returnType, func);
    }

    public <C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, R> put_ttoo_o(String fname, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, argTypeD, returnType, func, stringFunction);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, T> put_ttoo_t(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, argTypeD, getType(), func);
    }

    public <C, D> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, T> put_ttoo_t(String fname, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, argTypeD, getType(), func, stringFunction);
    }

    public <A, B, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, R> put_ooto_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, D, R> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), argTypeD, returnType, func);
    }

    public <A, B, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, R> put_ooto_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), argTypeD, returnType, func, stringFunction);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, T> put_ooto_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, T, D, T> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), argTypeD, getType(), func);
    }

    public <A, B, D> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, T> put_ooto_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, B, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), argTypeD, getType(), func, stringFunction);
    }

    public <B, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, R> put_toto_o(String fname, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, D, R> func) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), argTypeD, returnType, func);
    }

    public <B, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, R> put_toto_o(String fname, Class<B> argTypeB, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), argTypeD, returnType, func, stringFunction);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, T> put_toto_t(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, T, D, T> func) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), argTypeD, getType(), func);
    }

    public <B, D> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, T> put_toto_t(String fname, Class<B> argTypeB, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), argTypeD, getType(), func, stringFunction);
    }

    public <A, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, R> put_otto_o(String fname, Class<A> argTypeA, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, D, R> func) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), argTypeD, returnType, func);
    }

    public <A, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, R> put_otto_o(String fname, Class<A> argTypeA, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), argTypeD, returnType, func, stringFunction);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, T> put_otto_t(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, T, D, T> func) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), argTypeD, getType(), func);
    }

    public <A, D> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, T> put_otto_t(String fname, Class<A> argTypeA, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<A, T, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), argTypeD, getType(), func, stringFunction);
    }

    public <D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, R> put_ttto_o(String fname, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, D, R> func) {
        return put_oooo_o(fname, getType(), getType(), getType(), argTypeD, returnType, func);
    }

    public <D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, R> put_ttto_o(String fname, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, D, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), getType(), argTypeD, returnType, func, stringFunction);
    }

    public <D> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, T> put_ttto_t(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func) {
        return put_oooo_o(fname, getType(), getType(), getType(), argTypeD, getType(), func);
    }

    public <D> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, T> put_ttto_t(String fname, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), getType(), argTypeD, getType(), func, stringFunction);
    }

    public <A, B, C, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, R> put_ooot_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, T, R> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, argTypeC, getType(), returnType, func);
    }

    public <A, B, C, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, R> put_ooot_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, argTypeC, getType(), returnType, func, stringFunction);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, T> put_ooot_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, B, C, T, T> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, argTypeC, getType(), getType(), func);
    }

    public <A, B, C> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, T> put_ooot_t(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, B, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, argTypeC, getType(), getType(), func, stringFunction);
    }

    public <B, C, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, R> put_toot_o(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, T, R> func) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, getType(), returnType, func);
    }

    public <B, C, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, R> put_toot_o(String fname, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, getType(), returnType, func, stringFunction);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, T> put_toot_t(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, B, C, T, T> func) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, getType(), getType(), func);
    }

    public <B, C> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, T> put_toot_t(String fname, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, B, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, argTypeC, getType(), getType(), func, stringFunction);
    }

    public <A, C, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, R> put_otot_o(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, T, R> func) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, getType(), returnType, func);
    }

    public <A, C, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, R> put_otot_o(String fname, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, getType(), returnType, func, stringFunction);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, T> put_otot_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, T, C, T, T> func) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, getType(), getType(), func);
    }

    public <A, C> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, T> put_otot_t(String fname, Class<A> argTypeA, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<A, T, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), argTypeC, getType(), getType(), func, stringFunction);
    }

    public <C, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, R> put_ttot_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, T, R> func) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, getType(), returnType, func);
    }

    public <C, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, R> put_ttot_o(String fname, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, C, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, getType(), returnType, func, stringFunction);
    }

    public <C> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, T> put_ttot_t(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, T, C, T, T> func) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, getType(), getType(), func);
    }

    public <C> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, T> put_ttot_t(String fname, Class<C> argTypeC, IFuncObjectObjectObjectObjectToObject<T, T, C, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), argTypeC, getType(), getType(), func, stringFunction);
    }

    public <A, B, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, R> put_oott_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, T, R> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), getType(), returnType, func);
    }

    public <A, B, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, R> put_oott_o(String fname, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), getType(), returnType, func, stringFunction);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, T> put_oott_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<A, B, T, T, T> func) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), getType(), getType(), func);
    }

    public <A, B> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, T> put_oott_t(String fname, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<A, B, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, argTypeB, getType(), getType(), getType(), func, stringFunction);
    }

    public <B, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, R> put_tott_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, T, R> func) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), getType(), returnType, func);
    }

    public <B, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, R> put_tott_o(String fname, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), getType(), returnType, func, stringFunction);
    }

    public <B> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, T> put_tott_t(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<T, B, T, T, T> func) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), getType(), getType(), func);
    }

    public <B> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, T> put_tott_t(String fname, Class<B> argTypeB, IFuncObjectObjectObjectObjectToObject<T, B, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), argTypeB, getType(), getType(), getType(), func, stringFunction);
    }

    public <A, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, R> put_ottt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, T, R> func) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), getType(), returnType, func);
    }

    public <A, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, R> put_ottt_o(String fname, Class<A> argTypeA, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, T, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), getType(), returnType, func, stringFunction);
    }

    public <A> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, T> put_ottt_t(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToObject<A, T, T, T, T> func) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), getType(), getType(), func);
    }

    public <A> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, T> put_ottt_t(String fname, Class<A> argTypeA, IFuncObjectObjectObjectObjectToObject<A, T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, argTypeA, getType(), getType(), getType(), getType(), func, stringFunction);
    }

    public <R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, R> put_tttt_o(String fname, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, T, R> func) {
        return put_oooo_o(fname, getType(), getType(), getType(), getType(), returnType, func);
    }

    public <R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, R> put_tttt_o(String fname, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, T, T, T, R> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), getType(), getType(), returnType, func, stringFunction);
    }

    public  NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, T> put_tttt_t(String fname, IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func) {
        return put_oooo_o(fname, getType(), getType(), getType(), getType(), getType(), func);
    }

    public  NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, T> put_tttt_t(String fname, IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func, StringFunctionPenta stringFunction) {
        return put_oooo_o(fname, getType(), getType(), getType(), getType(), getType(), func, stringFunction);
    }

}
