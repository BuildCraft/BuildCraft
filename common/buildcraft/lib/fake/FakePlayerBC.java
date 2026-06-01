/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fake;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.entity.BlockEntitySign;
import net.minecraft.server.world.ServerWorld;

import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerBC extends FakePlayer {
    public FakePlayerBC(ServerWorld world, GameProfile name) {
        super(world, name);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void openEditSign(TileEntitySign signTile) {
        // TODO: Put this in forge!
    }
}
