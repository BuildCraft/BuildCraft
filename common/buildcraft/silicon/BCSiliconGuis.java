/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.silicon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public enum BCSiliconGuis {
    ASSEMBLY_TABLE,
    ADVANCED_CRAFTING_TABLE,
    INTEGRATION_TABLE,
    GATE;

    public void openGUI(EntityPlayer player) {
        player.openGui(BCSilicon.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
    }

    public void openGUI(EntityPlayer player, BlockPos pos) {
        openGui(player, pos, 0);
    }

    public void openGui(EntityPlayer player, BlockPos pos, int data) {
        int fullId = (data << 8) | ordinal();
        player.openGui(BCSilicon.INSTANCE, fullId, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
