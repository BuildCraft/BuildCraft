/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;

@Environment(EnvType.CLIENT)
public abstract class GuiElementContainer2 implements IContainingElement {

    public final BuildCraftGui gui;
    private final List<IGuiElement> children = new ArrayList<>();

    public GuiElementContainer2(BuildCraftGui gui) {
        this.gui = gui;
    }

    @Override
    public List<IGuiElement> getChildElements() {
        return children;
    }
}
