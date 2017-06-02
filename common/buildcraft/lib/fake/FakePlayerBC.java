/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fake;

import com.mojang.authlib.GameProfile;

import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerBC extends FakePlayer {
    public FakePlayerBC(WorldServer world, GameProfile name) {
        super(world, name);
    }

    @Override
    public void openEditSign(TileEntitySign signTile) {
    }
}
