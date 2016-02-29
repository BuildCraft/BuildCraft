package buildcraft.factory.tile;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftFactory;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IComplexRefineryRecipeManager.IDistilationRecipe;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankManager;

import io.netty.buffer.ByteBuf;

public class TileDistiller extends TileBuildCraft implements IFluidHandler, IHasWork, IControllable, IDebuggable, IInventory {
    private final Tank in, outGas, outLiquid;
    private final TankManager<Tank> manager;
    private IDistilationRecipe currentRecipe;
    private int sleep = 0, lateSleep = 0;
    private long lastCraftTick = -1;

    @SideOnly(Side.CLIENT)
    private boolean hasCraftedRecently;

    public TileDistiller() {
        in = new Tank("in", 1000, this);
        outGas = new Tank("outGas", 1000, this);
        outLiquid = new Tank("outLiquid", 1000, this);
        manager = new TankManager<>(in, outGas, outLiquid);
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
        manager.readData(stream);
        sleep = stream.readInt();
        hasCraftedRecently = stream.readBoolean();
    }

    @Override
    public void writeData(ByteBuf stream) {
        manager.writeData(stream);
        stream.writeInt(sleep);
        stream.writeBoolean(worldObj.getTotalWorldTime() - lastCraftTick < 30);
    }

    @SideOnly(Side.CLIENT)
    public Tank getInputTank() {
        return in;
    }

    @SideOnly(Side.CLIENT)
    public Tank getOutputTankGas() {
        return outGas;
    }

    @SideOnly(Side.CLIENT)
    public Tank getOutputTankLiquid() {
        return outLiquid;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCraftedRecently() {
        return hasCraftedRecently;
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
                distill(true);
            } else if (hasWork(false)) {
                if (lateSleep < 20) {
                    lateSleep++;
                    return;
                }
                distill(false);
                lateSleep = 0;
            }
        }
    }

    private void export() {
        exportGas();
        exportLiquid();
    }

    private void exportGas() {
        if (outGas.getFluidAmount() <= 0) return;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.distillerBlock) return;
        EnumFacing exportDir = EnumFacing.UP;
        exportFluid(outGas, exportDir);
    }

    private void exportLiquid() {
        if (outLiquid.getFluidAmount() <= 0) return;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null || state.getBlock() != BuildCraftFactory.distillerBlock) return;
        EnumFacing exportDir = EnumFacing.DOWN;
        exportFluid(outLiquid, exportDir);
    }

    private void exportFluid(Tank from, EnumFacing exportDir) {
        TileEntity tile = worldObj.getTileEntity(getPos().offset(exportDir));
        if (!(tile instanceof IPipeTile)) return;
        if (!(tile instanceof IFluidHandler)) return;
        IFluidHandler fluid = (IFluidHandler) tile;
        if (!fluid.canFill(exportDir.getOpposite(), from.getFluidType())) return;
        FluidStack stack = from.drain(20, true);
        int filled = fluid.fill(exportDir.getOpposite(), stack, true);
        if (filled < stack.amount) {
            FluidStack back = stack.copy();
            back.amount -= filled;
            from.fill(back, true);
        }
    }

    private void checkRecipe() {
        if (currentRecipe == null) {
            currentRecipe = BuildcraftRecipeRegistry.complexRefinery.getDistilationRegistry().getRecipeForInput(in.getFluid());
            if (currentRecipe != null) {
                resetSleep();
            }
            return;
        }
        if (!currentRecipe.in().equals(in.getFluid())) {
            currentRecipe = null;
        }
    }

    private void distill(boolean care) {
        if (!care) return;
        FluidStack stack = in.drain(currentRecipe.in().amount, true);
        if (stack.amount < currentRecipe.in().amount) {
            in.fill(stack, true);
        } else {
            outGas.fill(currentRecipe.outGas(), true);
            outLiquid.fill(currentRecipe.outLiquid(), true);
            resetSleep();
            lastCraftTick = worldObj.getTotalWorldTime();
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
        if (from == null || from.getAxis() == Axis.Y) return 0;
        if (BuildcraftRecipeRegistry.complexRefinery.getDistilationRegistry().getRecipeForInput(resource) == null) return 0;
        return in.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if (!canDrain(from, resource.getFluid())) return null;
        if (from == EnumFacing.DOWN) {
            if (outLiquid.getFluid().equals(resource)) return outLiquid.drain(resource.amount, doDrain);
        } else if (from == EnumFacing.UP) {
            if (outGas.getFluid().equals(resource)) return outGas.drain(resource.amount, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        if (from == EnumFacing.DOWN) {
            return outLiquid.drain(maxDrain, doDrain);
        } else if (from == EnumFacing.UP) {
            return outGas.drain(maxDrain, doDrain);
        } else return null;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        if (from == null || from.getAxis() == Axis.Y) return false;
        return in.fill(new FluidStack(fluid, 1), false) == 1;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        if (from == EnumFacing.DOWN) {
            return outLiquid.drain(1, false) != null;
        } else if (from == EnumFacing.UP) {
            return outGas.drain(1, false) != null;
        } else return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        if (from == null) return new FluidTankInfo[0];
        if (from == EnumFacing.DOWN) {
            return new FluidTankInfo[] { outLiquid.getInfo() };
        } else if (from == EnumFacing.UP) {
            return new FluidTankInfo[] { outGas.getInfo() };
        }
        return new FluidTankInfo[] { in.getInfo() };
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
        // Gas
        ret &= outGas.isEmpty() || outGas.getFluid().equals(currentRecipe.outGas());
        ret &= outGas.getCapacity() - outGas.getFluidAmount() >= currentRecipe.outGas().amount;

        // Liquid
        ret &= outLiquid.isEmpty() || outLiquid.getFluid().equals(currentRecipe.outLiquid());
        ret &= outLiquid.getCapacity() - outLiquid.getFluidAmount() >= currentRecipe.outLiquid().amount;
        return ret;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        Tank[] tanks = { in, outGas, outLiquid };
        left.add("");
        left.add("Sleep = " + sleep);
        for (Tank t : tanks) {
            left.add(StringUtils.capitalize(t.getTankName()) + ":");
            left.add(" " + t.getFluidAmount() + "/" + t.getCapacity() + "mB");
            left.add(" " + (t.getFluid() == null ? "empty" : t.getFluidType().getLocalizedName(t.getFluid())));
        }
    }

    @Override
    public int getSizeInventory() {
        return 3;
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
