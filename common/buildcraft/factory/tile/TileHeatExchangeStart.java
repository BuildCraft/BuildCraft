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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockHeatExchange;

public class TileHeatExchangeStart extends TileBC_Neptune implements ITickable, IDebuggable {

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("HeatExchangeStart");
    public static final int NET_TANK_HEATABLE_IN = IDS.allocId("TANK_HEATABLE_IN");
    public static final int NET_TANK_COOLABLE_OUT = IDS.allocId("TANK_COOLABLE_OUT");
    public static final int NET_STATE = IDS.allocId("STATE");

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private static final int[] FLUID_MULT = { 10, 16, 20 };

    private final Tank tankHeatableIn = new Tank("heatable_in", 2 * Fluid.BUCKET_VOLUME, this, this::isHeatant);
    private final Tank tankCoolableOut = new Tank("coolable_out", 2 * Fluid.BUCKET_VOLUME, this);

    public final FluidSmoother smoothedHeatableIn;
    public final FluidSmoother smoothedCoolableOut;

    private TileHeatExchangeEnd tileEnd;
    private int middles;
    private int progress = 0;
    private EnumProgressState progressState = EnumProgressState.OFF;
    private EnumProgressState lastSentState = EnumProgressState.OFF;
    private int heatProvided = 0;
    private int coolingProvided = 0;

    private int progressLast = 0;

    public TileHeatExchangeStart() {
        tankManager.addAll(tankHeatableIn, tankCoolableOut);
        tankCoolableOut.setCanFill(false);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankHeatableIn, EnumPipePart.DOWN);
        caps.addCapability(CapUtil.CAP_FLUIDS, this::getTankForSide, EnumPipePart.HORIZONTALS);

