/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldEventListenerAdapter implements IWorldEventListener {
    @Override
    public void notifyBlockUpdate(@Nonnull World world,
                                  @Nonnull BlockPos pos,
                                  @Nonnull IBlockState oldState,
                                  @Nonnull IBlockState newState,
                                  int flags) {
    }

    @Override
    public void notifyLightSet(@Nonnull BlockPos pos) {
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player,
                                         @Nonnull SoundEvent sound,
                                         @Nonnull SoundCategory category,
                                         double x,
                                         double y,
                                         double z,
                                         float volume,
                                         float pitch) {
    }

    @Override
    public void playRecord(@Nonnull SoundEvent sound, @Nonnull BlockPos pos) {
    }

    @Override
    public void spawnParticle(int particleID,
                              boolean ignoreRange,
                              double xCoord,
                              double yCoord,
                              double zCoord,
                              double xSpeed,
                              double ySpeed,
                              double zSpeed,
                              @Nonnull int... parameters) {
    }

    @Override
    public void spawnParticle(int id,
                              boolean ignoreRange,
                              boolean minParticles,
                              double x,
                              double y,
                              double z,
                              double xSpeed,
                              double ySpeed,
                              double zSpeed,
                              @Nonnull int... parameters) {
    }

    @Override
    public void onEntityAdded(@Nonnull Entity entity) {
    }

    @Override
    public void onEntityRemoved(@Nonnull Entity entity) {
    }

    @Override
    public void broadcastSound(int soundID, @Nonnull BlockPos pos, int data) {
    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int type, @Nonnull BlockPos blockPos, int data) {
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress) {
    }
}
