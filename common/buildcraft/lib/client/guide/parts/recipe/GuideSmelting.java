/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): GuideSmelting deferred
package buildcraft.lib.client.guide.parts.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;

@Environment(EnvType.CLIENT)
public class GuideSmelting extends GuidePart {

    public GuideSmelting(GuiGuide gui, ItemStack input, ItemStack output) {
        super(gui);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
            int mouseX, int mouseY, int mouseButton) {
        return null;
    }
}
