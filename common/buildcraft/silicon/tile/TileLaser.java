/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.*;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.lib.block.ILocalBlockUpdateSubscriber;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VolumeUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.client.render.AdvDebuggerLaser;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileLaser extends TileBC_Neptune implements ITickable, IDebuggable, ILocalBlockUpdateSubscriber {
    private static final int TARGETING_RANGE = 6;

    private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5, 10);
    private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10, 20);

    private final List<BlockPos> targetPositions = new ArrayList<>();
    private BlockPos targetPos;
    public Vector3d laserPos;
    private boolean worldHasUpdated = true;

    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;

    public TileLaser() {
        super(BCSiliconBlocks.laserTile.get());
        battery = new MjBattery(1024 * MjAPI.MJ);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    @Override
    public int getUpdateRange() {
        return TARGETING_RANGE;
    }

    @Override
    public BlockPos getSubscriberPos() {
        return getBlockPos();
    }

    @Override
//    public void setWorldUpdated(World world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags)
    public void setWorldUpdated(IWorld world, BlockPos eventPos) {
        this.worldHasUpdated = true;
    }

    private void findPossibleTargets() {
        targetPositions.clear();
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() != BCSiliconBlocks.laser.get()) {
            return;
        }
        Direction face = state.getValue(BuildCraftProperties.BLOCK_FACING_6);

        VolumeUtil.iterateCone(level, worldPosition, face, TARGETING_RANGE, true, (w, s, p, visible) ->
        {
            if (!visible) {
                return;
            }
            BlockState stateAt = level.getBlockState(p);
            if (stateAt.getBlock() instanceof ILaserTargetBlock) {
                TileEntity tileAt = level.getBlockEntity(p);
                if (tileAt instanceof ILaserTarget) {
                    targetPositions.add(p);

                }
            }
        });
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
        targetPos = targetsNeedingPower.get(level.random.nextInt(targetsNeedingPower.size()));
    }

    private boolean isPowerNeededAt(BlockPos position) {
        if (position != null) {
            TileEntity tile = level.getBlockEntity(position);
            if (tile instanceof ILaserTarget) {
                ILaserTarget target = (ILaserTarget) tile;
                return target.getRequiredLaserPower() > 0;
            }
        }
        return false;
    }

    private ILaserTarget getTarget() {
        if (targetPos != null) {
            if (level.getBlockEntity(targetPos) instanceof ILaserTarget) {
                return (ILaserTarget) level.getBlockEntity(targetPos);
            }
        }
        return null;
    }

    private void updateLaser() {
        if (targetPos != null) {
            laserPos = Vector3d.atLowerCornerOf(targetPos)
                    .add(
                            (5 + level.random.nextInt(6) + 0.5) / 16D,
                            9 / 16D,
                            (5 + level.random.nextInt(6) + 0.5) / 16D
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

    @Override
    public void update() {
        ITickable.super.update();
        if (level.isClientSide) {
            // set laser render position on client side
            if (clientLaserMoveInterval.markTimeIfDelay(level) || targetPos == null) {
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

        if (serverTargetMoveInterval.markTimeIfDelay(level) || !isPowerNeededAt(targetPos)) {
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
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.put("battery", battery.serializeNBT());
        if (laserPos != null) {
            nbt.put("laser_pos", NBTUtilBC.writeVec3d(laserPos));
        }
        if (targetPos != null) {
            nbt.put("target_pos", NBTUtilBC.writeBlockPos(targetPos));
        }
        avgPower.writeToNbt(nbt, "average_power");
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        battery.deserializeNBT(nbt.getCompound("battery"));
        targetPos = NBTUtilBC.readBlockPos(nbt.get("target_pos"));
        laserPos = NBTUtilBC.readVec3d(nbt.get("laser_pos"));
        avgPower.readFromNbt(nbt, "average_power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                battery.writeToBuffer(buffer);
                buffer.writeBoolean(targetPos != null);
                if (targetPos != null) {
                    MessageUtil.writeBlockPos(buffer, targetPos);
                }
                buffer.writeLong((long) avgPower.getAverage());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                battery.readFromBuffer(buffer);
                if (buffer.readBoolean()) {
                    targetPos = MessageUtil.readBlockPos(buffer);
                } else {
                    targetPos = null;
                }
                averageClient = buffer.readLong();
            }
        }
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        left.add("battery = " + battery.getDebugString());
//        left.add("target = " + targetPos);
//        left.add("laser = " + laserPos);
//        left.add("average = " + LocaleUtil.localizeMjFlow(averageClient == 0 ? (long) avgPower.getAverage() : averageClient));
        left.add(new StringTextComponent("battery = " + battery.getDebugString()));
        left.add(new StringTextComponent("target = " + targetPos));
        left.add(new StringTextComponent("laser = " + laserPos));
        left.add(new StringTextComponent("average = ").append(LocaleUtil.localizeMjFlowComponent(averageClient == 0 ? (long) avgPower.getAverage() : averageClient)));
    }

    @Override
//    public void validate()
    public void clearRemoved() {
//        super.validate();
        super.clearRemoved();
        if (!level.isClientSide) {
            LocalBlockUpdateNotifier.instance(level).registerSubscriberForUpdateNotifications(this);
        }
    }

    @Override
//    public void invalidate()
    public void setRemoved() {
//        super.invalidate();
        super.setRemoved();
        if (!level.isClientSide) {
            LocalBlockUpdateNotifier.instance(level).removeSubscriberFromUpdateNotifications(this);
        }
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new Box(this).extendToEncompass(targetPos).getBoundingBox();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerLaser(this);
    }
}
