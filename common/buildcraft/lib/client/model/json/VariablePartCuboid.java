/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.ModelUtil.TexturedFace;
import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;

public class VariablePartCuboid extends VariablePartCuboidBase {
    public final Map<EnumFacing, JsonVariableFaceUV> faces = new HashMap<>();

    public VariablePartCuboid(JsonObject obj, FunctionContext fnCtx) {
        super(obj, fnCtx);
        if (!obj.has("faces")) {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
        }
        JsonElement elem = obj.get("faces");
        if (!elem.isJsonObject()) {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got '" + elem + "'");
        }
        JsonObject jFaces = elem.getAsJsonObject();
        for (EnumFacing face : EnumFacing.VALUES) {
            if (jFaces.has(face.getName())) {
                JsonElement jFace = jFaces.get(face.getName());
                if (!jFace.isJsonObject()) {
                    throw new JsonSyntaxException("Expected an object, but got " + jFace);
                }
                faces.put(face, new JsonVariableFaceUV(jFace.getAsJsonObject(), fnCtx));
            }
        }
        if (faces.size() == 0) {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
        }
    }

    @Override
    protected VariableFaceData getFaceData(EnumFacing side, ITextureGetter spriteLookup) {
        JsonVariableFaceUV var = faces.get(side);
        if (var == null || !var.visible.evaluate()) {
            return null;
        }
        VariableFaceData data = new VariableFaceData();
        TexturedFace face = spriteLookup.get(var.texture.evaluate());
        data.sprite = face.sprite;
        data.rotations = (int) var.textureRotation.evaluate();
        data.uvs.minU = (float) (var.uv[0].evaluate() / 16.0);
        data.uvs.minV = (float) (var.uv[1].evaluate() / 16.0);
        data.uvs.maxU = (float) (var.uv[2].evaluate() / 16.0);
        data.uvs.maxV = (float) (var.uv[3].evaluate() / 16.0);
        data.uvs = data.uvs.inParent(face.faceData);
        return data;
    }
}
