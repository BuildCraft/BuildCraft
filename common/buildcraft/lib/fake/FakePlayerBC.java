/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fake;

import com.mojang.authlib.GameProfile;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerBC extends FakePlayer {
    public FakePlayerBC(ServerWorld world, GameProfile name) {
        super(world, name);
    }

    @Override
    public void openTextEdit(SignTileEntity signTile) {
        // TODO: Put this in forge!
    }
}
