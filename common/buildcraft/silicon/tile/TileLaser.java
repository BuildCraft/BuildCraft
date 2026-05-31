/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.ILocalBlockUpdateSubscriber;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.misc.LocaleUtil;
// STUB(R.Chen): MessageUtil.writeBlockPos/readBlockPos not in migrated MessageUtil — inlined.
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

// STUB(R.Chen): BCSiliconBlocks import removed — referenced only in stub comment.

/**
 * Tile entity for the BuildCraft Laser block.
 *
 * STUB(R.Chen): VolumeUtil.iterateCone() not in libLeaf — target position search replaced with a
 * no-op stub; targets will never be found until VolumeUtil is migrated (Phase 4E).
 * STUB(R.Chen): MjCapabilityHelper/MjBatteryReceiver capability registration deferred.
 * STUB(R.Chen): LocalBlockUpdateNotifier world event listener stub — subscribes but no block
 * updates propagated until a mixin/Fabric event wires notifySubscribersInRange().
 * STUB(R.Chen): IDetachedRenderer/AdvDebuggerLaser, getRenderBoundingBox deferred to client phase.
 */
public class TileLaser extends TileBC_Neptune implements IDebuggable, ILocalBlockUpdateSubscriber {
    private static final int TARGETING_RANGE = 6;

