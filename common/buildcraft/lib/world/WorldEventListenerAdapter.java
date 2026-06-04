/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class WorldEventListenerAdapter implements IWorldEventListener {
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void notifyBlockUpdate(@Nonnull World world,
                                  @Nonnull BlockPos pos,
                                  @Nonnull BlockState oldState,
                                  @Nonnull BlockState newState,
                                  int flags) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void notifyLightSet(@Nonnull BlockPos pos) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void playSoundToAllNearExcept(@Nullable PlayerEntity player,
                                         @Nonnull SoundEvent sound,
                                         @Nonnull SoundCategory category,
                                         double x,
                                         double y,
                                         double z,
                                         float volume,
                                         float pitch) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void playRecord(@Nonnull SoundEvent sound, @Nonnull BlockPos pos) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
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

    // @Override -- removed: method does not exist in Fabric 1.20.1
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

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void onEntityAdded(@Nonnull Entity entity) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void onEntityRemoved(@Nonnull Entity entity) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void broadcastSound(int soundID, @Nonnull BlockPos pos, int data) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void playEvent(@Nullable PlayerEntity player, int type, @Nonnull BlockPos blockPos, int data) {
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress) {
    }
}
