/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class TileDistiller_BC8 extends TileBC_Neptune implements IDebuggable, ITickable {
    public static final FunctionContext MODEL_FUNC_CTX;
    private static final NodeVariableObject<Direction> MODEL_FACING;
    private static final NodeVariableBoolean MODEL_ACTIVE;
    private static final NodeVariableLong MODEL_POWER_AVG;
    private static final NodeVariableLong MODEL_POWER_MAX;

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("Distiller");
    public static final int NET_TANK_IN = IDS.allocId("TANK_IN");
    public static final int NET_TANK_GAS_OUT = IDS.allocId("TANK_GAS_OUT");
    public static final int NET_TANK_LIQUID_OUT = IDS.allocId("TANK_LIQUID_OUT");

    static {
        MODEL_FUNC_CTX = DefaultContexts.createWithAll();
        MODEL_FACING = MODEL_FUNC_CTX.putVariableObject("direction", Direction.class);
        MODEL_POWER_AVG = MODEL_FUNC_CTX.putVariableLong("power_average");
        MODEL_POWER_MAX = MODEL_FUNC_CTX.putVariableLong("power_max");
        MODEL_ACTIVE = MODEL_FUNC_CTX.putVariableBoolean("active");
    }

    public static final long MAX_MJ_PER_TICK = 6 * MjAPI.MJ;

    private final Tank tankIn = new Tank("in", 4 * FluidAttributes.BUCKET_VOLUME, this, this::isDistillableFluid);
    private final Tank tankGasOut = new Tank("gasOut", 4 * FluidAttributes.BUCKET_VOLUME, this);
    private final Tank tankLiquidOut = new Tank("liquidOut", 4 * FluidAttributes.BUCKET_VOLUME, this);

    private final MjBattery mjBattery = new MjBattery(1024 * MjAPI.MJ);

    public final FluidSmoother smoothedTankIn;
    public final FluidSmoother smoothedTankGasOut;
    public final FluidSmoother smoothedTankLiquidOut;

    /** The model variables, used to keep track of the various state-based variables. */
    public final ModelVariableData clientModelData = new ModelVariableData();

    private IRefineryRecipeManager.IDistillationRecipe currentRecipe;
    private long distillPower = 0;
    private boolean isActive = false;
    private final AverageLong powerAvg = new AverageLong(100);
    private final SafeTimeTracker updateTracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate, 2);
    private boolean changedSinceNetUpdate = true;

    private long powerAvgClient;

    public TileDistiller_BC8() {
        super(BCFactoryBlocks.distillerTile.get());
        tankIn.setCanDrain(false);
        tankGasOut.setCanFill(false);
        tankLiquidOut.setCanFill(false);

        tankManager.add(tankIn);
        tankManager.add(tankGasOut);
        tankManager.add(tankLiquidOut);

        smoothedTankIn = new FluidSmoother(createSender(NET_TANK_IN), tankIn);
        smoothedTankGasOut = new FluidSmoother(createSender(NET_TANK_GAS_OUT), tankGasOut);
        smoothedTankLiquidOut = new FluidSmoother(createSender(NET_TANK_LIQUID_OUT), tankLiquidOut);

        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankIn, EnumPipePart.HORIZONTALS);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankGasOut, EnumPipePart.UP);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankLiquidOut, EnumPipePart.DOWN);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> !tankIn.isEmpty(), EnumPipePart.VALUES);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(mjBattery)));
    }

    private FluidSmoother.IFluidDataSender createSender(int netId) {
        return writer -> createAndSendMessage(netId, writer);
    }

    private boolean isDistillableFluid(FluidStack fluid) {
        IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
//        IRefineryRecipeManager.IDistillationRecipe recipe = manager.getDistillationRegistry().getRecipeForInput(fluid);
        IDistillationRecipe recipe = manager.getDistillationRegistry().getRecipeForInput(this.level, fluid);
        return recipe != null;
    }

    @Override
