/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

// STUB(R.Chen): PipeBehaviourWood — Forge MjCapabilityHelper (removes with cap layer), FluidStack
// (Forge fluid, Phase 4E fluid). getCapability → stub null. extractFluid → stub null.

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeFaceTex;

// BCTransportConfig inlined below — BCTransportConfig has Forge deps and is not yet in libLeaf.

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver, IDebuggable {

    private static final PipeFaceTex TEX_CLEAR = PipeFaceTex.get(0);
    private static final PipeFaceTex TEX_FILLED = PipeFaceTex.get(1);

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public PipeFaceTex getTextureData(Direction face) {
        return (face != null && face == getCurrentDir()) ? TEX_FILLED : TEX_CLEAR;
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

    // BCTransportConfig constants inlined (BCTransportConfig has Forge deps, not in libLeaf).
    private static final long MJ_PER_ITEM        = MjAPI.MJ;      // BCTransportConfig.mjPerItem default
    private static final long MJ_PER_MILLIBUCKET = 1_000L;        // BCTransportConfig.mjPerMillibucket default

    protected long extract(long power, boolean simulate) {
        if (power > 0) {
            if (pipe.getFlow() instanceof IFlowItems) {
                IFlowItems flow = (IFlowItems) pipe.getFlow();
                int maxItems = (int) (power / MJ_PER_ITEM);
                if (maxItems > 0) {
                    int extracted = extractItems(flow, getCurrentDir(), maxItems, simulate);
                    if (extracted > 0) {
                        return power - extracted * MJ_PER_ITEM;
                    }
                }
            } else if (pipe.getFlow() instanceof IFlowFluid) {
                // STUB(R.Chen): extractFluid deferred — FluidStack (Forge) not migrated (Phase 4E).
            }
        }
        return power;
    }

    protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
        // StackFilter.ALL inlined — StackFilter (Forge TileEntityFurnace dep) not yet in libLeaf.
        return flow.tryExtractItems(count, dir, null, stack -> true, simulate);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        final long power = 512 * MjAPI.MJ;
        return power - extract(power, true);
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return extract(microJoules, simulate);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Facing = " + currentDir);
    }
}
