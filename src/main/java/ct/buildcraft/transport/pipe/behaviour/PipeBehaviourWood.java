/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.api.transport.pipe.IFlowFluid;
import ct.buildcraft.api.transport.pipe.IFlowItems;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipe.ConnectedType;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeEventFluid;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeFaceTex;
import ct.buildcraft.lib.inventory.filter.StackFilter;
import ct.buildcraft.transport.BCTransportConfig;
import ct.buildcraft.transport.client.render.RenderPipeHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver, IDebuggable {

    private static final PipeFaceTex TEX_CLEAR = PipeFaceTex.get(0);
    private static final PipeFaceTex TEX_FILLED = PipeFaceTex.get(1);
	
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @Override
    public PipeFaceTex getTextureData(Direction face) {
        return (face != null && face == getCurrentDir()) ? TEX_FILLED : TEX_CLEAR;
    }
    
    @Override
    public int[] getTextureUVs(Direction face) {
        return (face != null && face == getCurrentDir()) ? RenderPipeHolder.UP_UV : RenderPipeHolder.DOWN_UV;
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    protected boolean canFaceDirection(Direction dir) {
        return dir != null && pipe.getConnectedType(dir) == ConnectedType.TILE;
    }

    @PipeEventHandler
    public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
        if (currentDir.face != null) {
            sideCheck.disallow(currentDir.face);
        }
    }

    protected long extract(long power, FluidAction simulate) {
        if (power > 0) {
            if (pipe.getFlow() instanceof IFlowItems) {
                IFlowItems flow = (IFlowItems) pipe.getFlow();
                int maxItems = (int) (power / BCTransportConfig.mjPerItem);
                if (maxItems > 0) {
                    int extracted = extractItems(flow, getCurrentDir(), maxItems, simulate);
                    if (extracted > 0) {
                        return power - extracted * BCTransportConfig.mjPerItem;
                    }
                }
            } else if (pipe.getFlow() instanceof IFlowFluid) {
                IFlowFluid flow = (IFlowFluid) pipe.getFlow();
                int maxMillibuckets = (int) (power / BCTransportConfig.mjPerMillibucket);
                if (maxMillibuckets > 0) {
                    FluidStack extracted = extractFluid(flow, getCurrentDir(), maxMillibuckets, simulate);
                    if (!extracted.isEmpty() && extracted.getAmount() > 0) {
                        return power - extracted.getAmount() * BCTransportConfig.mjPerMillibucket;
                    }
                }
            }
        }
        return power;
    }

    protected int extractItems(IFlowItems flow, Direction dir, int count, FluidAction simulate) {
        return flow.tryExtractItems(count, dir, null, StackFilter.ALL, simulate);
    }

    @Nullable
    protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, FluidAction simulate) {
        return flow.tryExtractFluid(millibuckets, dir, FluidStack.EMPTY, simulate);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        final long power = 512 * MjAPI.MJ;
        return power - extract(power, FluidAction.SIMULATE);
    }

    @Override
    public long receivePower(long microJoules, FluidAction simulate) {
        return extract(microJoules, simulate);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        return mjCaps.getCapability(capability, facing);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Facing = " + currentDir);
    }
    
}
