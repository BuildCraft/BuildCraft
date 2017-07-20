/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;

public class JsonVariableFaceUV {
    final INodeDouble[] uv;
    final INodeLong textureRotation;
    final INodeBoolean visible;
    final INodeString texture;

    public JsonVariableFaceUV(JsonObject json, FunctionContext fnCtx) {
        uv = readVariableUV(json, "uv", fnCtx);
        if (json.has("visible")) {
            visible = JsonVariableModelPart.readVariableBoolean(json, "visible", fnCtx);
        } else {
            visible = NodeConstantBoolean.TRUE;
        }
        texture = readVariableString(json, "texture", fnCtx);
        if (json.has("rotation")) {
            textureRotation = JsonVariableModelPart.readVariableLong(json, "rotation", fnCtx);
        } else {
            textureRotation = NodeConstantLong.ZERO;
        }
    }

    private static INodeString readVariableString(JsonObject json, String member, FunctionContext fnCtx) {
        if (!json.has(member)) {
            throw new JsonSyntaxException("Required member " + member + " in '" + json + "'");
        }
        JsonElement elem = json.get(member);
        if (!elem.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected a string, but got '" + json + "'");
        }
        String asString = elem.getAsString();
        if (asString.startsWith("#")) {
            // Its a simple texture definition
            asString = "'" + asString + "'";
        }
        return JsonVariableModelPart.convertStringToStringNode(asString, fnCtx);
    }

    public static INodeDouble[] readVariableUV(JsonObject obj, String member, FunctionContext fnCtx) {
        String[] got = JsonUtil.getSubAsStringArray(obj, member);
        INodeDouble[] to = new INodeDouble[4];
        if (got.length != 4) {
            throw new JsonSyntaxException("Expected exactly 4 doubles, but got " + Arrays.toString(got));
        } else {
            to[0] = JsonVariableModelPart.convertStringToDoubleNode(got[0], fnCtx);
            to[1] = JsonVariableModelPart.convertStringToDoubleNode(got[1], fnCtx);
            to[2] = JsonVariableModelPart.convertStringToDoubleNode(got[2], fnCtx);
            to[3] = JsonVariableModelPart.convertStringToDoubleNode(got[3], fnCtx);
        }
        return to;
    }
}
