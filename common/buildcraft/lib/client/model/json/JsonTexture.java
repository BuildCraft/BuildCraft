/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.misc.MathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;

public class JsonTexture {
    public final String location;
    public final UvFaceData faceData;

    public JsonTexture(String location, UvFaceData faceData) {
        this.location = location;
        this.faceData = faceData;
    }

    public JsonTexture(String location, double minU, double minV, double maxU, double maxV) {
        this.location = location;
        faceData = new UvFaceData(minU, minV, maxU, maxV);
    }

    public JsonTexture(String location) {
        this(location, 0, 0, 1, 1);
    }

    public JsonTexture(JsonObject obj) {
        try {
//            location = JsonUtils.getString(obj, "location");
            location = JSONUtils.getAsString(obj, "location");
//            JsonArray uvs = JsonUtils.getJsonArray(obj, "uv");
            JsonArray uvs = JSONUtils.getAsJsonArray(obj, "uv");
            if (uvs.size() != 4) {
                throw new JsonSyntaxException("Must have 4 elements (uMin, vMin, uMax, vMax)");
            }
            double[] arr = new double[4];
            for (int i = 0; i < 4; i++) {
                JsonElement elem = uvs.get(i);
                if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
                    arr[i] = elem.getAsDouble();
                } else if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                    try {
                        arr[i] = GenericExpressionCompiler.compileExpressionDouble(elem.getAsString(), DefaultContexts.createWithAll()).evaluate();
                    } catch (InvalidExpressionException e) {
                        throw new JsonSyntaxException("in " + elem, e);
                    }
                } else {
                    throw new JsonSyntaxException("Expected a number or a double expression!");
                }
            }
            faceData = new UvFaceData();
            faceData.minU = (float) MathUtil.clamp(arr[0] / 16.0, 0, 1.0);
            faceData.minV = (float) MathUtil.clamp(arr[1] / 16.0, 0, 1.0);
            faceData.maxU = (float) MathUtil.clamp(arr[2] / 16.0, 0, 1.0);
            faceData.maxV = (float) MathUtil.clamp(arr[3] / 16.0, 0, 1.0);
        } catch (JsonSyntaxException jse) {
            throw new JsonSyntaxException("in " + obj, jse);
        }
    }

    public JsonTexture andSub(JsonTexture sub) {
        UvFaceData data = faceData.andSub(sub.faceData);
        return new JsonTexture(location, data);
    }

    public JsonTexture inParent(JsonTexture parent) {
        return parent.andSub(this);
    }

    @Override
    public String toString() {
        return "location = " + location + ", uvs = " + faceData;
    }
}
