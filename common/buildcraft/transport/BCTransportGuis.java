/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public enum BCTransportGuis {
    FILTERED_BUFFER,
    PIPE_DIAMOND,
    PIPE_DIAMOND_WOOD,
    GATE;

    public void openGui(EntityPlayer player) {
        openGui(player, 0, -1, 0);
    }

    public void openGui(EntityPlayer player, BlockPos pos) {
        openGui(player, pos.getX(), pos.getY(), pos.getZ());
    }

    public void openGui(EntityPlayer player, int x, int y, int z) {
        player.openGui(BCTransport.INSTANCE, ordinal(), player.getEntityWorld(), x, y, z);
    }
}
