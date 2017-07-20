/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.*;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.transport.BCTransportConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver, IDebuggable {

    private final MjBattery mjBattery = new MjBattery(2 * MjAPI.MJ);
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        mjBattery.deserializeNBT(nbt.getCompoundTag("mjBattery"));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("mjBattery", mjBattery.serializeNBT());
        return nbt;
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return (face != null && face == getCurrentDir()) ? 1 : 0;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    protected boolean canFaceDirection(EnumFacing dir) {
        return pipe.getConnectedType(dir) == ConnectedType.TILE;
    }

    @PipeEventHandler
    public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
        if (currentDir.face != null) {
            sideCheck.disallow(currentDir.face);
        }
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }

        if (currentDir == EnumPipePart.CENTER) {
            advanceFacing();
        } else if (!canFaceDirection(getCurrentDir())) {
            currentDir = EnumPipePart.CENTER;
        }

        mjBattery.tick(pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos());
        long potential = mjBattery.getStored();
        if (potential > 0) {
            if (pipe.getFlow() instanceof IFlowItems) {
                IFlowItems flow = (IFlowItems) pipe.getFlow();
                int maxItems = (int) (potential / BCTransportConfig.mjPerItem);
                if (maxItems > 0) {
                    int extracted = extractItems(flow, getCurrentDir(), maxItems);
                    if (extracted > 0) {
                        mjBattery.extractPower(extracted * BCTransportConfig.mjPerItem);
                        return;
                    }
                }
            } else if (pipe.getFlow() instanceof IFlowFluid) {
                IFlowFluid flow = (IFlowFluid) pipe.getFlow();
                int maxMillibuckets = (int) (potential / BCTransportConfig.mjPerMillibucket);
                if (maxMillibuckets > 0) {
                    FluidStack extracted = extractFluid(flow, getCurrentDir(), maxMillibuckets);
                    if (extracted != null && extracted.amount > 0) {
                        mjBattery.extractPower(extracted.amount * BCTransportConfig.mjPerMillibucket);
                        return;
                    }
                }
            }
            mjBattery.extractPower(0, 5 * MjAPI.MJ / 100);
        }
    }

    protected int extractItems(IFlowItems flow, EnumFacing dir, int count) {
        return flow.tryExtractItems(count, dir, null, StackFilter.ALL);
    }

    @Nullable
    protected FluidStack extractFluid(IFlowFluid flow, EnumFacing dir, int millibuckets) {
        return flow.tryExtractFluid(millibuckets, dir, null);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return mjBattery.addPowerChecking(microJoules, simulate);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return mjCaps.getCapability(capability, facing);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("Power = " + mjBattery.getDebugString());
        left.add("Facing = " + currentDir);
    }
}
