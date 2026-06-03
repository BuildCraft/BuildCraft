/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): guide item part rendering deferred
package buildcraft.lib.client.guide.parts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;

@Environment(EnvType.CLIENT)
public abstract class GuidePartItem extends GuidePart {

    public final ItemStack stack;

    public GuidePartItem(GuiGuide gui, ItemStack stack) {
        super(gui);
        this.stack = stack;
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
