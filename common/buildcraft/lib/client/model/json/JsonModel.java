/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/** {@link BlockModel} but with different/additional features */
public class JsonModel {
    public final boolean ambientOcclusion;
    public final Map<String, String> textures;
    public final JsonModelPart[] cutoutElements, translucentElements;

    public static JsonModel deserialize(ResourceLocation from) throws JsonParseException, IOException {
        return deserialize(from, new ResourceLoaderContext());
    }

    public static JsonModel deserialize(ResourceLocation from, ResourceLoaderContext ctx) throws JsonParseException, IOException {
        try (InputStreamReader isr = ctx.startLoading(from)) {
            return new JsonModel(new Gson().fromJson(isr, JsonObject.class), ctx);
        } finally {
            ctx.finishLoading();
        }
    }

    public static void deserializePart(List<JsonModelPart> to, boolean translucent, JsonElement json, ResourceLoaderContext ctx) throws JsonParseException, IOException {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String str = json.getAsString();
            ResourceLocation parent = new ResourceLocation(str);
            JsonModel model = deserialize(parent, ctx);
            if (translucent) {
                Collections.addAll(to, model.translucentElements);
            } else {
                Collections.addAll(to, model.cutoutElements);
            }
        } else {
            to.add(new JsonModelPart(json, ctx));
        }
    }

    private static JsonModelPart[] deserializePartArray(JsonObject json, String member, boolean translucent, ResourceLoaderContext ctx) throws JsonParseException, IOException {
        if (!json.has(member)) {
            throw new JsonSyntaxException("Did not have '" + member + "' in '" + json + "'");
        }
        JsonElement elem = json.get(member);
        if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got '" + elem + "'");
        }
        JsonArray array = elem.getAsJsonArray();
        List<JsonModelPart> to = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            deserializePart(to, translucent, array.get(i), ctx);
        }
        return to.toArray(new JsonModelPart[to.size()]);
    }

    public JsonModel(JsonObject obj, ResourceLoaderContext ctx) throws JsonParseException, IOException {
//        ambientOcclusion = JsonUtils.getBoolean(obj, "ambientocclusion", false);
        ambientOcclusion = JSONUtils.getAsBoolean(obj, "ambientocclusion", false);
        textures = JsonUtil.getSubAsImmutableMap(obj, "textures", new TypeToken<HashMap<String, String>>() {
        });
        if (obj.has("elements")) {
            cutoutElements = deserializePartArray(obj, "elements", false, ctx);
            translucentElements = new JsonModelPart[0];
        } else {
            cutoutElements = deserializePartArray(obj, "cutout", false, ctx);
            translucentElements = deserializePartArray(obj, "translucent", true, ctx);
        }
    }
}
