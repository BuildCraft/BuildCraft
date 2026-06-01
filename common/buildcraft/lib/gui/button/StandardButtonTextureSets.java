/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.button;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;

@Deprecated
@Environment(EnvType.CLIENT)
public enum StandardButtonTextureSets implements IButtonTextureSet {
    LARGE_BUTTON(0, 0, 20, 200),
    SMALL_BUTTON(0, 100, 15, 200),
    LEFT_BUTTON(204, 0, 16, 10),
    RIGHT_BUTTON(214, 0, 16, 10);

    public static final Identifier BUTTON_TEXTURES = new Identifier("buildcraftcore:textures/gui/buttons.png");
    private final int x, y, height, width;

    StandardButtonTextureSets(int x, int y, int height, int width) {
        this.setX(x);
        this.setY(y);
        this.height = height;
        this.width = width;
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getHeight() { return height; }
    @Override public int getWidth() { return width; }
    @Override public Identifier getTexture() { return BUTTON_TEXTURES; }
}
