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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.recipes.IRefineryRecipeManager.ICoolableRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockHeatExchange;

public class TileHeatExchangeStart extends TileBC_Neptune implements ITickable, IDebuggable {
    public final Tank tankHeatableIn = new Tank("heatable_in", 2 * Fluid.BUCKET_VOLUME, this, this::isHeatant);
    public final Tank tankCoolableOut = new Tank("coolable_out", 2 * Fluid.BUCKET_VOLUME, this);
    private final TankManager<Tank> tankManager = new TankManager<>(tankHeatableIn, tankCoolableOut);

    private TileHeatExchangeEnd tileEnd;
    private int progress = 0;

    public TileHeatExchangeStart() {
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankHeatableIn, EnumPipePart.DOWN);
        caps.addCapability(CapUtil.CAP_FLUIDS, this::getTankForSide, EnumPipePart.HORIZONTALS);
        tankCoolableOut.setCanFill(false);
    }

    private boolean isHeatant(FluidStack fluid) {
        return BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getRecipeForInput(fluid) != null;
    }

    private IFluidHandler getTankForSide(EnumFacing side) {
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeStart);
        if (state == null) {
            return null;
        }
        EnumFacing thisFacing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        if (side != thisFacing.getOpposite()) {
            return null;
        }
        return tankCoolableOut;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tanks", tankManager.serializeNBT());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tankManager.deserializeNBT(nbt.getCompoundTag("tanks"));
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                tankManager.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                tankManager.readData(buffer);
            }
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            // TODO: Client stuffs
            return;
        }
        tileEnd = null;
        findEnd();
        if (tileEnd != null) {
            craft();
        }
        output();
    }

    private void findEnd() {
        // TODO (AlexIIL): Make this check passive, not active.
        tileEnd = null;
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeStart);
        if (state == null) {
            // BCLog.logger.warn("Null state");
            return;
        }
        BlockHeatExchange block = (BlockHeatExchange) state.getBlock();
        EnumFacing facing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        int middles = 0;
        BlockPos search = getPos();
        for (int i = 0; i < 3; i++) {
            search = search.offset(facing);
            state = getLocalState(search);
            if (state.getBlock() != BCFactoryBlocks.heatExchangeMiddle) {
                // BCLog.logger.warn("Not middle @ " + search + " (" + i + ")");
                break;
            }
            block = BCFactoryBlocks.heatExchangeMiddle;
            if (block.part.getAxis(state) != facing.getAxis()) {
                // BCLog.logger.warn("Wrong axis!");
                return;
            }
            middles++;
        }
        if (middles == 0) {
            // BCLog.logger.warn("No middles!");
            return;
        }
        if (state.getBlock() != BCFactoryBlocks.heatExchangeEnd) {
            // BCLog.logger.warn("Not end @ " + search);
            return;
        }
        if (state.getValue(BlockBCBase_Neptune.PROP_FACING) != facing.getOpposite()) {
            // BCLog.logger.warn("Wrong EnumFacing");
            return;
        }
        TileEntity tile = getLocalTile(search);
        if (tile instanceof TileHeatExchangeEnd) {
            tileEnd = (TileHeatExchangeEnd) tile;
        } else {
            // BCLog.logger.warn("Not end tile!");
        }
    }

    private void craft() {
        Tank c_in = tileEnd.tankCoolableIn;
        Tank c_out = tankCoolableOut;
        Tank h_in = tankHeatableIn;
        Tank h_out = tileEnd.tankHeatableOut;
        IRefineryRecipeManager reg = BuildcraftRecipeRegistry.refineryRecipes;
        ICoolableRecipe c_recipe = reg.getCoolableRegistry().getRecipeForInput(c_in.getFluid());
        IHeatableRecipe h_recipe = reg.getHeatableRegistry().getRecipeForInput(h_in.getFluid());
        if (h_recipe == null || c_recipe == null) {
            return;
        }
        if (c_recipe.heatFrom() <= h_recipe.heatFrom()) {
            BCLog.logger.warn("Invalid heat values!");
            return;
        }
        int c_diff = c_recipe.heatFrom() - c_recipe.heatTo();
        int h_diff = h_recipe.heatTo() - h_recipe.heatFrom();
        if (h_diff < 1 || c_diff < 1) {
            throw new IllegalStateException("Invalid recipe " + c_recipe + ", " + h_recipe);
        }
        int c_mult = 1;
        int h_mult = 1;
        if (h_diff != c_diff) {
            int lcm = MathUtil.findLowestCommonMultiple(c_diff, h_diff);
            c_mult = lcm / c_diff;
            h_mult = lcm / h_diff;
        }
        FluidStack c_in_f = mult(c_recipe.in(), c_mult);
        FluidStack c_out_f = mult(c_recipe.out(), c_mult);
        FluidStack h_in_f = mult(h_recipe.in(), h_mult);
        FluidStack h_out_f = mult(h_recipe.out(), h_mult);
        if (canFill(c_out, c_out_f) && canFill(h_out, h_out_f) && canDrain(c_in, c_in_f) && canDrain(h_in, h_in_f)) {
            progress++;
            int lag = Math.max(c_recipe.ticks(), h_recipe.ticks());
            if (progress >= lag) {
                fill(c_out, c_out_f);
                fill(h_out, h_out_f);
                drain(c_in, c_in_f);
                drain(h_in, h_in_f);
                progress = lag;
                if (c_in_f.getFluid() == FluidRegistry.LAVA) {
                    // Output is at the other end
                    Vec3d from = VecUtil.convertCenter(getPos());
                    EnumFacing dir = EnumFacing.SOUTH;
                    spewForth(from, dir, EnumParticleTypes.SMOKE_LARGE);
                }
                if (h_in_f.getFluid() == FluidRegistry.WATER) {
                    // Output is here
                    Vec3d from = VecUtil.convertCenter(tileEnd.getPos());
                    EnumFacing dir = EnumFacing.UP;
                    spewForth(from, dir, EnumParticleTypes.CLOUD);
                }
            }
        }
    }

    private static void spewForth(Vec3d from, EnumFacing dir, EnumParticleTypes particle) {
        Vec3d vecDir = new Vec3d(dir.getDirectionVec());
        from = from.add(VecUtil.scale(vecDir, 0.5));

        double x = from.xCoord;
        double y = from.yCoord;
        double z = from.zCoord;

        Vec3d motion = VecUtil.scale(vecDir, 0.4);
        for (int i = 0; i < 10; i++) {
            double dx = motion.xCoord + Math.random() * 0.01;
            double dy = motion.yCoord + Math.random() * 0.01;
            double dz = motion.zCoord + Math.random() * 0.01;

            WorldClient w = Minecraft.getMinecraft().world;
            if (w != null) {
                w.spawnParticle(particle, x, y, z, dx, dy, dz);
            }
        }
    }

    private void output() {
        IFluidHandler thisOut = getFluidAutoOutputTarget();
        FluidUtilBC.move(tankCoolableOut, thisOut, 1000);

        if (tileEnd != null) {
            IFluidHandler endOut = tileEnd.getFluidAutoOutputTarget();
            FluidUtilBC.move(tileEnd.tankHeatableOut, endOut, 1000);
        }
    }

    private static FluidStack mult(FluidStack fluid, int mult) {
        if (fluid == null) {
            return null;
        }
        switch (mult) {
            case 0:
            case 1:
                return fluid;
            default:
                return new FluidStack(fluid, fluid.amount * mult);
        }
    }

    private static boolean canFill(Tank t, FluidStack fluid) {
        return fluid == null || t.fillInternal(fluid, false) == fluid.amount;
    }

    private static boolean canDrain(Tank t, FluidStack fluid) {
        FluidStack f2 = t.drainInternal(fluid, false);
        return f2 != null && f2.amount == fluid.amount;
    }

    private static void fill(Tank t, FluidStack fluid) {
        if (fluid == null) {
            return;
        }
        int a = t.fillInternal(fluid, true);
        if (a != fluid.amount) {
            String err = "Buggy transation! Failed to fill " + fluid.getFluid();
            throw new IllegalStateException(err + " x " + fluid.amount + " into " + t);
        }
    }

    private static void drain(Tank t, FluidStack fluid) {
        FluidStack f2 = t.drainInternal(fluid, true);
        if (f2 == null || f2.amount != fluid.amount) {
            String err = "Buggy transation! Failed to drain " + fluid.getFluid();
            throw new IllegalStateException(err + " x " + fluid.amount + " from " + t);
        }
    }

    @Nullable
    private IFluidHandler getFluidAutoOutputTarget() {
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeStart);
        if (state == null) {
            return null;
        }
        EnumFacing facing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity tile = getNeighbourTile(facing.getOpposite());
        if (tile == null) {
            return null;
        }
        return tile.getCapability(CapUtil.CAP_FLUIDS, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("heatable_in = " + tankHeatableIn.getDebugString());
        left.add("coolable_out = " + tankCoolableOut.getDebugString());
        left.add("progress = " + progress);
    }
}
