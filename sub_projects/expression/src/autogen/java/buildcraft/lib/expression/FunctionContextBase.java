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
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToLong.IFuncLongLongLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong.IFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToLong.IFuncDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToLong.IFuncBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToLong.IFuncBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong.IFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongToDouble.IFuncLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToDouble.IFuncLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToDouble.IFuncLongLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToDouble.IFuncLongLongLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToDouble.IFuncObjectLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToDouble.IFuncObjectLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToDouble.IFuncDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToDouble.IFuncBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToDouble.IFuncBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToDouble.IFuncObjectBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble.IFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongToBoolean.IFuncLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToBoolean.IFuncLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToBoolean.IFuncLongLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToBoolean.IFuncLongLongLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToBoolean.IFuncObjectLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToBoolean.IFuncDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToBoolean.IFuncDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToBoolean.IFuncBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToBoolean.IFuncBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToBoolean.IFuncObjectBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean.IFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject.IFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToObject.IFuncLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToObject.IFuncLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject.IFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject.IFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToObject.IFuncBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject.IFuncObjectToObject;
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
