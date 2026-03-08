/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.LazyValue;

import java.util.Arrays;

public class JsonVariableFaceUV {
    final INodeDouble[] uv;
    final INodeLong textureRotation;
    final INodeBoolean visible;
    final INodeBoolean invert;
    final INodeBoolean bothSides;
    final INodeObject<String> texture;

    public JsonVariableFaceUV(JsonObject json, FunctionContext fnCtx) {
        uv = readVariableUV(json, "uv", fnCtx);
        if (json.has("visible")) {
            visible = JsonVariableModelPart.readVariableBoolean(json, "visible", fnCtx);
        } else {
            visible = NodeConstantBoolean.TRUE;
        }
        if (json.has("invert")) {
            invert = JsonVariableModelPart.readVariableBoolean(json, "invert", fnCtx);
        } else {
            invert = NodeConstantBoolean.FALSE;
        }
        if (json.has("both_sides")) {
            bothSides = JsonVariableModelPart.readVariableBoolean(json, "both_sides", fnCtx);
        } else {
            bothSides = NodeConstantBoolean.FALSE;
        }
        texture = readVariableString(json, "texture", fnCtx);
        if (json.has("rotation")) {
            textureRotation = JsonVariableModelPart.readVariableLong(json, "rotation", fnCtx);
        } else {
            textureRotation = NodeConstantLong.ZERO;
        }
    }

    private static INodeObject<String> readVariableString(JsonObject json, String member, FunctionContext fnCtx) {
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

    public VariablePartCuboidBase.VariableFaceData evaluate(JsonVariableModel.ITextureGetter spriteLookup) {
        VariablePartCuboidBase.VariableFaceData data = new VariablePartCuboidBase.VariableFaceData();
        ModelUtil.TexturedFace face = spriteLookup.get(texture.evaluate());
//        data.sprite = face.sprite;
        data.sprite = new LazyValue<>(() -> face.sprite);
        data.rotations = (int) textureRotation.evaluate();
        data.uvs.minU = (float) (uv[0].evaluate() / 16.0);
        data.uvs.minV = (float) (uv[1].evaluate() / 16.0);
        data.uvs.maxU = (float) (uv[2].evaluate() / 16.0);
        data.uvs.maxV = (float) (uv[3].evaluate() / 16.0);
        data.uvs = data.uvs.inParent(face.faceData);
        data.invertNormal = invert.evaluate();
        data.bothSides = bothSides.evaluate();
        return data;
    }
}
