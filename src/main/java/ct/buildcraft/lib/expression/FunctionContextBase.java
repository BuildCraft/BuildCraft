/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import ct.buildcraft.lib.expression.api.INodeFunc;
import ct.buildcraft.lib.expression.node.func.StringFunctionBi;
import ct.buildcraft.lib.expression.node.func.StringFunctionPenta;
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.StringFunctionTri;
import ct.buildcraft.lib.expression.node.func.gen.*;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanToBoolean.IFuncBooleanBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanToDouble.IFuncBooleanBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanToLong.IFuncBooleanBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanToBoolean.IFuncBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanToDouble.IFuncBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanToLong.IFuncBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncBooleanToObject.IFuncBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToBoolean.IFuncDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToLong.IFuncDoubleDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToBoolean.IFuncDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToDouble.IFuncDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToLong.IFuncDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToObject.IFuncDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongLongToBoolean.IFuncLongLongLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongLongToDouble.IFuncLongLongLongLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongLongToLong.IFuncLongLongLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongToBoolean.IFuncLongLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongToDouble.IFuncLongLongLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongToLong.IFuncLongLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongLongToObject.IFuncLongLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToBoolean.IFuncLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToDouble.IFuncLongLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToLong.IFuncLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToObject.IFuncLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToBoolean.IFuncLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToDouble.IFuncLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToLong.IFuncLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToObject.IFuncLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToDouble.IFuncObjectBooleanBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToBoolean.IFuncObjectBooleanToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToBoolean.IFuncObjectLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToDouble.IFuncObjectLongLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToDouble.IFuncObjectLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectLongToObject.IFuncObjectLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToLong.IFuncObjectObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToObject.IFuncObjectObjectToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToBoolean.IFuncObjectToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToDouble.IFuncObjectToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToLong.IFuncObjectToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectToObject.IFuncObjectToObject;


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

    public  NodeFuncLongLongLongLongToLong put_llll_l(String name, IFuncLongLongLongLongToLong func) {
        return putFunction(name, new NodeFuncLongLongLongLongToLong(name, func));
    }

    public  NodeFuncLongLongLongLongToLong put_llll_l(String name, IFuncLongLongLongLongToLong func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongLongToLong(func, stringFunction));
    }

    public <A> NodeFuncObjectLongToLong<A> put_ol_l(String name, Class<A> argTypeA, IFuncObjectLongToLong<A> func) {
        return putFunction(name, new NodeFuncObjectLongToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongToLong<A> put_ol_l(String name, Class<A> argTypeA, IFuncObjectLongToLong<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectLongToLong<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectLongLongToObject<A, R> put_oll_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectLongLongToObject<A, R> put_oll_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongLongToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongLongToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectLongLongToLong<A> put_oll_l(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> func) {
        return putFunction(name, new NodeFuncObjectLongLongToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongLongToLong<A> put_oll_l(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongLongToLong<>(argTypeA, func, stringFunction));
    }

    public  NodeFuncDoubleToLong put_d_l(String name, IFuncDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleToLong(name, func));
    }

    public  NodeFuncDoubleToLong put_d_l(String name, IFuncDoubleToLong func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncDoubleToLong(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleToLong put_dd_l(String name, IFuncDoubleDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleDoubleToLong(name, func));
    }

    public  NodeFuncDoubleDoubleToLong put_dd_l(String name, IFuncDoubleDoubleToLong func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleToLong(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleDoubleToLong put_ddd_l(String name, IFuncDoubleDoubleDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToLong(name, func));
    }

    public  NodeFuncDoubleDoubleDoubleToLong put_ddd_l(String name, IFuncDoubleDoubleDoubleToLong func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToLong(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToLong put_dddd_l(String name, IFuncDoubleDoubleDoubleDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToLong(name, func));
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToLong put_dddd_l(String name, IFuncDoubleDoubleDoubleDoubleToLong func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToLong(func, stringFunction));
    }

    public <A> NodeFuncObjectDoubleToLong<A> put_od_l(String name, Class<A> argTypeA, IFuncObjectDoubleToLong<A> func) {
        return putFunction(name, new NodeFuncObjectDoubleToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectDoubleToLong<A> put_od_l(String name, Class<A> argTypeA, IFuncObjectDoubleToLong<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleToLong<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectLongDoubleToObject<A, R> put_old_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongDoubleToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongDoubleToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectLongDoubleToObject<A, R> put_old_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongDoubleToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongDoubleToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectDoubleDoubleToLong<A> put_odd_l(String name, Class<A> argTypeA, IFuncObjectDoubleDoubleToLong<A> func) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectDoubleDoubleToLong<A> put_odd_l(String name, Class<A> argTypeA, IFuncObjectDoubleDoubleToLong<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToLong<>(argTypeA, func, stringFunction));
    }

    public  NodeFuncBooleanToLong put_b_l(String name, IFuncBooleanToLong func) {
        return putFunction(name, new NodeFuncBooleanToLong(name, func));
    }

    public  NodeFuncBooleanToLong put_b_l(String name, IFuncBooleanToLong func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncBooleanToLong(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanToLong put_bb_l(String name, IFuncBooleanBooleanToLong func) {
        return putFunction(name, new NodeFuncBooleanBooleanToLong(name, func));
    }

    public  NodeFuncBooleanBooleanToLong put_bb_l(String name, IFuncBooleanBooleanToLong func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanToLong(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanBooleanToLong put_bbb_l(String name, IFuncBooleanBooleanBooleanToLong func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToLong(name, func));
    }

    public  NodeFuncBooleanBooleanBooleanToLong put_bbb_l(String name, IFuncBooleanBooleanBooleanToLong func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToLong(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToLong put_bbbb_l(String name, IFuncBooleanBooleanBooleanBooleanToLong func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToLong(name, func));
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToLong put_bbbb_l(String name, IFuncBooleanBooleanBooleanBooleanToLong func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToLong(func, stringFunction));
    }

    public <A> NodeFuncObjectBooleanToLong<A> put_ob_l(String name, Class<A> argTypeA, IFuncObjectBooleanToLong<A> func) {
        return putFunction(name, new NodeFuncObjectBooleanToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectBooleanToLong<A> put_ob_l(String name, Class<A> argTypeA, IFuncObjectBooleanToLong<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanToLong<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectLongBooleanToObject<A, R> put_olb_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongBooleanToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongBooleanToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectLongBooleanToObject<A, R> put_olb_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongBooleanToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongBooleanToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectBooleanBooleanToLong<A> put_obb_l(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToLong<A> func) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectBooleanBooleanToLong<A> put_obb_l(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToLong<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToLong<>(argTypeA, func, stringFunction));
    }

    public <A> NodeFuncObjectToLong<A> put_o_l(String name, Class<A> argTypeA, IFuncObjectToLong<A> func) {
        return putFunction(name, new NodeFuncObjectToLong<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectToLong<A> put_o_l(String name, Class<A> argTypeA, IFuncObjectToLong<A> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncObjectToLong<>(argTypeA, func, stringFunction));
    }

    public <A, B> NodeFuncObjectObjectToLong<A, B> put_oo_l(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToLong<A, B> func) {
        return putFunction(name, new NodeFuncObjectObjectToLong<>(name, argTypeA, argTypeB, func));
    }

    public <A, B> NodeFuncObjectObjectToLong<A, B> put_oo_l(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToLong<A, B> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectToLong<>(argTypeA, argTypeB, func, stringFunction));
    }

    public <A, B, C> NodeFuncObjectObjectObjectToLong<A, B, C> put_ooo_l(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToLong<A, B, C> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectToLong<>(name, argTypeA, argTypeB, argTypeC, func));
    }

    public <A, B, C> NodeFuncObjectObjectObjectToLong<A, B, C> put_ooo_l(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToLong<A, B, C> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectToLong<>(argTypeA, argTypeB, argTypeC, func, stringFunction));
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToLong<A, B, C, D> put_oooo_l(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, B, C, D> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToLong<>(name, argTypeA, argTypeB, argTypeC, argTypeD, func));
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToLong<A, B, C, D> put_oooo_l(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, B, C, D> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToLong<>(argTypeA, argTypeB, argTypeC, argTypeD, func, stringFunction));
    }

    public <A, C, R> NodeFuncObjectLongObjectToObject<A, C, R> put_olo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectLongObjectToObject<A, C, R> func) {
        return putFunction(name, new NodeFuncObjectLongObjectToObject<>(name, argTypeA, argTypeC, returnType, func));
    }

    public <A, C, R> NodeFuncObjectLongObjectToObject<A, C, R> put_olo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectLongObjectToObject<A, C, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongObjectToObject<>(argTypeA, argTypeC, returnType, func, stringFunction));
    }

    public  NodeFuncLongToDouble put_l_d(String name, IFuncLongToDouble func) {
        return putFunction(name, new NodeFuncLongToDouble(name, func));
    }

    public  NodeFuncLongToDouble put_l_d(String name, IFuncLongToDouble func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncLongToDouble(func, stringFunction));
    }

    public  NodeFuncLongLongToDouble put_ll_d(String name, IFuncLongLongToDouble func) {
        return putFunction(name, new NodeFuncLongLongToDouble(name, func));
    }

    public  NodeFuncLongLongToDouble put_ll_d(String name, IFuncLongLongToDouble func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncLongLongToDouble(func, stringFunction));
    }

    public  NodeFuncLongLongLongToDouble put_lll_d(String name, IFuncLongLongLongToDouble func) {
        return putFunction(name, new NodeFuncLongLongLongToDouble(name, func));
    }

    public  NodeFuncLongLongLongToDouble put_lll_d(String name, IFuncLongLongLongToDouble func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongToDouble(func, stringFunction));
    }

    public  NodeFuncLongLongLongLongToDouble put_llll_d(String name, IFuncLongLongLongLongToDouble func) {
        return putFunction(name, new NodeFuncLongLongLongLongToDouble(name, func));
    }

    public  NodeFuncLongLongLongLongToDouble put_llll_d(String name, IFuncLongLongLongLongToDouble func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongLongToDouble(func, stringFunction));
    }

    public <A> NodeFuncObjectLongToDouble<A> put_ol_d(String name, Class<A> argTypeA, IFuncObjectLongToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectLongToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongToDouble<A> put_ol_d(String name, Class<A> argTypeA, IFuncObjectLongToDouble<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectLongToDouble<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectDoubleLongToObject<A, R> put_odl_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectDoubleLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectDoubleLongToObject<A, R> put_odl_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleLongToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleLongToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectLongLongToDouble<A> put_oll_d(String name, Class<A> argTypeA, IFuncObjectLongLongToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectLongLongToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongLongToDouble<A> put_oll_d(String name, Class<A> argTypeA, IFuncObjectLongLongToDouble<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongLongToDouble<>(argTypeA, func, stringFunction));
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

    public  NodeFuncDoubleDoubleDoubleDoubleToDouble put_dddd_d(String name, IFuncDoubleDoubleDoubleDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToDouble(name, func));
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToDouble put_dddd_d(String name, IFuncDoubleDoubleDoubleDoubleToDouble func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToDouble(func, stringFunction));
    }

    public <A> NodeFuncObjectDoubleToDouble<A> put_od_d(String name, Class<A> argTypeA, IFuncObjectDoubleToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectDoubleToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectDoubleToDouble<A> put_od_d(String name, Class<A> argTypeA, IFuncObjectDoubleToDouble<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleToDouble<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectDoubleDoubleToObject<A, R> put_odd_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleDoubleToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectDoubleDoubleToObject<A, R> put_odd_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleDoubleToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectDoubleDoubleToDouble<A> put_odd_d(String name, Class<A> argTypeA, IFuncObjectDoubleDoubleToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectDoubleDoubleToDouble<A> put_odd_d(String name, Class<A> argTypeA, IFuncObjectDoubleDoubleToDouble<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToDouble<>(argTypeA, func, stringFunction));
    }

    public  NodeFuncBooleanToDouble put_b_d(String name, IFuncBooleanToDouble func) {
        return putFunction(name, new NodeFuncBooleanToDouble(name, func));
    }

    public  NodeFuncBooleanToDouble put_b_d(String name, IFuncBooleanToDouble func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncBooleanToDouble(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanToDouble put_bb_d(String name, IFuncBooleanBooleanToDouble func) {
        return putFunction(name, new NodeFuncBooleanBooleanToDouble(name, func));
    }

    public  NodeFuncBooleanBooleanToDouble put_bb_d(String name, IFuncBooleanBooleanToDouble func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanToDouble(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanBooleanToDouble put_bbb_d(String name, IFuncBooleanBooleanBooleanToDouble func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToDouble(name, func));
    }

    public  NodeFuncBooleanBooleanBooleanToDouble put_bbb_d(String name, IFuncBooleanBooleanBooleanToDouble func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToDouble(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToDouble put_bbbb_d(String name, IFuncBooleanBooleanBooleanBooleanToDouble func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToDouble(name, func));
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToDouble put_bbbb_d(String name, IFuncBooleanBooleanBooleanBooleanToDouble func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToDouble(func, stringFunction));
    }

    public <A> NodeFuncObjectBooleanToDouble<A> put_ob_d(String name, Class<A> argTypeA, IFuncObjectBooleanToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectBooleanToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectBooleanToDouble<A> put_ob_d(String name, Class<A> argTypeA, IFuncObjectBooleanToDouble<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanToDouble<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectDoubleBooleanToObject<A, R> put_odb_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleBooleanToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectDoubleBooleanToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectDoubleBooleanToObject<A, R> put_odb_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleBooleanToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleBooleanToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectBooleanBooleanToDouble<A> put_obb_d(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToDouble<A> func) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToDouble<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectBooleanBooleanToDouble<A> put_obb_d(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToDouble<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToDouble<>(argTypeA, func, stringFunction));
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

    public <A, B, C> NodeFuncObjectObjectObjectToDouble<A, B, C> put_ooo_d(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<A, B, C> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectToDouble<>(name, argTypeA, argTypeB, argTypeC, func));
    }

    public <A, B, C> NodeFuncObjectObjectObjectToDouble<A, B, C> put_ooo_d(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToDouble<A, B, C> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectToDouble<>(argTypeA, argTypeB, argTypeC, func, stringFunction));
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToDouble<A, B, C, D> put_oooo_d(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, B, C, D> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToDouble<>(name, argTypeA, argTypeB, argTypeC, argTypeD, func));
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToDouble<A, B, C, D> put_oooo_d(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToDouble<A, B, C, D> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToDouble<>(argTypeA, argTypeB, argTypeC, argTypeD, func, stringFunction));
    }

    public <A, C, R> NodeFuncObjectDoubleObjectToObject<A, C, R> put_odo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectDoubleObjectToObject<A, C, R> func) {
        return putFunction(name, new NodeFuncObjectDoubleObjectToObject<>(name, argTypeA, argTypeC, returnType, func));
    }

    public <A, C, R> NodeFuncObjectDoubleObjectToObject<A, C, R> put_odo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectDoubleObjectToObject<A, C, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleObjectToObject<>(argTypeA, argTypeC, returnType, func, stringFunction));
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

    public  NodeFuncLongLongLongToBoolean put_lll_b(String name, IFuncLongLongLongToBoolean func) {
        return putFunction(name, new NodeFuncLongLongLongToBoolean(name, func));
    }

    public  NodeFuncLongLongLongToBoolean put_lll_b(String name, IFuncLongLongLongToBoolean func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongToBoolean(func, stringFunction));
    }

    public  NodeFuncLongLongLongLongToBoolean put_llll_b(String name, IFuncLongLongLongLongToBoolean func) {
        return putFunction(name, new NodeFuncLongLongLongLongToBoolean(name, func));
    }

    public  NodeFuncLongLongLongLongToBoolean put_llll_b(String name, IFuncLongLongLongLongToBoolean func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncLongLongLongLongToBoolean(func, stringFunction));
    }

    public <A> NodeFuncObjectLongToBoolean<A> put_ol_b(String name, Class<A> argTypeA, IFuncObjectLongToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectLongToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongToBoolean<A> put_ol_b(String name, Class<A> argTypeA, IFuncObjectLongToBoolean<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectLongToBoolean<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectBooleanLongToObject<A, R> put_obl_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectBooleanLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectBooleanLongToObject<A, R> put_obl_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanLongToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanLongToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectLongLongToBoolean<A> put_oll_b(String name, Class<A> argTypeA, IFuncObjectLongLongToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectLongLongToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectLongLongToBoolean<A> put_oll_b(String name, Class<A> argTypeA, IFuncObjectLongLongToBoolean<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectLongLongToBoolean<>(argTypeA, func, stringFunction));
    }

    public  NodeFuncDoubleToBoolean put_d_b(String name, IFuncDoubleToBoolean func) {
        return putFunction(name, new NodeFuncDoubleToBoolean(name, func));
    }

    public  NodeFuncDoubleToBoolean put_d_b(String name, IFuncDoubleToBoolean func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncDoubleToBoolean(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleToBoolean put_dd_b(String name, IFuncDoubleDoubleToBoolean func) {
        return putFunction(name, new NodeFuncDoubleDoubleToBoolean(name, func));
    }

    public  NodeFuncDoubleDoubleToBoolean put_dd_b(String name, IFuncDoubleDoubleToBoolean func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleToBoolean(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleDoubleToBoolean put_ddd_b(String name, IFuncDoubleDoubleDoubleToBoolean func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToBoolean(name, func));
    }

    public  NodeFuncDoubleDoubleDoubleToBoolean put_ddd_b(String name, IFuncDoubleDoubleDoubleToBoolean func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleToBoolean(func, stringFunction));
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToBoolean put_dddd_b(String name, IFuncDoubleDoubleDoubleDoubleToBoolean func) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToBoolean(name, func));
    }

    public  NodeFuncDoubleDoubleDoubleDoubleToBoolean put_dddd_b(String name, IFuncDoubleDoubleDoubleDoubleToBoolean func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncDoubleDoubleDoubleDoubleToBoolean(func, stringFunction));
    }

    public <A> NodeFuncObjectDoubleToBoolean<A> put_od_b(String name, Class<A> argTypeA, IFuncObjectDoubleToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectDoubleToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectDoubleToBoolean<A> put_od_b(String name, Class<A> argTypeA, IFuncObjectDoubleToBoolean<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleToBoolean<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectBooleanDoubleToObject<A, R> put_obd_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanDoubleToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectBooleanDoubleToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectBooleanDoubleToObject<A, R> put_obd_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanDoubleToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanDoubleToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectDoubleDoubleToBoolean<A> put_odd_b(String name, Class<A> argTypeA, IFuncObjectDoubleDoubleToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectDoubleDoubleToBoolean<A> put_odd_b(String name, Class<A> argTypeA, IFuncObjectDoubleDoubleToBoolean<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleDoubleToBoolean<>(argTypeA, func, stringFunction));
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

    public  NodeFuncBooleanBooleanBooleanToBoolean put_bbb_b(String name, IFuncBooleanBooleanBooleanToBoolean func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToBoolean(name, func));
    }

    public  NodeFuncBooleanBooleanBooleanToBoolean put_bbb_b(String name, IFuncBooleanBooleanBooleanToBoolean func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToBoolean(func, stringFunction));
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToBoolean put_bbbb_b(String name, IFuncBooleanBooleanBooleanBooleanToBoolean func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToBoolean(name, func));
    }

    public  NodeFuncBooleanBooleanBooleanBooleanToBoolean put_bbbb_b(String name, IFuncBooleanBooleanBooleanBooleanToBoolean func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToBoolean(func, stringFunction));
    }

    public <A> NodeFuncObjectBooleanToBoolean<A> put_ob_b(String name, Class<A> argTypeA, IFuncObjectBooleanToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectBooleanToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectBooleanToBoolean<A> put_ob_b(String name, Class<A> argTypeA, IFuncObjectBooleanToBoolean<A> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanToBoolean<>(argTypeA, func, stringFunction));
    }

    public <A, R> NodeFuncObjectBooleanBooleanToObject<A, R> put_obb_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanBooleanToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectBooleanBooleanToObject<A, R> put_obb_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanBooleanToObject<A, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A> NodeFuncObjectBooleanBooleanToBoolean<A> put_obb_b(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToBoolean<A> func) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToBoolean<>(name, argTypeA, func));
    }

    public <A> NodeFuncObjectBooleanBooleanToBoolean<A> put_obb_b(String name, Class<A> argTypeA, IFuncObjectBooleanBooleanToBoolean<A> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanBooleanToBoolean<>(argTypeA, func, stringFunction));
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

    public <A, B, C> NodeFuncObjectObjectObjectToBoolean<A, B, C> put_ooo_b(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<A, B, C> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectToBoolean<>(name, argTypeA, argTypeB, argTypeC, func));
    }

    public <A, B, C> NodeFuncObjectObjectObjectToBoolean<A, B, C> put_ooo_b(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToBoolean<A, B, C> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectToBoolean<>(argTypeA, argTypeB, argTypeC, func, stringFunction));
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, D> put_oooo_b(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, B, C, D> func) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToBoolean<>(name, argTypeA, argTypeB, argTypeC, argTypeD, func));
    }

    public <A, B, C, D> NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, D> put_oooo_b(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToBoolean<A, B, C, D> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectObjectObjectToBoolean<>(argTypeA, argTypeB, argTypeC, argTypeD, func, stringFunction));
    }

    public <A, C, R> NodeFuncObjectBooleanObjectToObject<A, C, R> put_obo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectBooleanObjectToObject<A, C, R> func) {
        return putFunction(name, new NodeFuncObjectBooleanObjectToObject<>(name, argTypeA, argTypeC, returnType, func));
    }

    public <A, C, R> NodeFuncObjectBooleanObjectToObject<A, C, R> put_obo_o(String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, IFuncObjectBooleanObjectToObject<A, C, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanObjectToObject<>(argTypeA, argTypeC, returnType, func, stringFunction));
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

    public <A, R> NodeFuncObjectLongToObject<A, R> put_ol_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectLongToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectLongToObject<A, R> put_ol_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectLongToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A, B, R> NodeFuncObjectObjectLongToObject<A, B, R> put_ool_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectLongToObject<A, B, R> func) {
        return putFunction(name, new NodeFuncObjectObjectLongToObject<>(name, argTypeA, argTypeB, returnType, func));
    }

    public <A, B, R> NodeFuncObjectObjectLongToObject<A, B, R> put_ool_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectLongToObject<A, B, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectLongToObject<>(argTypeA, argTypeB, returnType, func, stringFunction));
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

    public <A, R> NodeFuncObjectDoubleToObject<A, R> put_od_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectDoubleToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectDoubleToObject<A, R> put_od_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectDoubleToObject<A, R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectDoubleToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A, B, R> NodeFuncObjectObjectDoubleToObject<A, B, R> put_ood_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectDoubleToObject<A, B, R> func) {
        return putFunction(name, new NodeFuncObjectObjectDoubleToObject<>(name, argTypeA, argTypeB, returnType, func));
    }

    public <A, B, R> NodeFuncObjectObjectDoubleToObject<A, B, R> put_ood_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectDoubleToObject<A, B, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectDoubleToObject<>(argTypeA, argTypeB, returnType, func, stringFunction));
    }

    public <R> NodeFuncBooleanToObject<R> put_b_o(String name, Class<R> returnType, IFuncBooleanToObject<R> func) {
        return putFunction(name, new NodeFuncBooleanToObject<>(name, returnType, func));
    }

    public <R> NodeFuncBooleanToObject<R> put_b_o(String name, Class<R> returnType, IFuncBooleanToObject<R> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncBooleanToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncBooleanBooleanToObject<R> put_bb_o(String name, Class<R> returnType, IFuncBooleanBooleanToObject<R> func) {
        return putFunction(name, new NodeFuncBooleanBooleanToObject<>(name, returnType, func));
    }

    public <R> NodeFuncBooleanBooleanToObject<R> put_bb_o(String name, Class<R> returnType, IFuncBooleanBooleanToObject<R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncBooleanBooleanBooleanToObject<R> put_bbb_o(String name, Class<R> returnType, IFuncBooleanBooleanBooleanToObject<R> func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToObject<>(name, returnType, func));
    }

    public <R> NodeFuncBooleanBooleanBooleanToObject<R> put_bbb_o(String name, Class<R> returnType, IFuncBooleanBooleanBooleanToObject<R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanToObject<>(returnType, func, stringFunction));
    }

    public <R> NodeFuncBooleanBooleanBooleanBooleanToObject<R> put_bbbb_o(String name, Class<R> returnType, IFuncBooleanBooleanBooleanBooleanToObject<R> func) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToObject<>(name, returnType, func));
    }

    public <R> NodeFuncBooleanBooleanBooleanBooleanToObject<R> put_bbbb_o(String name, Class<R> returnType, IFuncBooleanBooleanBooleanBooleanToObject<R> func, StringFunctionPenta stringFunction) {
        return putFunction(name, new NodeFuncBooleanBooleanBooleanBooleanToObject<>(returnType, func, stringFunction));
    }

    public <A, R> NodeFuncObjectBooleanToObject<A, R> put_ob_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectBooleanToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectBooleanToObject<A, R> put_ob_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectBooleanToObject<A, R> func, StringFunctionTri stringFunction) {
        return putFunction(name, new NodeFuncObjectBooleanToObject<>(argTypeA, returnType, func, stringFunction));
    }

    public <A, B, R> NodeFuncObjectObjectBooleanToObject<A, B, R> put_oob_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectBooleanToObject<A, B, R> func) {
        return putFunction(name, new NodeFuncObjectObjectBooleanToObject<>(name, argTypeA, argTypeB, returnType, func));
    }

    public <A, B, R> NodeFuncObjectObjectBooleanToObject<A, B, R> put_oob_o(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectBooleanToObject<A, B, R> func, StringFunctionQuad stringFunction) {
        return putFunction(name, new NodeFuncObjectObjectBooleanToObject<>(argTypeA, argTypeB, returnType, func, stringFunction));
    }

    public <A, R> NodeFuncObjectToObject<A, R> put_o_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectToObject<A, R> func) {
        return putFunction(name, new NodeFuncObjectToObject<>(name, argTypeA, returnType, func));
    }

    public <A, R> NodeFuncObjectToObject<A, R> put_o_o(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectToObject<A, R> func, StringFunctionBi stringFunction) {
        return putFunction(name, new NodeFuncObjectToObject<>(argTypeA, returnType, func, stringFunction));
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
