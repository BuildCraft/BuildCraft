/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.pos.IGuiArea;

// TODO(R.Chen): GuiIcon draw — blocked by SpriteRaw migration (raw-texture sprite draws).
@Environment(EnvType.CLIENT)
public class GuiIcon implements ISimpleDrawable {
    public final ISprite sprite;
    public final int textureSize;

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
    }

    public static void draw(ISprite sprite, double x1, double y1, double x2, double y2) {
        // TODO(R.Chen): GL draw with Tessellator/BufferBuilder once SpriteRaw migrated.
    }

    @Override
    public void drawAt(double x, double y) {
        // TODO(R.Chen): Tessellator/BufferBuilder render pending SpriteRaw migration.
    }

    public void drawScaledInside(IGuiArea element) {
        drawAt(element.getX(), element.getY());
    }
}