//    public CompoundNBT writeToNBT(CompoundNBT nbt)
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.put("tanks", tankManager.serializeNBT());
        nbt.put("battery", mjBattery.serializeNBT());
        nbt.putLong("distillPower", distillPower);
        powerAvg.writeToNbt(nbt, "powerAvg");
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        tankManager.deserializeNBT(nbt.getCompound("tanks"));
        mjBattery.deserializeNBT(nbt.getCompound("battery"));
        distillPower = nbt.getLong("distillPower");
        powerAvg.readFromNbt(nbt, "powerAvg");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_TANK_IN, buffer, side);
                writePayload(NET_TANK_GAS_OUT, buffer, side);
                writePayload(NET_TANK_LIQUID_OUT, buffer, side);
                buffer.writeBoolean(isActive);
                powerAvgClient = powerAvg.getAverageLong();
                final long div = MjAPI.MJ / 2;
                powerAvgClient = Math.round(powerAvgClient / (double) div) * div;
                buffer.writeLong(powerAvgClient);
            } else if (id == NET_TANK_IN) {
                smoothedTankIn.writeInit(buffer);
            } else if (id == NET_TANK_GAS_OUT) {
                smoothedTankGasOut.writeInit(buffer);
            } else if (id == NET_TANK_LIQUID_OUT) {
                smoothedTankLiquidOut.writeInit(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_TANK_IN, buffer, side, ctx);
                readPayload(NET_TANK_GAS_OUT, buffer, side, ctx);
                readPayload(NET_TANK_LIQUID_OUT, buffer, side, ctx);

                smoothedTankIn.resetSmoothing(getLevel());
                smoothedTankGasOut.resetSmoothing(getLevel());
                smoothedTankLiquidOut.resetSmoothing(getLevel());

                isActive = buffer.readBoolean();
                powerAvgClient = buffer.readLong();
            } else if (id == NET_TANK_IN) {
                smoothedTankIn.handleMessage(getLevel(), buffer);
            } else if (id == NET_TANK_GAS_OUT) {
                smoothedTankGasOut.handleMessage(getLevel(), buffer);
            } else if (id == NET_TANK_LIQUID_OUT) {
                smoothedTankLiquidOut.handleMessage(getLevel(), buffer);
            }
        }
    }

    public static void setClientModelVariablesForItem() {
        DefaultContexts.RENDER_PARTIAL_TICKS.value = 1;
        MODEL_ACTIVE.value = false;
        MODEL_POWER_AVG.value = 0;
        MODEL_POWER_MAX.value = 6;
        MODEL_FACING.value = Direction.WEST;
    }

    public void setClientModelVariables(float partialTicks) {
        DefaultContexts.RENDER_PARTIAL_TICKS.value = partialTicks;

        MODEL_ACTIVE.value = isActive;
        MODEL_POWER_AVG.value = powerAvgClient / MjAPI.MJ;
        MODEL_POWER_MAX.value = MAX_MJ_PER_TICK / MjAPI.MJ;
        MODEL_FACING.value = Direction.WEST;

        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() == BCFactoryBlocks.distiller.get()) {
            MODEL_FACING.value = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        }
    }

    @Override
    public void update() {
        ITickable.super.update();
        smoothedTankIn.tick(getLevel());
        smoothedTankGasOut.tick(getLevel());
        smoothedTankLiquidOut.tick(getLevel());
        if (level.isClientSide) {
            setClientModelVariables(1);
            clientModelData.tick();
            return;
        }
        powerAvg.tick();
        changedSinceNetUpdate |= powerAvgClient != powerAvg.getAverageLong();

        currentRecipe =
//                BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(tankIn.getFluid());
                BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(this.level, tankIn.getFluid());
        if (currentRecipe == null) {
            mjBattery.addPowerChecking(distillPower, false);
            distillPower = 0;
            isActive = false;
        } else {
            FluidStack reqIn = currentRecipe.in();
            FluidStack outLiquid = currentRecipe.outLiquid();
            FluidStack outGas = currentRecipe.outGas();

            FluidStack potentialIn = tankIn.drainInternal(reqIn, IFluidHandler.FluidAction.SIMULATE);
            boolean canExtract = reqIn.isFluidStackIdentical(potentialIn);

            boolean canFillLiquid = tankLiquidOut.fillInternal(outLiquid, IFluidHandler.FluidAction.SIMULATE) == outLiquid.getAmount();
            boolean canFillGas = tankGasOut.fillInternal(outGas, IFluidHandler.FluidAction.SIMULATE) == outGas.getAmount();

            if (canExtract && canFillLiquid && canFillGas) {
                long max = MAX_MJ_PER_TICK;
                max *= mjBattery.getStored() + max;
                max /= mjBattery.getCapacity() / 2;
                max = Math.min(max, MAX_MJ_PER_TICK);
                long powerReq = currentRecipe.powerRequired();
                long power = mjBattery.extractPower(0, max);
                powerAvg.push(max);
                distillPower += power;
                isActive = power > 0;
                if (distillPower >= powerReq) {
                    isActive = true;
                    distillPower -= powerReq;
                    tankIn.drainInternal(reqIn, IFluidHandler.FluidAction.EXECUTE);
                    tankGasOut.fillInternal(outGas, IFluidHandler.FluidAction.EXECUTE);
                    tankLiquidOut.fillInternal(outLiquid, IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                mjBattery.addPowerChecking(distillPower, false);
                distillPower = 0;
                isActive = false;
            }
        }

        if (changedSinceNetUpdate && updateTracker.markTimeIfDelay(level)) {
            powerAvgClient = powerAvg.getAverageLong();
            sendNetworkUpdate(NET_RENDER_DATA);
            changedSinceNetUpdate = false;
        }
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        left.add("In = " + tankIn.getDebugString());
//        left.add("GasOut = " + tankGasOut.getDebugString());
//        left.add("LiquidOut = " + tankLiquidOut.getDebugString());
//        left.add("Battery = " + mjBattery.getDebugString());
//        left.add("Progress = " + MjAPI.formatMj(distillPower));
//        left.add("Rate = " + LocaleUtil.localizeMjFlow(powerAvgClient));
//        left.add("CurrRecipe = " + currentRecipe);
        left.add(new StringTextComponent("In = " + tankIn.getDebugString()));
        left.add(new StringTextComponent("GasOut = " + tankGasOut.getDebugString()));
        left.add(new StringTextComponent("LiquidOut = " + tankLiquidOut.getDebugString()));
        left.add(new StringTextComponent("Battery = " + mjBattery.getDebugString()));
        left.add(new StringTextComponent("Progress = " + MjAPI.formatMj(distillPower)));
        left.add(new StringTextComponent("Rate = ").append(LocaleUtil.localizeMjFlowComponent(powerAvgClient)));
        left.add(new StringTextComponent("CurrRecipe = " + currentRecipe));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
        setClientModelVariables(1);
        left.add("Model Variables:");
        left.add("  facing = " + MODEL_FACING.value);
        left.add("  active = " + MODEL_ACTIVE.value);
        left.add("  power_average = " + MODEL_POWER_AVG.value);
        left.add("  power_max = " + MODEL_POWER_MAX.value);
        left.add("Current Model Variables:");
        clientModelData.refresh();
        clientModelData.addDebugInfo(left);
    }

    // Calen: for other mods to show tanks contents

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        LazyOptional<T> ret = super.getCapability(capability, facing);
        if ((!ret.isPresent()) && facing == null && capability == CapUtil.CAP_FLUIDS) {
            ret = LazyOptional.of(() -> fakeFluidHandlerOfAllTanks).cast();
        }
        return ret;
    }

    private IFluidHandler fakeFluidHandlerOfAllTanks = new IFluidHandler() {
        @Override
        public int getTanks() {
            return tankManager.getTanks();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return tankManager.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tankManager.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return StackUtil.EMPTY_FLUID;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return StackUtil.EMPTY_FLUID;
        }
    };
}