    private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5, 10);
    private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10, 20);

    private final List<BlockPos> targetPositions = new ArrayList<>();
    private BlockPos targetPos;
    public Vec3d laserPos;
    private boolean worldHasUpdated = true;

    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;

    public TileLaser(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        battery = new MjBattery(1024 * MjAPI.MJ);
        // STUB(R.Chen): MjCapabilityHelper capability registration deferred until Fabric capability API lands.
        // caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    public static <T extends TileLaser> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    @Override
    public int getUpdateRange() {
        return TARGETING_RANGE;
    }

    @Override
    public BlockPos getSubscriberPos() {
        return getPos();
    }

    @Override
    public void setWorldUpdated(World world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags) {
        this.worldHasUpdated = true;
    }

    private void findPossibleTargets() {
        targetPositions.clear();
        // STUB(R.Chen): VolumeUtil.iterateCone() not in libLeaf — target scanning disabled.
        // When VolumeUtil is migrated, restore:
        //   BlockState state = world.getBlockState(pos);
        //   if (state.getBlock() != BCSiliconBlocks.laser) return;
        //   Direction face = state.get(BuildCraftProperties.BLOCK_FACING_6);
        //   VolumeUtil.iterateCone(world, pos, face, TARGETING_RANGE, true, (w, s, p, visible) -> { ... });
    }

    private void randomlyChooseTargetPos() {
        List<BlockPos> targetsNeedingPower = new ArrayList<>();
        for (BlockPos position : targetPositions) {
            if (isPowerNeededAt(position)) {
                targetsNeedingPower.add(position);
            }
        }
        if (targetsNeedingPower.isEmpty()) {
            targetPos = null;
            return;
        }
        targetPos = targetsNeedingPower.get(world.random.nextInt(targetsNeedingPower.size()));
    }

    private boolean isPowerNeededAt(BlockPos position) {
        if (position != null) {
            if (world.getBlockEntity(position) instanceof ILaserTarget target) {
                return target.getRequiredLaserPower() > 0;
            }
        }
        return false;
    }

    private ILaserTarget getTarget() {
        if (targetPos != null) {
            if (world.getBlockEntity(targetPos) instanceof ILaserTarget target) {
                return target;
            }
        }
        return null;
    }

    private void updateLaser() {
        if (targetPos != null) {
            laserPos = new Vec3d(targetPos.getX(), targetPos.getY(), targetPos.getZ())
                .add(
                    (5 + world.random.nextInt(6) + 0.5) / 16D,
                    9 / 16D,
                    (5 + world.random.nextInt(6) + 0.5) / 16D
                );
        } else {
            laserPos = null;
        }
    }

    public long getAverageClient() {
        return averageClient;
    }

    public long getMaxPowerPerTick() {
        return 4 * MjAPI.MJ;
    }

    public void tick() {
        if (world.isClient) {
            // set laser render position on client side
            if (clientLaserMoveInterval.markTimeIfDelay(world) || targetPos == null) {
                updateLaser();
            }
            return;
        }

        // set target tile on server side
        avgPower.tick();

        BlockPos previousTargetPos = targetPos;
        if (worldHasUpdated) {
            findPossibleTargets();
            worldHasUpdated = false;
        }

        if (!isPowerNeededAt(targetPos)) {
            targetPos = null;
        }

        if (serverTargetMoveInterval.markTimeIfDelay(world) || !isPowerNeededAt(targetPos)) {
            randomlyChooseTargetPos();
        }

        ILaserTarget target = getTarget();
        if (target != null) {
            long max = getMaxPowerPerTick();
            max *= battery.getStored() + max;
            max /= battery.getCapacity() / 2;
            max = Math.min(Math.min(max, getMaxPowerPerTick()), target.getRequiredLaserPower());
            long power = battery.extractPower(0, max);
            long excess = target.receiveLaserPower(power);
            if (excess > 0) {
                battery.addPowerChecking(excess, false);
            }
            avgPower.push(power - excess);
        } else {
            avgPower.clear();
        }

        if (!Objects.equals(previousTargetPos, targetPos) || true) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }

        markChunkDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // MjBattery.writeToNbt returns a NbtCompound (migrated API)
        nbt.put("battery", battery.writeToNbt());
        if (laserPos != null) {
            nbt.put("laser_pos", NBTUtilBC.writeVec3d(laserPos));
        }
        if (targetPos != null) {
            nbt.put("target_pos", NBTUtilBC.writeBlockPos(targetPos));
        }
        avgPower.writeToNbt(nbt, "average_power");
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        // TODO(R.Chen): remove legacy key in next version
        if (nbt.contains("mj_battery")) {
            nbt.put("battery", nbt.get("mj_battery"));
        }
        battery.readFromNbt(nbt.getCompound("battery"));
        targetPos = NBTUtilBC.readBlockPos(nbt.get("target_pos"));
        laserPos = NBTUtilBC.readVec3d(nbt.get("laser_pos"));
        avgPower.readFromNbt(nbt, "average_power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == TileBC_Neptune.NetEnvType.SERVER) {
            if (id == NET_RENDER_DATA) {
                battery.writeToBuffer(buffer);
                buffer.writeBoolean(targetPos != null);
                if (targetPos != null) {
                    // STUB(R.Chen): inline writeBlockPos (MessageUtil version not migrated)
                    buffer.writeInt(targetPos.getX());
                    buffer.writeInt(targetPos.getY());
                    buffer.writeInt(targetPos.getZ());
                }
                buffer.writeLong((long) avgPower.getAverage());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == TileBC_Neptune.NetEnvType.CLIENT) {
            if (id == NET_RENDER_DATA) {
                battery.readFromBuffer(buffer);
                if (buffer.readBoolean()) {
                    // STUB(R.Chen): inline readBlockPos (MessageUtil version not migrated)
                    targetPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
                } else {
                    targetPos = null;
                }
                averageClient = buffer.readLong();
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("target = " + targetPos);
        left.add("laser = " + laserPos);
        left.add("average = " + LocaleUtil.localizeMjFlow(averageClient == 0 ? (long) avgPower.getAverage() : averageClient));
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        if (world != null && !world.isClient) {
            LocalBlockUpdateNotifier.instance(world).registerSubscriberForUpdateNotifications(this);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (world != null && !world.isClient) {
            LocalBlockUpdateNotifier.instance(world).removeSubscriberFromUpdateNotifications(this);
        }
    }

    /** STUB(R.Chen): getRenderBoundingBox not a standard Fabric BlockEntity method — kept as
     * environment-annotated helper for any render code that calls it explicitly. */
    @Nonnull
    @Environment(EnvType.CLIENT)
    public net.minecraft.util.math.Box getRenderBoundingBox() {
        // STUB(R.Chen): lib.misc.data.Box.extendToEncompass deferred. Return unit box around tile.
        return new net.minecraft.util.math.Box(
            getPos().getX(), getPos().getY(), getPos().getZ(),
            getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1
        );
    }

    // STUB(R.Chen): getDebugRenderer() / IDetachedRenderer / AdvDebuggerLaser deferred to client render phase.
}
