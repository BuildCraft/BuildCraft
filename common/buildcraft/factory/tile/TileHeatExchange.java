package buildcraft.factory.tile;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftFactory;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IComplexRefineryRecipeManager.ICoolableRecipe;
import buildcraft.api.recipes.IComplexRefineryRecipeManager.IHeatableRecipe;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.block.BlockBuildCraftBase;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankManager;

import io.netty.buffer.ByteBuf;

public class TileHeatExchange extends TileBuildCraft implements IFluidHandler, IHasWork, IControllable, IDebuggable {
    private final Tank inCoolable, outCooled;
    private final Tank inHeatable, outHeated;

    private final TankManager<Tank> manager;
    private IHeatableRecipe heatableRecipe;
    private ICoolableRecipe coolableRecipe;
    private int sleep = 0, lateSleep = 0;

    public TileHeatExchange() {
        inCoolable = new Tank("inCoolable", 500, this);
        outCooled = new Tank("outCooled", 500, this);
        inHeatable = new Tank("inHeatable", 500, this);
        outHeated = new Tank("outHeated", 500, this);
        manager = new TankManager<>(inCoolable, outCooled, inHeatable, outHeated);
        mode = Mode.On;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        manager.deserializeNBT(nbt.getCompoundTag("tanks"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tanks", manager.serializeNBT());
    }

    @Override
    public void readData(ByteBuf stream) {
        manager.readData(stream);
        sleep = stream.readInt();
    }

    @Override
    public void writeData(ByteBuf stream) {
        manager.writeData(stream);
        stream.writeInt(sleep);
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) return;

        craft();
        export();
    }

    private void craft() {
        checkRecipe();
        if (mode == Mode.On) {
            if (hasWork()) {
                if (sleep > 0) {
                    sleep--;
                    return;
                }
                exchangeHeat(true);
            } else if (hasWork(false)) {
                if (lateSleep < 20) {
                    lateSleep++;
                    return;
                }
                exchangeHeat(false);
                lateSleep = 0;
            }
        }
    }

    private void export() {
        exportCooled();
        exportHeated();
    }

    private void exportCooled() {
        if (outCooled.getFluidAmount() <= 0) return;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.heatExchangeBlock) return;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        TileEntity tile = worldObj.getTileEntity(getPos().offset(exportDir));
        if (!(tile instanceof IPipeTile)) return;
        if (!(tile instanceof IFluidHandler)) return;
        IFluidHandler fluid = (IFluidHandler) tile;
        if (!fluid.canFill(exportDir.getOpposite(), outCooled.getFluidType())) return;
        FluidStack stack = outCooled.drain(20, true);
        int filled = fluid.fill(exportDir.getOpposite(), stack, true);
        if (filled < stack.amount) {
            FluidStack back = stack.copy();
            back.amount -= filled;
            outCooled.fill(back, true);
        }
    }

