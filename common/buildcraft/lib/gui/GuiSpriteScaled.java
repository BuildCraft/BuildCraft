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

// STUB(R.Chen): GlStateManager render — Phase 5.
@Environment(EnvType.CLIENT)
public class GuiSpriteScaled implements ISimpleDrawable {
    public GuiSpriteScaled(ISprite sprite, int u, int v, int w, int h, int textureSize) {}
    @Override public void drawAt(double x, double y) {}
}
