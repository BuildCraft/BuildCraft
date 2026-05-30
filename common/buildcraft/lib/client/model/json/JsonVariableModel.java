/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model.json;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonParseException;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;

import buildcraft.lib.client.model.ModelUtil.TexturedFace;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.ITickableNode;

/**
 * STUB(R.Chen): model/json — Phase 5.
 *
 * The Forge original parsed BuildCraft's custom JSON model format (variable cuboids/LEDs with
 * expression-based parameters) using {@code ModelBlock}/{@code ModelLoader}. Porting to Fabric
 * requires reimplementing the parser against Fabric's {@code ModelLoadingPlugin} pipeline and
 * replacing all {@code javax.vecmath} math with JOML. Only the compile-time surface that
 * {@link buildcraft.lib.client.model.ModelHolderVariable} depends on is preserved.
 */
@Environment(EnvType.CLIENT)
public class JsonVariableModel {

    // STUB(R.Chen): public fields referenced by ModelHolderVariable / AdvModelCache.
    public final Map<String, JsonTexture> textures = Collections.emptyMap();
    public final JsonVariableModelPart[] cutoutElements = new JsonVariableModelPart[0];
    public final JsonVariableModelPart[] translucentElements = new JsonVariableModelPart[0];

    private JsonVariableModel() {}

    // STUB(R.Chen): Phase 5 — full JSON deserialization.
    public static JsonVariableModel deserialize(Identifier from, FunctionContext fnCtx)
        throws JsonParseException, IOException {
        throw new IOException("JsonVariableModel not yet implemented (Phase 5): " + from);
    }

    // STUB(R.Chen): Phase 5 — sprite registration.
    public void onTextureStitchPre(Identifier modelLocation, Set<Identifier> toRegisterSprites) {
        // no-op stub
    }

    // STUB(R.Chen): Phase 5 — bake quads from variable model parts.
    public MutableQuad[] bakePart(JsonVariableModelPart[] parts, ITextureGetter spriteLookup) {
        return MutableQuad.EMPTY_ARRAY;
    }

    // STUB(R.Chen): Phase 5 — tickable expression nodes.
    public ITickableNode[] createTickableNodes() {
        return new ITickableNode[0];
    }

    public interface ITextureGetter {
        TexturedFace get(String location);
    }
}
