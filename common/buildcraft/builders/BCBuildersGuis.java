/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public enum BCBuildersGuis {
    ARCHITECT,
    BUILDER,
    FILLER,
    LIBRARY,
    REPLACER,
    FILLER_PLANNER;

    public void openGUI(PlayerEntity player) {
        player.openGui(BCBuilders.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
    }

    public void openGUI(PlayerEntity player, BlockPos pos) {
        player.openGui(BCBuilders.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
