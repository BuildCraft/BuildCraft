/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IFakePlayerProvider;

import buildcraft.lib.fake.FakePlayerBC;

public enum FakePlayerProvider implements IFakePlayerProvider {
    INSTANCE;

    /** The default {@link GameProfile} to use if a tile entity cannot determine its real owner. Most of the time this
     * shouldn't be necessary, as we should be able to get a {@link GameProfile} from all {@link EntityPlayer}'s that
     * place or create tiles/robots */
    @Deprecated
    public static final GameProfile NULL_PROFILE;

    static {
        UUID id = UUID.nameUUIDFromBytes("buildcraft.core".getBytes(StandardCharsets.UTF_8));
        NULL_PROFILE = new GameProfile(id, "[BuildCraft]");
    }

    private final Map<GameProfile, FakePlayerBC> players = new HashMap<>();

    @Override
    @Deprecated
    public FakePlayerBC getBuildCraftPlayer(WorldServer world) {
        return getFakePlayer(world, NULL_PROFILE, BlockPos.ORIGIN);
    }

    @Override
    public FakePlayerBC getFakePlayer(WorldServer world, GameProfile profile) {
        return getFakePlayer(world, profile, BlockPos.ORIGIN);
    }

    @Override
    public FakePlayerBC getFakePlayer(WorldServer world, GameProfile profile, BlockPos pos) {
        if (profile == null) {
            BCLog.logger.warn("[lib.fake] Null GameProfile! This is a bug!", new IllegalArgumentException());
            profile = NULL_PROFILE;
        }
        FakePlayerBC player = players.computeIfAbsent(profile, p -> new FakePlayerBC(world, p));
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
