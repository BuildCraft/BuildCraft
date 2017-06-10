/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.item.ItemBC_Neptune;

import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ItemVolumeBox extends ItemBC_Neptune {
    public ItemVolumeBox(String id) {
        super(id);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        BlockPos offset = pos.offset(facing);

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        VolumeBox current = volumeBoxes.getBoxAt(offset);

        if (current == null) {
            volumeBoxes.addBox(offset);
            volumeBoxes.markDirty();
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }
}
