/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;

import ct.buildcraft.api.transport.pipe.PipeDefinition;

import ct.buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import ct.buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;

public interface IPipeBaseModelGen {
//    DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.725");

    List<BakedQuad> generateCutout(PipeBaseCutoutKey key);

    List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key);

    TextureAtlasSprite[] getItemSprites(PipeDefinition def);

    void onTextureStitchPre(TextureStitchEvent.Pre event);
}
