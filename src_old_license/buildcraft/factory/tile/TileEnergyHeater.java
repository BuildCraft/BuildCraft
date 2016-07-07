package buildcraft.factory.tile;

import java.util.List;
import io.netty.buffer.ByteBuf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IComplexRefineryRecipeManager.IHeatableRecipe;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.BlockBuildCraftBase;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;

public class TileEnergyHeater extends TileBuildCraft implements IFluidHandler, IHasWork, IControllable, IDebuggable, IInventory {
    private final Tank in, out;
    private final TankManager<Tank> manager;
    private final SafeTimeTracker networkTimeTracker = new SafeTimeTracker(BuildCraftCore.updateFactor);
    private IHeatableRecipe currentRecipe;
    private int sleep = 0;
    private long lastCraftTick = -1;

    public TileEnergyHeater() {
        this.setBattery(new RFBattery(1000, 20, 0));
        in = new Tank("in", 1000, this);
        out = new Tank("out", 1000, this);
        manager = new TankManager<>(in, out);
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
    @SideOnly(Side.CLIENT)
    public void readData(ByteBuf stream) {
        getBattery().setEnergy(stream.readInt());
        manager.readData(stream);
        sleep = stream.readInt();
        lastCraftTick = stream.readLong();
    }

    @Override
    public void writeData(ByteBuf stream) {
        stream.writeInt(getBattery().getEnergyStored());
        manager.writeData(stream);
        stream.writeInt(sleep);
        stream.writeLong(lastCraftTick);
    }

    @SideOnly(Side.CLIENT)
    public Tank getInputTank() {
        return in;
    }

    @SideOnly(Side.CLIENT)
    public Tank getOutputTank() {
        return out;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCraftedRecently() {
        return lastCraftTick + 30 > worldObj.getTotalWorldTime();
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEnergy() {
        checkRecipe();
        if (currentRecipe == null) return getBattery().getEnergyStored() > 10;
        int heatDiff = currentRecipe.heatTo() - currentRecipe.heatFrom();
        int required = heatDiff * BuildCraftFactory.rfPerHeatPerMB * currentRecipe.ticks() * currentRecipe.in().amount;
        return getBattery().getEnergyStored() >= required;
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) return;

        if (networkTimeTracker.markTimeIfDelay(worldObj)) {
            sendNetworkUpdate();
        }

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
                heat();
            }
        }
    }

    private void export() {
        if (out.getFluidAmount() <= 0) return;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.energyHeaterBlock) return;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        TileEntity tile = worldObj.getTileEntity(getPos().offset(exportDir));
        if (!(tile instanceof IPipeTile)) return;
        if (!(tile instanceof IFluidHandler)) return;
        IFluidHandler fluid = (IFluidHandler) tile;
        if (!fluid.canFill(exportDir.getOpposite(), out.getFluidType())) return;
        FluidStack stack = out.drain(20, true);
        int filled = fluid.fill(exportDir.getOpposite(), stack, true);
        if (filled < stack.amount) {
            FluidStack back = stack.copy();
            back.amount -= filled;
            out.fill(back, true);
        }
    }

    private void checkRecipe() {
        if (currentRecipe == null) {
            currentRecipe = BuildcraftRecipeRegistry.complexRefinery.getHeatableRegistry().getRecipeForInput(in.getFluid());
            if (currentRecipe != null) {
                resetSleep();
            }
            return;
        }
        if (!currentRecipe.in().equals(in.getFluid())) {
            currentRecipe = null;
        }
    }

    private void heat() {
        int heatDiff = currentRecipe.heatTo() - currentRecipe.heatFrom();
        int required = heatDiff * BuildCraftFactory.rfPerHeatPerMB * currentRecipe.ticks() * Math.min(in.getFluidAmount(), currentRecipe.in().amount);
        if (getBattery().useEnergy(required, required, false) == required) {
            FluidStack stack = in.drain(currentRecipe.in().amount, true);
            if (stack.amount < currentRecipe.in().amount) {
                in.fill(stack, true);
            } else {
                out.fill(currentRecipe.out(), true);
                resetSleep();
                lastCraftTick = worldObj.getTotalWorldTime();
            }
        }
    }

    private void resetSleep() {
        double multiplier = in.getCapacity() - in.getFluidAmount();
        multiplier /= in.getCapacity();
        multiplier *= 3;
        if (multiplier < 1) multiplier = 1;
        sleep = currentRecipe.ticks() * (int) multiplier;
    }

    // IFluidHandler
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.energyHeaterBlock) return 0;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        if (exportDir.getOpposite() != from) return 0;

        if (BuildcraftRecipeRegistry.complexRefinery.getHeatableRegistry().getRecipeForInput(resource) == null) return 0;
        return in.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.energyHeaterBlock) return null;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        if (exportDir != from) return null;

        if (!canDrain(from, resource.getFluid())) return null;
        if (out.getFluid().equals(resource)) return out.drain(resource.amount, doDrain);
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.energyHeaterBlock) return null;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        if (exportDir != from) return null;

        return out.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.energyHeaterBlock) return false;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        if (exportDir.getOpposite() != from) return false;

        return in.fill(new FluidStack(fluid, 1), false) == 1;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.energyHeaterBlock) return false;
        EnumFacing curFace = state.getValue(BlockBuildCraftBase.FACING_PROP);
        EnumFacing exportDir = curFace.rotateYCCW();
        if (exportDir != from) return false;

        return out.drain(1, false) != null;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return new FluidTankInfo[] { in.getInfo(), out.getInfo() };
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
        if (currentRecipe == null) return false;
        boolean ret = !care || in.getFluidAmount() >= currentRecipe.in().amount;
        ret &= out.isEmpty() || out.getFluid().equals(currentRecipe.out());
        ret &= out.getCapacity() - out.getFluidAmount() >= currentRecipe.out().amount;
        return ret;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        Tank[] tanks = { in, out };
        left.add("");
        left.add("Sleep = " + sleep);
        left.add("Power = " + getBattery().getEnergyStored() + "RF");
        left.add("Input");
        left.add(" " + tanks[0].getFluidAmount() + "/" + tanks[0].getCapacity() + "mB");
        left.add(" " + (tanks[0].getFluid() == null ? "empty" : tanks[0].getFluidType().getLocalizedName(tanks[0].getFluid())));
        left.add("Output");
        left.add(" " + tanks[1].getFluidAmount() + "/" + tanks[1].getCapacity() + "mB");
        left.add(" " + (tanks[1].getFluid() == null ? "empty" : tanks[1].getFluidType().getLocalizedName(tanks[1].getFluid())));
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (isItemValidForSlot(index, stack)) {

        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }
}
