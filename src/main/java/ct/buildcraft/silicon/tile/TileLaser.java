/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.SafeTimeTracker;
import ct.buildcraft.api.mj.ILaserTarget;
import ct.buildcraft.api.mj.ILaserTargetBlock;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjBattery;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.VolumeUtil;
import ct.buildcraft.lib.misc.data.AverageLong;
import ct.buildcraft.lib.misc.data.Box;
import ct.buildcraft.lib.mj.MjBatteryReceiver;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.client.render.AdvDebuggerLaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class TileLaser extends TileBC_Neptune implements IDebuggable, GameEventListener {
    private static final int TARGETING_RANGE = 6;

    private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5, 10);
    private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10, 20);

    private final List<BlockPos> targetPositions = new ArrayList<>();
    private BlockPos targetPos;
    public Vec3 laserPos;
    private boolean levelHasUpdated = true;

    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;

    public TileLaser(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.LASER_TILE.get(), pos, state);
        battery = new MjBattery(1024 * MjAPI.MJ);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    @Override
    public int getListenerRadius() {
        return TARGETING_RANGE;
    }

    @Override
    public PositionSource getListenerSource() {
        return new BlockPositionSource(this.worldPosition);
    }

    @Override
    public boolean handleGameEvent(ServerLevel level, GameEvent.Message msg) {
    	GameEvent gameEvent = msg.gameEvent();
    	if(gameEvent == (GameEvent.BLOCK_PLACE)||gameEvent == (GameEvent.BLOCK_DESTROY)||gameEvent == (GameEvent.BLOCK_CHANGE)) {
    		this.levelHasUpdated = true;
    		return true;
    	}
        return false;
    }

    private void findPossibleTargets() {
        targetPositions.clear();
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() != BCSiliconBlocks.LASER_BLOCK.get()) {
            return;
        }
        Direction face = state.getValue(BuildCraftProperties.BLOCK_FACING_6);

        VolumeUtil.iterateCone(level, worldPosition, face, TARGETING_RANGE, true, (w, s, p, visible) -> {
            if (!visible) {
                return;
            }
            BlockState stateAt = level.getBlockState(p);
            if (stateAt.getBlock() instanceof ILaserTargetBlock) {
                BlockEntity tileAt = level.getBlockEntity(p);
                if (tileAt instanceof ILaserTarget) {
                    targetPositions.add(p);

                }
            }
        });
    }

    private void randomlyChooseTargetPos() {
        List<BlockPos> targetsNeedingPower = new ArrayList<>();
        for(BlockPos position: targetPositions) {
            if (isPowerNeededAt(position)) {
                targetsNeedingPower.add(position);
            }
        }
        if (targetsNeedingPower.isEmpty()) {
            targetPos = null;
            return;
        }
        targetPos = targetsNeedingPower.get(level.getRandom().nextInt(targetsNeedingPower.size()));
    }

    private boolean isPowerNeededAt(BlockPos position) {
        if (position != null) {
            BlockEntity tile = level.getBlockEntity(position);
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
            laserPos = Vec3.atLowerCornerOf(targetPos)
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

    public void update() {
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
        if (levelHasUpdated) {
            findPossibleTargets();
            levelHasUpdated = false;
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
                battery.addPowerChecking(excess, FluidAction.EXECUTE);
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
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
        nbt.put("battery", battery.serializeNBT());
        if (laserPos != null) {
            nbt.put("laser_pos", NBTUtilBC.writeVec3(laserPos));
        }
        if (targetPos != null) {
            nbt.putIntArray("target_pos", NBTUtilBC.writeBlockPos(targetPos));
        }
        avgPower.writeToNbt(nbt, "average_power");
	}
	
    @Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
        // TODO: remove in next version
        if (nbt.contains("mj_battery")) {
            nbt.put("battery", nbt.get("mj_battery"));
        }
        battery.deserializeNBT(nbt.getCompound("battery"));
        targetPos = NBTUtilBC.readBlockPos(nbt.get("target_pos"));
        laserPos = NBTUtilBC.readVec3(nbt.get("laser_pos"));
        avgPower.readFromNbt(nbt, "average_power");
	}

	@Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
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
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
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
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("target = " + targetPos);
        left.add("laser = " + laserPos);
        left.add("average = " + LocaleUtil.localizeMjFlow(averageClient == 0 ? (long) avgPower.getAverage() : averageClient));
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return new Box(this).extendToEncompass(targetPos).getBoundingBox();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerLaser(this);
    }
}
