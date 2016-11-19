/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public enum BCCoreGuis {
    LIST;

    public void openGUI(EntityPlayer player) {
        player.openGui(BCCore.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
    }

    public void openGUI(EntityPlayer player, BlockPos pos) {
        player.openGui(BCCore.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
