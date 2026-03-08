/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.List;

/** {@link JsonModelPart} but with can be animated */
public abstract class JsonVariableModelPart {

    public abstract void addQuads(List<MutableQuad> to, ITextureGetter spriteLookup);

    public static JsonVariableModelPart deserializeModelPart(JsonElement json, FunctionContext fnCtx, ResourceLoaderContext ctx) {
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected an object, got " + json);
        }
        JsonObject obj = json.getAsJsonObject();
        String type = "cuboid";
        if (obj.has("type")) {
            JsonElement jType = obj.get("type");
            if (jType.isJsonPrimitive()) {
                JsonPrimitive prim = jType.getAsJsonPrimitive();
                type = prim.getAsString();
            } else {
                throw new JsonSyntaxException("Expected a string, got " + jType);
            }
        }
        if ("face".equals(type)) {
            throw new AbstractMethodError("// TODO: Implement face type!");
        } else if ("led".equals(type)) {
            return new VariablePartLed(obj, fnCtx);
        } else if ("texture_expand".equals(type)) {
            return new VariablePartTextureExpand(obj, fnCtx);
        } else if ("cuboid".equals(type)) {
            return new VariablePartCuboid(obj, fnCtx);
        } else if ("container".equals(type)) {
            return new VariablePartContainer(obj, fnCtx, ctx);
        } else {
            throw new JsonSyntaxException(
                    "Unknown type '" + type + "' -- known types are [ face, led, texture_expand, cuboid, container ]");
        }
    }

    public static INodeDouble convertStringToDoubleNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionDouble(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression " + expression, e);
        }
    }

    public static INodeObject<String> convertStringToStringNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionString(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression " + expression, e);
        }
    }

    public static INodeBoolean convertStringToBooleanNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression " + expression, e);
        }
    }

    public static INodeLong convertStringToLongNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionLong(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression " + expression, e);
        }
    }

    public static <T> INodeObject<T> convertStringToObjectNode(String expression, FunctionContext context, Class<T> clazz) {
        try {
            return GenericExpressionCompiler.compileExpressionObject(clazz, expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression " + expression, e);
        }
    }

    // private static JsonVariableQuad[] readFace(JsonObject obj, FunctionContext fnCtx) {
    // throw new AbstractMethodError("Implement this!");
    // }

    public static INodeDouble[] readVariablePosition(JsonObject obj, String member, FunctionContext fnCtx) {
        String[] got = JsonUtil.getSubAsStringArray(obj, member);
        INodeDouble[] to = new INodeDouble[3];
        if (got.length != 3) {
            throw new JsonSyntaxException("Expected exactly 3 floats, but got " + Arrays.toString(got));
        } else {
            to[0] = convertStringToDoubleNode(got[0], fnCtx);
            to[1] = convertStringToDoubleNode(got[1], fnCtx);
            to[2] = convertStringToDoubleNode(got[2], fnCtx);
        }
        return to;
    }

    public static INodeBoolean readVariableBoolean(JsonObject obj, String member, FunctionContext context) {
        if (!obj.has(member)) {
            throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonPrimitive()) {
            return convertStringToBooleanNode(elem.getAsString(), context);
        } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
        }
    }

    public static INodeLong readVariableLong(JsonObject obj, String member, FunctionContext context) {
        if (!obj.has(member)) {
            throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonPrimitive()) {
            return convertStringToLongNode(elem.getAsString(), context);
        } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
        }
    }

    public static INodeObject<String> readVariableString(JsonObject obj, String member, FunctionContext context) {
        if (!obj.has(member)) {
            throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonPrimitive()) {
            return convertStringToStringNode(elem.getAsString(), context);
        } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
        }
    }

    public static float[] bakePosition(INodeDouble[] in) {
        float x = (float) in[0].evaluate() / 16f;
        float y = (float) in[1].evaluate() / 16f;
        float z = (float) in[2].evaluate() / 16f;
        return new float[] { x, y, z };
    }
}
