/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.ILaserTargetBlock;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IDebuggable;

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
import buildcraft.lib.world.WorldEventListenerAdapter;

import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.client.render.AdvDebuggerLaser;

public class TileLaser extends TileBC_Neptune implements ITickable, IDebuggable {
    private List<BlockPos> possible;
    private BlockPos targetPos;
    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;

    public Vec3d laserPos;

    private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5, 10);
    private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10, 20);

    private final IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
        @Override
        public void notifyBlockUpdate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState,
                                      @Nonnull IBlockState newState, int flags) {
            findPossibleTargets();
        }
    };

    public TileLaser() {
        super();
        battery = new MjBattery(1024 * MjAPI.MJ);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    private void findPossibleTargets() {
        possible = new ArrayList<>();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != BCSiliconBlocks.laser) {
            return;
        }
        EnumFacing face = state.getValue(BuildCraftProperties.BLOCK_FACING_6);

        VolumeUtil.iterateCone(world, pos, face, 6, true, (w, s, p, visible) -> {
            if (!visible) {
                return;
            }
            IBlockState stateAt = world.getBlockState(p);
            if (stateAt.getBlock() instanceof ILaserTargetBlock) {
                TileEntity tileAt = world.getTileEntity(p);
                if (tileAt instanceof ILaserTarget) {
                    ILaserTarget targetAt = (ILaserTarget) tileAt;
                    if (targetAt.getRequiredLaserPower() > 0) {
                        possible.add(p);
                    }
                }
            }
        });
    }

    private void randomlyChooseTargetPos() {
        if (possible.isEmpty()) {
            targetPos = null;
            return;
        }
        targetPos = possible.get(world.rand.nextInt(possible.size()));
    }

    private ILaserTarget getTarget() {
        if (targetPos != null) {
            TileEntity tile = world.getTileEntity(targetPos);
            if (tile instanceof ILaserTarget) {
                ILaserTarget target = (ILaserTarget) tile;
                return target.getRequiredLaserPower() > 0 ? target : null;
            } else {
                possible.remove(targetPos);
                return null;
            }
        } else {
            return null;
        }
    }

    private void updateLaser() {
        if (targetPos != null) {
            laserPos = new Vec3d(targetPos)
                .addVector(
                    (5 + world.rand.nextInt(6) + 0.5) / 16D,
                    9 / 16D,
                    (5 + world.rand.nextInt(6) + 0.5) / 16D
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
        if (world.isRemote) {
            // set laser render position on client side
            if (clientLaserMoveInterval.markTimeIfDelay(world) || targetPos == null) {
                updateLaser();
            }
            return;
        }
        // set target tile on server side
        avgPower.tick();

        BlockPos previousTargetPos = targetPos;
        if (possible == null) {
            findPossibleTargets();
        }

        if (getTarget() == null) {
            targetPos = null;
        }

        if (serverTargetMoveInterval.markTimeIfDelay(world) || getTarget() == null) {
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

        if (!Objects.equals(previousTargetPos, targetPos)) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("battery", battery.serializeNBT());
        if (laserPos != null) {
            nbt.setTag("laser_pos", NBTUtilBC.writeVec3d(laserPos));
        }
        if (targetPos != null) {
            nbt.setTag("target_pos", NBTUtilBC.writeBlockPos(targetPos));
        }
        avgPower.writeToNbt(nbt, "average_power");
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        // TODO: remove in next version
        if (nbt.hasKey("mj_battery")) {
            nbt.setTag("battery", nbt.getTag("mj_battery"));
        }
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        targetPos = NBTUtilBC.readBlockPos(nbt.getTag("target_pos"));
        laserPos = NBTUtilBC.readVec3d(nbt.getTag("laser_pos"));
        avgPower.readFromNbt(nbt, "average_power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
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
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
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
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("battery = " + battery.getDebugString());
        left.add("target = " + targetPos);
        left.add("laser = " + laserPos);
        left.add("average = " + LocaleUtil.localizeMjFlow(averageClient == 0 ? (long) avgPower.getAverage() : averageClient));
    }

    @Override
    public void validate() {
        super.validate();
        if (!world.isRemote) {
            world.addEventListener(worldEventListener);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote) {
            world.removeEventListener(worldEventListener);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new Box(this).extendToEncompass(targetPos).getBoundingBox();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return AdvDebuggerLaser.getForTile(this);
    }
}
