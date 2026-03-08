/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import net.minecraft.resources.ResourceLocation;

@Deprecated(forRemoval = true)
public class ButtonTextureSet implements IButtonTextureSet {
    private final ResourceLocation texture;
    private final int x, y, height, width;

    public ButtonTextureSet(int x, int y, int height, int width) {
        this(x, y, height, width, StandardButtonTextureSets.BUTTON_TEXTURES);
    }

    public ButtonTextureSet(int x, int y, int height, int width, ResourceLocation texture) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.texture = texture;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }
}
