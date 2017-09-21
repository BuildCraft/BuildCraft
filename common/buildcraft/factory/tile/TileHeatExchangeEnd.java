/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;

public class TileHeatExchangeEnd extends TileBC_Neptune implements IDebuggable, ITickable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("HeatExchangeEnd");
    public static final int NET_TANK_HEATABLE_OUT = IDS.allocId("TANK_HEATABLE_OUT");
    public static final int NET_TANK_COOLABLE_IN = IDS.allocId("TANK_COOLABLE_IN");

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // Package-private to allow TileHeatExchangeStart to access them
    final Tank tankHeatableOut = new Tank("heatableOut", 2 * Fluid.BUCKET_VOLUME, this);
    final Tank tankCoolableIn = new Tank("coolableIn", 2 * Fluid.BUCKET_VOLUME, this, this::isCoolant);

    public final FluidSmoother smoothedHeatableOut;
    public final FluidSmoother smoothedCoolableIn;

    public TileHeatExchangeEnd() {
        tankManager.addAll(tankHeatableOut, tankCoolableIn);
        tankHeatableOut.setCanFill(false);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankHeatableOut, EnumPipePart.UP);
        caps.addCapability(CapUtil.CAP_FLUIDS, this::getTankForSide, EnumPipePart.HORIZONTALS);

        smoothedHeatableOut = createFluidSmoother(tankHeatableOut, NET_TANK_HEATABLE_OUT);
        smoothedCoolableIn = createFluidSmoother(tankCoolableIn, NET_TANK_COOLABLE_IN);
    }

    private FluidSmoother createFluidSmoother(Tank tank, int netId) {
        return new FluidSmoother(w -> createAndSendMessage(netId, w), tank);
    }

    private boolean isCoolant(FluidStack fluid) {
        return BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getRecipeForInput(fluid) != null;
    }

    private IFluidHandler getTankForSide(EnumFacing side) {
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeEnd);
        if (state == null) {
            return null;
        }
        EnumFacing thisFacing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        if (side != thisFacing.getOpposite()) {
            return null;
        }
        return tankCoolableIn;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        // TODO: remove in next version
        NBTTagCompound tanksTag = nbt.getCompoundTag("tanks");
        if (tanksTag.hasKey("heatable_out")) {
            tanksTag.setTag("heatableOut", tanksTag.getTag("heatable_out"));
        }
        if (tanksTag.hasKey("coolable_in")) {
            tanksTag.setTag("coolableIn", tanksTag.getTag("coolable_in"));
        }
        super.readFromNBT(nbt);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_TANK_COOLABLE_IN, buffer, side);
                writePayload(NET_TANK_HEATABLE_OUT, buffer, side);
            } else if (id == NET_TANK_COOLABLE_IN) {
                smoothedCoolableIn.writeInit(buffer);
            } else if (id == NET_TANK_HEATABLE_OUT) {
                smoothedHeatableOut.writeInit(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_TANK_COOLABLE_IN, buffer, side, ctx);
                readPayload(NET_TANK_HEATABLE_OUT, buffer, side, ctx);
                smoothedHeatableOut.resetSmoothing(getWorld());
                smoothedCoolableIn.resetSmoothing(getWorld());
            } else if (id == NET_TANK_COOLABLE_IN) {
                smoothedCoolableIn.handleMessage(getWorld(), buffer);
            } else if (id == NET_TANK_HEATABLE_OUT) {
                smoothedHeatableOut.handleMessage(getWorld(), buffer);
            }
        }
    }

    @Override
    public void update() {
        smoothedCoolableIn.tick(getWorld());
        smoothedHeatableOut.tick(getWorld());
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("heatable_out = " + tankHeatableOut.getDebugString());
        left.add("coolable_in = " + tankCoolableIn.getDebugString());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("coolable:");
        smoothedCoolableIn.getDebugInfo(left, right, side);
        left.add("");
        left.add("heatable:");
        smoothedHeatableOut.getDebugInfo(left, right, side);
    }

    @Nullable
    public IFluidHandler getFluidAutoOutputTarget() {
        TileEntity tile = getNeighbourTile(EnumFacing.UP);
        if (tile == null) {
            return null;
        }
        return tile.getCapability(CapUtil.CAP_FLUIDS, EnumFacing.DOWN);
    }
}
