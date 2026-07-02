/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.model;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

import ct.buildcraft.api.transport.pipe.PipeDefinition;

import ct.buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import ct.buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;

public enum PipeBaseModelGenConnected implements IPipeBaseModelGen {
    INSTANCE;

    // Textures
    private static final Map<PipeDefinition, TextureAtlasSprite[]> sprites = new IdentityHashMap<>();

	@Override
	public void onTextureStitchPre(Pre event) {
		PipeBaseModelGenStandard.INSTANCE.onTextureStitchPre(event);

        // TODO: Custom sprite creation + stitching
    }

    @Override
    public TextureAtlasSprite[] getItemSprites(PipeDefinition def) {
        return PipeBaseModelGenStandard.INSTANCE.getItemSprites(def);
    }

    // Models

    @Override
    public List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key) {
        return PipeBaseModelGenStandard.INSTANCE.generateTranslucent(key);
    }

    @Override
    public List<BakedQuad> generateCutout(PipeBaseCutoutKey key) {
        return ImmutableList.of();
    }



}
