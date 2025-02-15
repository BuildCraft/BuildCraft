/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** {@link BlockElement} but with a few extra features */
public class JsonModelPart {
    public final JsonQuad[] quads;

    public JsonModelPart(JsonElement json, ResourceLoaderContext ctx) {
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
            quads = readFace(obj);
        } else {
            quads = readCuboid(obj);
        }
    }

    private JsonModelPart(JsonQuad[] quads) {
        this.quads = quads;
    }

    private static JsonQuad[] readFace(JsonObject obj) {
        throw new AbstractMethodError("Implement this!");
    }

    private static float[] readFloatPositionSmaller(JsonObject obj, String member) {
        float[] got = JsonUtil.getSubAsFloatArray(obj, member);
        if (got.length != 3) {
            throw new JsonSyntaxException("Expected exactly 3 floats, but got " + Arrays.toString(got));
        } else {
            got[0] /= 16;
            got[1] /= 16;
            got[2] /= 16;
        }
        return got;
    }

    private static JsonQuad[] readCuboid(JsonObject obj) {
        float[] from = readFloatPositionSmaller(obj, "from");
        float[] to = readFloatPositionSmaller(obj, "to");
//        boolean shade = JsonUtils.getBoolean(obj, "shade", false);
        boolean shade = GsonHelper.getAsBoolean(obj, "shade", false);

        if (obj.has("faces")) {
            JsonElement faces = obj.get("faces");
            if (faces.isJsonObject()) {
                JsonObject jFaces = faces.getAsJsonObject();
                List<JsonQuad> quads = new ArrayList<>();
                for (Direction face : Direction.VALUES) {
                    if (jFaces.has(face.getName())) {
                        JsonElement jFace = jFaces.get(face.getName());
                        if (!jFace.isJsonObject()) {
                            throw new JsonSyntaxException("Expected an object, but got " + jFace);
                        }
                        JsonQuad q = new JsonQuad(jFace.getAsJsonObject(), from, to, face);
                        q.shade = shade;
                        quads.add(q);
                    }
                }
                if (quads.size() == 0) {
                    throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
                }
                return quads.toArray(new JsonQuad[quads.size()]);
            } else {
                throw new JsonSyntaxException("Expected between 1 and 6 faces, got " + faces);
            }
        } else {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
        }
    }
}
