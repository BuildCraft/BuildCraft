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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

//public class ItemFillerPlanner extends ItemAddon
public class ItemFillerPlanner extends ItemAddon implements INamedContainerProvider {
    public ItemFillerPlanner(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    @Override
    public Addon createAddon() {
        return new AddonFillerPlanner();
    }

    // INamedContainerProvider

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("filler_planner");
    }

    @Nullable
    @Override
    public ContainerList createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return new ContainerList(BCBuildersMenuTypes.FILLER, id, player);
    }
}
