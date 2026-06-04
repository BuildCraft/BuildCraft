/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import net.minecraft.client.texture.Sprite;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.misc.SpriteUtil;

public class SpriteAtlas implements ISprite {
    public final Sprite sprite;

    public SpriteAtlas(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void bindTexture() {
        SpriteUtil.bindBlockTextureMap();
    }

    @Override
    public double getInterpU(double u) {
        return sprite.getFrameU(u * 16);
    }

    @Override
    public double getInterpV(double v) {
        return sprite.getFrameV(v * 16);
    }
}
