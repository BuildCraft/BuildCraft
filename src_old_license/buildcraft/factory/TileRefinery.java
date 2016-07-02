/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import java.util.List;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.TankManager;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.lib.fluids.SingleUseTank;

public class TileRefinery extends TileBuildCraft implements IFluidHandler, IInventory, IHasWork, IFlexibleCrafter, ICommandReceiver, IDebuggable {

    public static int FLUID_PER_SLOT = FluidContainerRegistry.BUCKET_VOLUME * 4;

    public IFlexibleRecipe<FluidStack> currentRecipe;
    public CraftingResult<FluidStack> craftingResult;

    public SingleUseTank[] tanks = { new SingleUseTank("tank1", FLUID_PER_SLOT, this), new SingleUseTank("tank2", FLUID_PER_SLOT, this) };

    public SingleUseTank result = new SingleUseTank("result", FLUID_PER_SLOT, this);
    public TankManager<SingleUseTank> tankManager = new TankManager<>(tanks[0], tanks[1], result);
    public float animationSpeed = 1;
    private short animationStage = 0;
    // TODO (PASS 1): Change this to either not be deprecated, or something else?
    private SafeTimeTracker time = new SafeTimeTracker();

    private SafeTimeTracker updateNetworkTime = new SafeTimeTracker(BuildCraftCore.updateFactor);
    private boolean isActive;

    private String currentRecipeId = "";

    public TileRefinery() {
        super();
        this.setBattery(new RFBattery(10000, 1500, 0));
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {}

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return worldObj.getTileEntity(pos) == this;
    }

    @Override
    public ItemStack removeStackFromSlot(int var1) {
        return null;
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            simpleAnimationIterate();
            return;
        }

        if (updateNetworkTime.markTimeIfDelay(worldObj)) {
            sendNetworkUpdate();
        }

        isActive = false;

        if (currentRecipe == null) {
            decreaseAnimation();
            return;
        }

        if (result.fill(craftingResult.crafted.copy(), false) != craftingResult.crafted.amount) {
            decreaseAnimation();
            return;
        }

        isActive = true;

        if (getBattery().getEnergyStored() >= craftingResult.energyCost) {
            increaseAnimation();
        } else {
            decreaseAnimation();
        }

        if (!time.markTimeIfDelay(worldObj, craftingResult.craftingTime)) {
            return;
        }

