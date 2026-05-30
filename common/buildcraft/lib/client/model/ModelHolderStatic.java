/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model;

import java.util.Set;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;

/**
 * STUB(R.Chen): client model — Phase 5. Also @Deprecated in the original source.
 *
 * The Forge original loaded a static JSON model and baked it during ModelBakeEvent + TextureStitchEvent.
 * Phase 5 will replace with ModelLoadingPlugin. Bodies return empty/null until then.
 *
 * @deprecated Unused in the original code — a lot of duplicated code with ModelHolderVariable.
 */
@Deprecated
@Environment(EnvType.CLIENT)
public class ModelHolderStatic extends ModelHolder {
    private final ImmutableMap<String, String> textureLookup;
    private final boolean allowTextureFallthrough;
    private MutableQuad[][] quads;

    public ModelHolderStatic(String location) {
        this(location, ImmutableMap.of(), false);
    }

    public ModelHolderStatic(String location, String[][] textures, boolean allowTextureFallthrough) {
        this(location, genTextureMap(textures), allowTextureFallthrough);
    }

    public ModelHolderStatic(String modelLocation, ImmutableMap<String, String> textureLookup,
        boolean allowTextureFallthrough) {
        super(modelLocation);
        this.textureLookup = textureLookup;
        this.allowTextureFallthrough = allowTextureFallthrough;
    }

    @Override
    public boolean hasBakedQuads() {
        return quads != null;
    }

    private static ImmutableMap<String, String> genTextureMap(String[][] textures) {
        if (textures == null || textures.length == 0) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (String[] ar : textures) {
            if (ar.length != 2) {
                throw new IllegalArgumentException("Must have 2 elements (key,value) but got " + ar.length);
            }
            builder.put(ar[0], ar[1]);
        }
        return builder.build();
    }

    @Override
    protected void onTextureStitchPre(Set<Identifier> toRegisterSprites) {
        // STUB(R.Chen): Phase 5
    }

    @Override
    protected void onModelBake() {
        // STUB(R.Chen): Phase 5
    }

    public MutableQuad[] getCutoutQuads() {
        return quads != null ? quads[0] : MutableQuad.EMPTY_ARRAY;
    }

    public MutableQuad[] getTranslucentQuads() {
        return quads != null ? quads[1] : MutableQuad.EMPTY_ARRAY;
    }
}
