package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.ILiquidContainer;

public class TileGenericPipe extends TileEntity implements IPowerReceptor, ILiquidContainer {
	public Pipe pipe;
	private boolean blockNeighborChange = false;
	private boolean initialized = false;

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {		
			nbttagcompound.setInteger("pipeId", pipe.itemID);
			pipe.writeToNBT(nbttagcompound);
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, nbttagcompound.getInteger("pipeId"));
		pipe.setTile(this);
		pipe.readFromNBT(nbttagcompound);	
	}
		
	@Override
	public void validate () {
		super.validate();
		
		if (pipe == null) {
			pipe = BlockGenericPipe.pipeBuffer.get(new BlockIndex(xCoord, yCoord, zCoord));
			pipe.setTile(this);
		}
		
		pipe.setWorld(worldObj);
	}
	
	@Override
	public void updateEntity () {
		if (!initialized) {
			pipe.initialize();
		}
		
		if (blockNeighborChange) {
			pipe.onNeighborBlockChange();
			blockNeighborChange = false;
		}
		
		PowerProvider provider = getPowerProvider();
		
		if (provider != null) {			
			provider.update(this);
		}
		
		pipe.updateEntity ();
	
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {		
		if (pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).setPowerProvider(provider);
		}
		
	}

	@Override
	public PowerProvider getPowerProvider() {
		if (pipe instanceof IPowerReceptor) {
			return ((IPowerReceptor) pipe).getPowerProvider();
		} else {
			return null;
		}
	}

	@Override
	public void doWork() {
		if (pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).doWork();
		}		
	}

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).fill(from, quantity, id, doFill);
		} else {
			return 0;	
		}		
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).empty(quantityMax, doEmpty);
		} else {
			return 0;
		}
	}

	@Override
	public int getLiquidQuantity() {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getLiquidQuantity();
		} else {
			return 0;	
		}		
	}

	@Override
	public int getCapacity() {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getCapacity();
		} else {
			return 0;
		}
	}

	@Override
	public int getLiquidId() {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getLiquidId();
		} else {
			return 0;
		}
	}
	
	public void scheduleNeighborChange() {
		blockNeighborChange  = true;
	}

}
