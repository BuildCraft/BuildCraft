/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemVolumeBox extends ItemBC_Neptune {
    public ItemVolumeBox(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    public ActionResultType useOn(ItemUseContext ctx) {
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction facing = ctx.getClickedFace();
        if (world.isClientSide) {
//            return EnumActionResult.PASS;
            return ActionResultType.PASS;
        }

        BlockPos offset = pos.relative(facing);

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        VolumeBox current = volumeBoxes.getVolumeBoxAt(offset);

        if (current == null) {
            volumeBoxes.addVolumeBox(offset);
            volumeBoxes.setDirty();
//            return EnumActionResult.SUCCESS;
            return ActionResultType.SUCCESS;
        }

//        return EnumActionResult.FAIL;
        return ActionResultType.FAIL;
    }
}
