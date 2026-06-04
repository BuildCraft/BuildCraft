// STUB(R.Chen): Forge IWorldEventListener — compile shim. Replaced by WorldEventCallbacks in 1.20.
package net.minecraft.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

public interface IWorldEventListener {
    default void onEntityAdded(Entity entity) {}
    default void onEntityRemoved(Entity entity) {}
    default void notifyBlockUpdate(World world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {}
    default void notifyLightSet(BlockPos pos) {}
    default void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}
    default void playSoundToAllNearExcept(net.minecraft.entity.player.PlayerEntity player, SoundEvent event, SoundCategory category, double x, double y, double z, float volume, float pitch) {}
    default void playRecord(SoundEvent event, BlockPos pos) {}
    default void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}
    default void onFireworkExplosion(Entity entity, int type) {}
    default void broadcastSound(int effectID, BlockPos pos, int data) {}
    default void playEvent(net.minecraft.entity.player.PlayerEntity player, int type, BlockPos pos, int data) {}
    default void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
}