        smoothedHeatableIn = createFluidSmoother(tankHeatableIn, NET_TANK_HEATABLE_IN);
        smoothedCoolableOut = createFluidSmoother(tankCoolableOut, NET_TANK_COOLABLE_OUT);
    }

    private FluidSmoother createFluidSmoother(Tank tank, int netId) {
        return new FluidSmoother(w -> createAndSendMessage(netId, w), tank);
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
        nbt.setInteger("coolingProvided", coolingProvided);
        nbt.setInteger("heatProvided", heatProvided);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        coolingProvided = nbt.getInteger("coolingProvided");
        heatProvided = nbt.getInteger("heatProvided");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_TANK_HEATABLE_IN, buffer, side);
                writePayload(NET_TANK_COOLABLE_OUT, buffer, side);
                writePayload(NET_STATE, buffer, side);
                buffer.writeInt(progress);
            } else if (id == NET_TANK_HEATABLE_IN) {
                smoothedHeatableIn.writeInit(buffer);
            } else if (id == NET_TANK_COOLABLE_OUT) {
                smoothedCoolableOut.writeInit(buffer);
            } else if (id == NET_STATE) {
                buffer.writeEnumValue(progressState);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_TANK_HEATABLE_IN, buffer, side, ctx);
                readPayload(NET_TANK_COOLABLE_OUT, buffer, side, ctx);
                readPayload(NET_STATE, buffer, side, ctx);
                progress = buffer.readInt();
                smoothedHeatableIn.resetSmoothing(getWorld());
                smoothedCoolableOut.resetSmoothing(getWorld());
            } else if (id == NET_TANK_HEATABLE_IN) {
                smoothedHeatableIn.handleMessage(getWorld(), buffer);
            } else if (id == NET_TANK_COOLABLE_OUT) {
                smoothedCoolableOut.handleMessage(getWorld(), buffer);
            } else if (id == NET_STATE) {
                progressState = buffer.readEnumValue(EnumProgressState.class);
            }
        }
    }

    @Override
    public void update() {
        smoothedHeatableIn.tick(getWorld());
        smoothedCoolableOut.tick(getWorld());
        findEnd();
        updateProgress();
        if (world.isRemote) {
            spawnParticles();
            return;
        }
        if (tileEnd != null) {
            craft();
        } else if (progressState != EnumProgressState.OFF) {
            progressState = EnumProgressState.STOPPING;
        }
        output();
        if (progressState != lastSentState) {
            lastSentState = progressState;
            sendNetworkUpdate(NET_STATE);
        }
    }

    private void updateProgress() {
        progressLast = progress;
        switch (progressState) {
            case STOPPING: {
                progress--;
                if (progress <= 0) {
                    progress = 0;
                    progressState = EnumProgressState.OFF;
                }
                return;
            }
            case PREPARING:
            case RUNNING: {
                int lag = 120;
                progress++;
                if (progress >= lag) {
                    progress = lag;
                    progressState = EnumProgressState.RUNNING;
                }
                return;
            }
            default: {
                return;
            }
        }
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
        middles = 0;
        BlockPos search = getPos();
        for (int i = 0; i <= 3; i++) {
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
            progressState = EnumProgressState.STOPPING;
            return;
        }
        if (c_recipe.heatFrom() <= h_recipe.heatFrom()) {
            BCLog.logger.warn("Invalid heat values!");
            progressState = EnumProgressState.STOPPING;
            return;
        }
        int c_diff = c_recipe.heatFrom() - c_recipe.heatTo();
        int h_diff = h_recipe.heatTo() - h_recipe.heatFrom();
        if (h_diff < 1 || c_diff < 1) {
            throw new IllegalStateException("Invalid recipe " + c_recipe + ", " + h_recipe);
        }
        // TODO: Make mult the *maximum* multiplier, not the exact one.
        int mult = FLUID_MULT[middles - 1];
        boolean needs_c = heatProvided <= 0;
        boolean needs_h = coolingProvided <= 0;

        FluidStack c_in_f = setAmount(c_recipe.in(), mult);
        FluidStack c_out_f = setAmount(c_recipe.out(), mult);
        FluidStack h_in_f = setAmount(h_recipe.in(), mult);
        FluidStack h_out_f = setAmount(h_recipe.out(), mult);
        if (canFill(c_out, c_out_f) && canFill(h_out, h_out_f) && canDrain(c_in, c_in_f) && canDrain(h_in, h_in_f)) {
            if (progressState == EnumProgressState.OFF) {
                progressState = EnumProgressState.PREPARING;
            } else if (progressState == EnumProgressState.RUNNING) {
                heatProvided--;
                coolingProvided--;
                if (needs_c) {
                    heatProvided += c_diff;
                    fill(c_out, c_out_f);
                    drain(c_in, c_in_f);
                }

                if (needs_h) {
                    coolingProvided += h_diff;
                    fill(h_out, h_out_f);
                    drain(h_in, h_in_f);
                }
            }
        } else {
            progressState = EnumProgressState.STOPPING;
        }
    }

    private void spawnParticles() {
        if (progressState == EnumProgressState.RUNNING) {
            TileHeatExchangeEnd end = tileEnd;
            if (end == null) {
                return;
            }
            Vec3d from = VecUtil.convertCenter(getPos());
            FluidStack c_in_f = end.smoothedCoolableIn.getFluidForRender();
            if (c_in_f != null && c_in_f.getFluid() == FluidRegistry.LAVA) {
                IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeStart);
                if (state != null) {
                    EnumFacing dir = state.getValue(BlockBCBase_Neptune.PROP_FACING);
                    spewForth(from, dir.getOpposite(), EnumParticleTypes.SMOKE_LARGE);
                }
            }

            FluidStack h_in_f = smoothedHeatableIn.getFluidForRender();
            from = VecUtil.convertCenter(tileEnd.getPos());
            if (h_in_f != null && h_in_f.getFluid() == FluidRegistry.WATER) {
                EnumFacing dir = EnumFacing.UP;
                spewForth(from, dir, EnumParticleTypes.CLOUD);
            }
        }
    }

    private void spewForth(Vec3d from, EnumFacing dir, EnumParticleTypes particle) {
        Vec3d vecDir = new Vec3d(dir.getDirectionVec());
        from = from.add(VecUtil.scale(vecDir, 0.5));

        double x = from.xCoord;
        double y = from.yCoord;
        double z = from.zCoord;

        Vec3d motion = VecUtil.scale(vecDir, 0.4);
        int particleCount = Minecraft.getMinecraft().gameSettings.particleSetting;
        World w = getWorld();
        if (particleCount == 2 || w == null) {
            return;
        }
        particleCount = particleCount == 0 ? 5 : 2;
        for (int i = 0; i < particleCount; i++) {
            double dx = motion.xCoord + (Math.random() - 0.5) * 0.1;
            double dy = motion.yCoord + (Math.random() - 0.5) * 0.1;
            double dz = motion.zCoord + (Math.random() - 0.5) * 0.1;
            double interp = i / (double) particleCount;
            x -= dx * interp;
            y -= dy * interp;
            z -= dz * interp;

            w.spawnParticle(particle, x, y, z, dx, dy, dz);
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

    private static FluidStack setAmount(FluidStack fluid, int mult) {
        if (fluid == null) {
            return null;
        }
        return new FluidStack(fluid, mult);
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

    public TileHeatExchangeEnd getOtherTile() {
        return tileEnd;
    }

    public double getProgress(float partialTicks) {
        return MathUtil.interp(partialTicks, progressLast, progress) / 120.0;
    }

    public EnumProgressState getProgressState() {
        return this.progressState;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        TileHeatExchangeEnd end = tileEnd;
        if (end == null) {
            return super.getRenderBoundingBox();
        }
        return BoundingBoxUtil.makeFrom(getPos(), end.getPos());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("progress = " + progress);
        left.add("state = " + progressState);
        left.add("has_end = " + (tileEnd != null));
        left.add("heatProvided = " + heatProvided);
        left.add("coolingProvided = " + coolingProvided);
        if (hasWorld() && world.isRemote) {
            left.add("");
            left.add("coolable:");
            smoothedCoolableOut.getDebugInfo(left, right, side);
            left.add("");
            left.add("heatable:");
            smoothedHeatableIn.getDebugInfo(left, right, side);
        } else {
            left.add("heatable_in = " + tankHeatableIn.getDebugString());
            left.add("coolable_out = " + tankCoolableOut.getDebugString());
        }
    }

    public enum EnumProgressState {
        /** Progress is at 0, not moving. */
        OFF,
        /** Progress is increasing from 0 to max */
        PREPARING,
        /** progress stays at max */
        RUNNING,
        /** Progress is decreasing from max to 0. */
        STOPPING;
    }
}
