/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuidePageStandInRecipes extends GuidePage {
    public GuidePageStandInRecipes(GuiGuide gui, List<GuidePart> parts, ItemStack stack) {
        super(gui, parts, stack.getDisplayName());
    }

    @Nonnull
    public static GuidePageFactory createFactory(@Nonnull ItemStack stack) {
        List<GuidePartFactory> factories = MarkdownPageLoader.loadAllCrafting(stack);
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
