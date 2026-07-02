/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.item;

import ct.buildcraft.core.marker.volume.VolumeBox;
import ct.buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemVolumeBox extends Item {
    public ItemVolumeBox(Properties pro) {
        super(pro);
    }
    
    @Override
	public InteractionResult useOn(UseOnContext ctx) {
    	Level world = ctx.getLevel();
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }

        BlockPos offset = ctx.getClickedPos().offset(ctx.getClickedFace().getNormal());

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        VolumeBox current = volumeBoxes.getVolumeBoxAt(offset);

        if (current == null) {
            volumeBoxes.addVolumeBox(offset);
            volumeBoxes.setDirty();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
	}


}
