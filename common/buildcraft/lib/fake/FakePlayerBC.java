/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fake;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerBC extends FakePlayer {
    public FakePlayerBC(ServerLevel world, GameProfile name) {
        super(world, name);
    }

    @Override
    public void openTextEdit(SignBlockEntity p_277909_, boolean p_277495_) {
        // TODO: Put this in forge!
    }
}
