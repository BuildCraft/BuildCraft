/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.loader.XmlPageLoader;

public class GuidePageStandInRecipes extends GuidePage {
    public GuidePageStandInRecipes(GuiGuide gui, List<GuidePart> parts, ItemStack stack) {
        super(gui, parts, stack.getDisplayName());
    }

    @Nonnull
    public static GuidePageFactory createFactory(@Nonnull ItemStack stack) {
        List<GuidePartFactory> factories = XmlPageLoader.loadAllCrafting(stack);
        if (factories.isEmpty()) {
            return (gui) -> {
                return new GuidePageStandInRecipes(gui, ImmutableList.of(new GuideText(gui, "No recipes!")), stack);
            };
        }
        return (gui) -> {
            List<GuidePart> parts = new ArrayList<>();
            for (GuidePartFactory factory : factories) {
                parts.add(factory.createNew(gui));
            }
            return new GuidePageStandInRecipes(gui, parts, stack);
        };
    }

    @Override
    public boolean shouldPersistHistory() {
        return false;
    }
}
