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

import net.minecraft.util.Identifier;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.pos.IGuiArea;

// TODO(R.Chen): GuiIcon draw — blocked by SpriteRaw migration (raw-texture sprite draws).
@Environment(EnvType.CLIENT)
public class GuiIcon implements ISimpleDrawable {
    public final ISprite sprite;
    public final int textureSize;

    // Legacy constructor: (Identifier texture, int u, int v, int width, int height)
    // Used by unmigrated GUI code before SpriteRaw migration.
    public final int u, v, width, height;
    public final Identifier texture;

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
        this.texture = null;
        this.u = this.v = this.width = this.height = 0;
    }

    /** Forge-compat constructor: (texture, u, v, width, height). Used before SpriteRaw migration. */
    public GuiIcon(Identifier texture, int u, int v, int width, int height) {
        this.sprite = null;
        this.textureSize = 256;
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    public static void draw(ISprite sprite, double x1, double y1, double x2, double y2) {
        // TODO(R.Chen): GL draw with Tessellator/BufferBuilder once SpriteRaw migrated.
    }

    @Override
    public void drawAt(double x, double y) {
        // TODO(R.Chen): Tessellator/BufferBuilder render pending SpriteRaw migration.
    }

    public void drawAt(IGuiArea area) {
        drawAt(area.getX(), area.getY());
    }

    public void drawScaledInside(IGuiArea element) {
        drawAt(element.getX(), element.getY());
    }

    /** Returns a new GuiIcon with (u,v) offset by (du,dv) on the same texture. */
    public GuiIcon offset(int du, int dv) {
        if (texture != null) {
            return new GuiIcon(texture, u + du, v + dv, width, height);
        }
        return this; // sprite-based icons: no simple offset
    }

    /** Returns true if the mouse position is within this icon drawn at (x, y). */
    public boolean containsGuiPos(double x, double y, buildcraft.lib.gui.pos.IGuiPosition mouse) {
        if (mouse == null) return false;
        double mx = mouse.getX(), my = mouse.getY();
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }
}
