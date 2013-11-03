package buildcraft.factory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.ItemFluidContainer;
import buildcraft.api.gates.IAction;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuffer;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.proxy.CoreProxy;

public class TileLiquidHopper extends TileHopper implements IInventory, IMachine {

	private TileBuffer[] tileBuffer = null;
	// This counter will determine the speed of the buffer tank
	private int liquidTick = 0;
	// The maximum amount of mB the buffer tank can gold
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 16;
	// Buffer tank that will push to consumers
	private SingleUseTank tank = new SingleUseTank("tank", MAX_LIQUID, this);
	
	public TileLiquidHopper() {
	    _inventory = new SimpleInventory(4, "LiquidHopper", 64);
	}

	@Override
	public void updateEntity() {
		if (CoreProxy.proxy.isRenderWorld(worldObj) || worldObj.getTotalWorldTime() % 2 != 0)
			return;

		TileEntity tile = this.worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);
		if (tile == null)
			return;

		ITransactor transactor = Transactor.getTransactorFor(tile);
		if (transactor == null)
			return;
		
		// Don't do anything until the tank is empty
		if(tank.isEmpty()) {
		    // Make sure the tank has no memory of the previous fluid.
		    tank.setAcceptedFluid(null);
		    
    		for (int internalSlot = 0; internalSlot < _inventory.getSizeInventory(); internalSlot++) {
    			ItemStack stackInSlot = _inventory.getStackInSlot(internalSlot);
    			if (stackInSlot == null)
    				continue;
    			
    			if(stackInSlot.getItem() instanceof ItemBucket
    			        || stackInSlot.getItem() instanceof IFluidContainerItem) {
    			    ItemStack clonedStack = stackInSlot.copy().splitStack(1);
    			    
    			    // Check if the attached inventory has space left for this item
    	            if (transactor.add(clonedStack, ForgeDirection.UP, false).stackSize > 0) {
    	                _inventory.decrStackSize(internalSlot, 1);
    	                
    	                // Two cases for fluid draining into the buffer tank
    	                if(stackInSlot.getItem() instanceof ItemBucket) { // item is ItemBucket
    	                    FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stackInSlot);
    	                    tank.fill(fluidStack, true);
    	                    clonedStack = FluidContainerRegistry.EMPTY_BUCKET;
    	                } else { // item is IFluidContainerItem
    	                    ItemFluidContainer container = (ItemFluidContainer) clonedStack.getItem();
    	                    // In the following case our container is empty
    	                    if(container.getFluid(stackInSlot) != null) {
    	                        FluidStack fluidStack = container.getFluid(stackInSlot).copy();
        	                    int filled = tank.fill(fluidStack, true);
        	                    container.drain(clonedStack, filled, true);
    	                    }
    	                }
    	                
    	                // Add the empty bucket/container to the attached inventory
    	                transactor.add(clonedStack, ForgeDirection.UP, true);
    	                return;
    	            }
    			}
    		}
		}
		
		// Push to pipes & tanks every tick, the speed depends on the viscosity of the fluid.
		if(!tank.isEmpty()) {
		    liquidTick++;
		    if(1 / tank.getFluid().getFluid().getViscosity() * 10 <= liquidTick)
		        pushToConsumers();
		}
	}
	
	private void pushToConsumers() {
        if (tileBuffer == null)
            tileBuffer = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
        FluidUtils.pushFluidToConsumers(tank, 400, tileBuffer);
        liquidTick = 0;
    }

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
	    return true;
	}
	
	@Override
	public boolean manageFluids() {
	    return true;
	}

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean manageSolids() {
        return false;
    }

    @Override
    public boolean allowAction(IAction action) {
        return false;
    }
}
