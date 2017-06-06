/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import buildcraft.api.core.IFakePlayerProvider;

import buildcraft.lib.fake.FakePlayerBC;

public enum FakePlayerProvider implements IFakePlayerProvider {
    INSTANCE;

    private final Map<GameProfile, FakePlayerBC> players = new HashMap<>();

    private final GameProfile gameProfile = new GameProfile(
        UUID.nameUUIDFromBytes("buildcraft.core".getBytes()),
        "[BuildCraft]"
    );

    @Override
    @Deprecated
    public FakePlayerBC getBuildCraftPlayer(WorldServer world) {
        return getFakePlayer(world, gameProfile, BlockPos.ORIGIN);
    }

    @Override
    public FakePlayerBC getFakePlayer(WorldServer world, GameProfile profile) {
        return getFakePlayer(world, profile, BlockPos.ORIGIN);
    }

    @Override
    public FakePlayerBC getFakePlayer(WorldServer world, GameProfile profile, BlockPos pos) {
        players.computeIfAbsent(profile, p -> new FakePlayerBC(world, profile));
        FakePlayerBC player = players.get(profile);
        player.world = world;
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return player;
    }

    public void unloadWorld(WorldServer world) {
        players.values().removeIf(entry -> entry.world == world);
    }
}
