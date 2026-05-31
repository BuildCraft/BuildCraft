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

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.pos.GuiRectangle;

/** @deprecated Use {@link GuiButtonDrawable} instead. */
@Deprecated
@Environment(EnvType.CLIENT)
public class GuiImageButton extends GuiAbstractButton {
    private final Identifier texture;

    public GuiImageButton(BuildCraftGui gui, int id, int x, int y, int size, Identifier texture) {
        super(gui, "" + id, new GuiRectangle(x, y, size, size));
        this.texture = texture;
    }

    // TODO(R.Chen): drawBackground render — DrawContext.drawTexture once atlas/sprite layer is ready.
}
