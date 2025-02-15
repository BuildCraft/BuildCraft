/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import net.minecraft.resources.ResourceLocation;

@Deprecated(forRemoval = true)
public enum StandardButtonTextureSets implements IButtonTextureSet {
    LARGE_BUTTON(0, 0, 20, 200),
    SMALL_BUTTON(0, 100, 15, 200),
    LEFT_BUTTON(204, 0, 16, 10),
    RIGHT_BUTTON(214, 0, 16, 10);

    public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("buildcraftcore:textures/gui/buttons.png");
    private final int x, y, height, width;

    StandardButtonTextureSets(int x, int y, int height, int width) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
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
        return BUTTON_TEXTURES;
    }
}
