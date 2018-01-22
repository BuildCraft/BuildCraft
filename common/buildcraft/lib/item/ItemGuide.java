/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.lib.BCLib;

public class ItemGuide extends ItemBC_Neptune {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:guide");

    public ItemGuide(String id) {
        super(id);
        setContainerItem(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        player.openGui(BCLib.INSTANCE, 0, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
}
