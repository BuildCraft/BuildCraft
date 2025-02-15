/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.item;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.core.list.ContainerList;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ItemAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

//public class ItemFillerPlanner extends ItemAddon
public class ItemFillerPlanner extends ItemAddon implements MenuProvider {
    public ItemFillerPlanner(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    @Override
    public Addon createAddon() {
        return new AddonFillerPlanner();
    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        return Component.literal("filler_planner");
    }

    @Nullable
    @Override
    public ContainerList createMenu(int id, Inventory inv, Player player) {
        return new ContainerList(BCBuildersMenuTypes.FILLER, id, player);
    }
}
