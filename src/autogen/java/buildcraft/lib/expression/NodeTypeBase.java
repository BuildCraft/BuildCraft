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
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean.IFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject.IFuncObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject.IFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject.IFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject;


// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public abstract class NodeTypeBase<T> extends FunctionContext {

    protected abstract Class<T> getType();

    public  INodeFuncLong put_t_l(String name, IFuncObjectToLong<T> func) {
        return put_o_l(name, getType(), func);
    }

    public  INodeFuncLong put_tl_l(String name, IFuncObjectLongToLong<T> func) {
        return put_ol_l(name, getType(), func);
    }

    public  INodeFuncLong put_tll_l(String name, IFuncObjectLongLongToLong<T> func) {
        return put_oll_l(name, getType(), func);
    }

    public  INodeFuncBoolean put_t_b(String name, IFuncObjectToBoolean<T> func) {
        return put_o_b(name, getType(), func);
    }

    public <R> INodeFuncObject<R> put_t_o(String name, Class<R> returnType, IFuncObjectToObject<T, R> func) {
        return put_o_o(name, getType(), returnType, func);
    }

    public  INodeFuncObject<T> put_t_t(String name, IFuncObjectToObject<T, T> func) {
        return put_o_o(name, getType(), getType(), func);
    }

    public <R> INodeFuncObject<R> put_tl_o(String name, Class<R> returnType, IFuncObjectLongToObject<T, R> func) {
        return put_ol_o(name, getType(), returnType, func);
    }

    public  INodeFuncObject<T> put_tl_t(String name, IFuncObjectLongToObject<T, T> func) {
        return put_ol_o(name, getType(), getType(), func);
    }

    public <R> INodeFuncObject<R> put_tll_o(String name, Class<R> returnType, IFuncObjectLongLongToObject<T, R> func) {
        return put_oll_o(name, getType(), returnType, func);
    }

    public  INodeFuncObject<T> put_tll_t(String name, IFuncObjectLongLongToObject<T, T> func) {
        return put_oll_o(name, getType(), getType(), func);
    }

    public <B, R> INodeFuncObject<R> put_to_o(String name, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectToObject<T, B, R> func) {
        return put_oo_o(name, getType(), argTypeB, returnType, func);
    }

    public <B> INodeFuncObject<T> put_to_t(String name, Class<B> argTypeB, IFuncObjectObjectToObject<T, B, T> func) {
        return put_oo_o(name, getType(), argTypeB, getType(), func);
    }

    public  INodeFuncObject<T> put_tt_t(String name, IFuncObjectObjectToObject<T, T, T> func) {
        return put_oo_o(name, getType(), getType(), getType(), func);
    }

    public <B, C, R> INodeFuncObject<R> put_too_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<R> returnType, IFuncObjectObjectObjectToObject<T, B, C, R> func) {
        return put_ooo_o(name, getType(), argTypeB, argTypeC, returnType, func);
    }

    public <B, C> INodeFuncObject<T> put_too_t(String name, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, B, C, T> func) {
        return put_ooo_o(name, getType(), argTypeB, argTypeC, getType(), func);
    }

    public <C> INodeFuncObject<T> put_tto_t(String name, Class<C> argTypeC, IFuncObjectObjectObjectToObject<T, T, C, T> func) {
        return put_ooo_o(name, getType(), getType(), argTypeC, getType(), func);
    }

    public  INodeFuncObject<T> put_ttt_t(String name, IFuncObjectObjectObjectToObject<T, T, T, T> func) {
        return put_ooo_o(name, getType(), getType(), getType(), getType(), func);
    }

    public <B, C, D, R> INodeFuncObject<R> put_tooo_o(String name, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, argTypeD, returnType, func);
    }

    public <B, C, D> INodeFuncObject<T> put_tooo_t(String name, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func) {
        return put_oooo_o(name, getType(), argTypeB, argTypeC, argTypeD, getType(), func);
    }

    public <C, D> INodeFuncObject<T> put_ttoo_t(String name, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func) {
        return put_oooo_o(name, getType(), getType(), argTypeC, argTypeD, getType(), func);
    }

    public <D> INodeFuncObject<T> put_ttto_t(String name, Class<D> argTypeD, IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func) {
        return put_oooo_o(name, getType(), getType(), getType(), argTypeD, getType(), func);
    }

    public  INodeFuncObject<T> put_tttt_t(String name, IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func) {
        return put_oooo_o(name, getType(), getType(), getType(), getType(), getType(), func);
    }

}
