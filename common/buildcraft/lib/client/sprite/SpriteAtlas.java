/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.misc.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteAtlas implements ISprite {
    public final TextureAtlasSprite sprite;

    public SpriteAtlas(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void bindTexture() {
        SpriteUtil.bindBlockTextureMap();
    }

    @Override
    public double getInterpU(double u) {
        return sprite.getU(u * 16);
    }

    @Override
    public double getInterpV(double v) {
        return sprite.getV(v * 16);
    }
}
