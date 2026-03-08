/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.config.DetailedConfigOption;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.List;

public interface IPipeBaseModelGen {
    DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.725");

    List<BakedQuad> generateCutout(PipeBaseCutoutKey key);

    List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key);

    TextureAtlasSprite[] getItemSprites(PipeDefinition def);

    void onTextureStitchPre(TextureStitchEvent.Pre event);

    void onTextureStitchPost(TextureStitchEvent.Post event);
}
