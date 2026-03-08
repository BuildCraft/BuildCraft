/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.misc.ProfilerUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class GuidePageStandInRecipes extends GuidePage {
    public GuidePageStandInRecipes(GuiGuide gui, List<GuidePart> parts, ItemStack stack) {
        super(gui, parts, new PageValue<>(PageEntryItemStack.INSTANCE, new ItemStackValueFilter(stack)));
    }

    @Nonnull
    public static GuidePageFactory createFactory(@Nonnull ItemStack stack) {
//        List<GuidePartFactory> factories = XmlPageLoader.loadAllCrafting(stack, new Profiler(), 0);
        List<GuidePartFactory> factories = XmlPageLoader.loadAllCrafting(stack, ProfilerUtil.newProfiler(), 0);
        if (factories.isEmpty()) {
            return (gui) ->
            {
//                return new GuidePageStandInRecipes(gui, ImmutableList.of(new GuideText(gui, "No recipes!")), stack);
                return new GuidePageStandInRecipes(gui, ImmutableList.of(new GuideText(gui, "No recipes!", ITextComponent.nullToEmpty("No recipes!"))), stack);
            };
        }
        return (gui) ->
        {
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

    @Override
    public GuidePageBase createReloaded() {
        // Recipes won't have reloaded, so there's no need to change what is displayed.
        return this;
    }
}
