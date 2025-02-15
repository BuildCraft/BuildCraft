/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

public class VariablePartCuboid extends VariablePartCuboidBase {
    public final Map<Direction, JsonVariableFaceUV> faces = new HashMap<>();

    public VariablePartCuboid(JsonObject obj, FunctionContext fnCtx) {
        super(obj, fnCtx);
        if (!obj.has("faces")) {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
        }
        String invertDefault = null;
        if (obj.has("invert")) {
            invertDefault = JsonUtil.getAsString(obj.get("invert"));
        }
        String bothSides = null;
        if (obj.has("both_sides")) {
            bothSides = JsonUtil.getAsString(obj.get("both_sides"));
        }
        JsonElement elem = obj.get("faces");
        if (!elem.isJsonObject()) {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got '" + elem + "'");
        }
        JsonObject jFaces = elem.getAsJsonObject();
        for (Direction face : Direction.VALUES) {
            if (jFaces.has(face.getName())) {
                JsonElement jFace = jFaces.get(face.getName());
                if (!jFace.isJsonObject()) {
                    throw new JsonSyntaxException("Expected an object, but got " + jFace);
                }
                JsonObject jFaceObj = jFace.getAsJsonObject();
                if (invertDefault != null && !jFaceObj.has("invert")) {
                    jFaceObj.addProperty("invert", invertDefault);
                }
                if (bothSides != null && !jFaceObj.has("both_sides")) {
                    jFaceObj.addProperty("both_sides", bothSides);
                }
                faces.put(face, new JsonVariableFaceUV(jFaceObj, fnCtx));
            }
        }
        if (faces.size() == 0) {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
        }
    }

    @Override
    protected VariableFaceData getFaceData(Direction side, ITextureGetter spriteLookup) {
        JsonVariableFaceUV var = faces.get(side);
        if (var == null || !var.visible.evaluate()) {
            return null;
        }
        return var.evaluate(spriteLookup);
    }
}