        if (getBattery().useEnergy(craftingResult.energyCost, craftingResult.energyCost, false) > 0) {
            CraftingResult<FluidStack> r = currentRecipe.craft(this, false);
            result.fill(r.crafted.copy(), true);
        }
    }

    @Override
    public boolean hasWork() {
        return isActive;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        tankManager.readFromNBT(data);

        animationStage = data.getShort("animationStage");
        animationSpeed = data.getFloat("animationSpeed");

        updateRecipe();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        tankManager.writeToNBT(data);

        data.setShort("animationStage", animationStage);
        data.setFloat("animationSpeed", animationSpeed);
    }

    public int getAnimationStage() {
        return animationStage;
    }

    /** Used to iterate the animation without computing the speed */
    public void simpleAnimationIterate() {
        if (animationSpeed > 1) {
            animationStage += animationSpeed;

            if (animationStage > 300) {
                animationStage = 100;
            }
        } else if (animationStage > 0) {
            animationStage--;
        }
    }

    public void increaseAnimation() {
        if (animationSpeed < 2) {
            animationSpeed = 2;
        } else if (animationSpeed <= 5) {
            animationSpeed += 0.1;
        }

        animationStage += animationSpeed;

        if (animationStage > 300) {
            animationStage = 100;
        }
    }

    public void decreaseAnimation() {
        if (animationSpeed >= 1) {
            animationSpeed -= 0.1;

            animationStage += animationSpeed;

            if (animationStage > 300) {
                animationStage = 100;
            }
        } else {
            if (animationStage > 0) {
                animationStage--;
            }
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    public void resetFilters() {
        for (SingleUseTank tank : tankManager) {
            tank.setAcceptedFluid(null);
        }
    }

    public void setFilter(int number, Fluid fluid) {
        tankManager.get(number).setAcceptedFluid(fluid);
    }

    public Fluid getFilter(int number) {
        return tankManager.get(number).getAcceptedFluid();
    }

    /* SMP GUI */
    public void getGUINetworkData(int id, int data) {
        switch (id) {
            case 0:
                setFilter(0, FluidRegistry.getFluid(data));
                break;
            case 1:
                setFilter(1, FluidRegistry.getFluid(data));
                break;
        }
    }

    public void sendGUINetworkData(Container container, ICrafting iCrafting) {
        if (getFilter(0) != null) {
            iCrafting.sendProgressBarUpdate(container, 0, getFilter(0).getID());
        }
        if (getFilter(1) != null) {
            iCrafting.sendProgressBarUpdate(container, 1, getFilter(1).getID());
        }
    }

    /* ITANKCONTAINER */
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        int used = 0;
        FluidStack resourceUsing = resource.copy();

        if (RefineryRecipeManager.INSTANCE.getValidFluidStacks1().contains(resource)) {
            used += tanks[0].fill(resourceUsing, doFill);
            resourceUsing.amount -= used;
        }
        if (RefineryRecipeManager.INSTANCE.getValidFluidStacks2().contains(resource)) {
            used += tanks[1].fill(resourceUsing, doFill);
            resourceUsing.amount -= used;
        }
        updateRecipe();

        return used;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxEmpty, boolean doDrain) {
        FluidStack r = result.drain(maxEmpty, doDrain);

        updateRecipe();

        return r;
    }

    private void updateRecipe() {
        currentRecipe = null;
        craftingResult = null;

        for (IFlexibleRecipe<FluidStack> recipe : RefineryRecipeManager.INSTANCE.getRecipes()) {
            craftingResult = recipe.craft(this, true);

            if (craftingResult != null) {
                currentRecipe = recipe;
                currentRecipeId = currentRecipe.getId();
                break;
            }
        }
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(result.getFluid())) {
            return null;
        }
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing direction) {
        return tankManager.getTankInfo(direction);
    }

    // Network
    @Override
    public void writeData(ByteBuf stream) {
        stream.writeFloat(animationSpeed);
        NetworkUtils.writeUTF(stream, currentRecipeId);
        tankManager.writeData(stream);
    }

    @Override
    public void readData(ByteBuf stream) {
        animationSpeed = stream.readFloat();
        currentRecipeId = NetworkUtils.readUTF(stream);
        tankManager.readData(stream);

        currentRecipe = RefineryRecipeManager.INSTANCE.getRecipe(currentRecipeId);

        if (currentRecipe != null) {
            craftingResult = currentRecipe.craft(this, true);
        }
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getCraftingItemStackSize() {
        return 0;
    }

    @Override
    public ItemStack getCraftingItemStack(int slotid) {
        return null;
    }

    @Override
    public ItemStack decrCraftingItemStack(int slotid, int val) {
        return null;
    }

    @Override
    public FluidStack getCraftingFluidStack(int tankid) {
        return tanks[tankid].getFluid();
    }

    @Override
    public FluidStack decrCraftingFluidStack(int tankid, int val) {
        FluidStack resultF;

        if (val >= tanks[tankid].getFluid().amount) {
            resultF = tanks[tankid].getFluid();
            tanks[tankid].setFluid(null);
        } else {
            resultF = tanks[tankid].getFluid().copy();
            resultF.amount = val;
            tanks[tankid].getFluid().amount -= val;
        }

        updateRecipe();

        return resultF;
    }

    @Override
    public int getCraftingFluidStackSize() {
        return tanks.length;
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        if (side == Side.SERVER && "setFilter".equals(command)) {
            setFilter(stream.readByte(), FluidRegistry.getFluid(stream.readShort()));
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("Side Tank 1");
        left.add(" " + tanks[0].getFluidAmount() + "/" + tanks[0].getCapacity() + "mB");
        left.add(" " + (tanks[0].getFluid() == null ? "empty" : tanks[0].getFluidType().getLocalizedName(tanks[0].getFluid())));
        left.add("Side Tank 2");
        left.add(" " + tanks[1].getFluidAmount() + "/" + tanks[1].getCapacity() + "mB");
        left.add(" " + (tanks[1].getFluid() == null ? "empty" : tanks[1].getFluidType().getLocalizedName(tanks[1].getFluid())));
        left.add("Result");
        left.add(" " + result.getFluidAmount() + "/" + result.getCapacity() + "mB");
        left.add(" " + (result.getFluid() == null ? "empty" : result.getFluidType().getLocalizedName(result.getFluid())));
    }
}