    private void exportHeated() {
        if (outHeated.getFluidAmount() <= 0) return;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.heatExchangeBlock) return;
        EnumFacing exportDir = EnumFacing.UP;
        TileEntity tile = worldObj.getTileEntity(getPos().offset(exportDir));
        if (!(tile instanceof IPipeTile)) return;
        if (!(tile instanceof IFluidHandler)) return;
        IFluidHandler fluid = (IFluidHandler) tile;
        if (!fluid.canFill(exportDir.getOpposite(), outHeated.getFluidType())) return;
        FluidStack stack = outHeated.drain(20, true);
        int filled = fluid.fill(exportDir.getOpposite(), stack, true);
        if (filled < stack.amount) {
            FluidStack back = stack.copy();
            back.amount -= filled;
            outHeated.fill(back, true);
        }
    }

    private void checkRecipe() {
        boolean reset = false;

        if (heatableRecipe != null) {
            if (!heatableRecipe.in().equals(inHeatable.getFluid())) {
                heatableRecipe = null;
            }
        }
        if (coolableRecipe != null) {
            if (!coolableRecipe.in().equals(inCoolable.getFluid())) {
                coolableRecipe = null;
            }
        }

        if (heatableRecipe == null) {
            reset = true;
            heatableRecipe = BuildcraftRecipeRegistry.complexRefinery.getHeatableRegistry().getRecipeForInput(inHeatable.getFluid());
        }
        if (coolableRecipe == null) {
            reset = true;
            coolableRecipe = BuildcraftRecipeRegistry.complexRefinery.getCoolableRegistry().getRecipeForInput(inCoolable.getFluid());
        }
        if (heatableRecipe != null && coolableRecipe != null && reset) {
            sleep = Math.max(heatableRecipe.ticks(), coolableRecipe.ticks());
        }
    }

    private void exchangeHeat(boolean care) {
        // Ignore care for now, need to figure out the logic later
        if (!care) return;

        FluidStack coolant = inCoolable.drain(coolableRecipe.in().amount, true);
        FluidStack heatant = inHeatable.drain(heatableRecipe.in().amount, true);

        if (coolant.amount != coolableRecipe.in().amount || heatant.amount != heatableRecipe.in().amount) {
            inCoolable.fill(coolant, true);
            inHeatable.fill(heatant, true);
            return;
        }

        outCooled.fill(coolableRecipe.out(), true);
        outHeated.fill(heatableRecipe.out(), true);
        sleep = Math.max(coolableRecipe.ticks(), heatableRecipe.ticks());
    }

    // IFluidHandler
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        if (from.getAxis() == Axis.Y) {
            IHeatableRecipe potential = BuildcraftRecipeRegistry.complexRefinery.getHeatableRegistry().getRecipeForInput(resource);
            if (potential == null) return 0;
            if (heatableRecipe != null && potential != heatableRecipe) return 0;

            if (coolableRecipe == null || potential.heatFrom() < coolableRecipe.heatFrom()) {
                return inHeatable.fill(resource, doFill);
            }
            return 0;
        } else {
            ICoolableRecipe potential = BuildcraftRecipeRegistry.complexRefinery.getCoolableRegistry().getRecipeForInput(resource);
            if (potential == null) return 0;

            if (coolableRecipe != null && potential != coolableRecipe) return 0;

            if (heatableRecipe == null || heatableRecipe.heatFrom() < potential.heatFrom()) {
                return inCoolable.fill(resource, doFill);
            }
            return 0;
        }
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if (from.getAxis() != Axis.Y) {
            if (outCooled.isEmpty()) return null;
            if (outCooled.getFluid().equals(resource)) return outCooled.drain(resource.amount, doDrain);
        } else {
            if (outHeated.isEmpty()) return null;
            if (outHeated.getFluid().equals(resource)) return outHeated.drain(resource.amount, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        if (from.getAxis() != Axis.Y) return outCooled.drain(maxDrain, doDrain);
        return outHeated.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return fill(from, new FluidStack(fluid, 1), false) > 0;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return drain(from, new FluidStack(fluid, 1), false) != null;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return new FluidTankInfo[] { inCoolable.getInfo(), inHeatable.getInfo(), outCooled.getInfo(), outHeated.getInfo() };
    }

    // Misc Interfaces

    @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == Mode.On || mode == Mode.Off;
    }

    @Override
    public boolean hasWork() {
        return hasWork(true);
    }

    private boolean hasWork(boolean care) {
        if (heatableRecipe == null) return false;
        if (coolableRecipe == null) return false;
        boolean ret = true;
        // Compare heatable
        ret &= !care || inHeatable.getFluidAmount() >= heatableRecipe.in().amount;
        ret &= outHeated.isEmpty() || outHeated.getFluid().equals(heatableRecipe.out());
        ret &= outHeated.getCapacity() - outHeated.getFluidAmount() >= heatableRecipe.out().amount;

        // Compare coolable
        ret &= !care || inCoolable.getFluidAmount() >= coolableRecipe.in().amount;
        ret &= outCooled.isEmpty() || outCooled.getFluid().equals(coolableRecipe.out());
        ret &= outCooled.getCapacity() - outCooled.getFluidAmount() >= coolableRecipe.out().amount;

        return ret;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        Tank[] tanks = { inCoolable, inHeatable, outCooled, outHeated };
        left.add("");
        left.add("Sleep = " + sleep);
        for (Tank t : tanks) {
            left.add(StringUtils.capitalize(t.getTankName()) + ":");
            left.add(" " + t.getFluidAmount() + "/" + t.getCapacity() + "mB");
            left.add(" " + (t.getFluid() == null ? "empty" : t.getFluidType().getLocalizedName(t.getFluid())));
        }
    }
}
