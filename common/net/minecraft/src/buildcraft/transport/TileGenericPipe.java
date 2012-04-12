/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.PersistentTile;
import net.minecraft.src.buildcraft.core.PersistentWorld;
import net.minecraft.src.buildcraft.core.network.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.network.PacketPayload;
import net.minecraft.src.buildcraft.core.network.PacketPipeDescription;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;


public class TileGenericPipe extends TileEntity implements IPowerReceptor,
		ILiquidContainer, ISpecialInventory, IPipeEntry, ISynchronizedTile {
	
	public SafeTimeTracker networkSyncTracker = new SafeTimeTracker();
	
	public Pipe pipe;
	private boolean blockNeighborChange = false;
	private boolean pipeBound = false;

	@TileNetworkData public int pipeId = -1;
	
	public TileGenericPipe () {
		
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {		
			nbttagcompound.setInteger("pipeId", pipe.itemID);
			pipe.writeToNBT(nbttagcompound);
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		pipe = BlockGenericPipe.createPipe(nbttagcompound.getInteger("pipeId"));
		
		if (pipe != null) {
			pipe.setTile(this);
			pipe.readFromNBT(nbttagcompound);
		}
	}	
	
	public void synchronizeIfDelay (int delay) {
		if (APIProxy.isServerSide()) {
			if (networkSyncTracker.markTimeIfDelay(worldObj, delay)) {
				CoreProxy.sendToPlayers(getUpdatePacket(), xCoord, yCoord,
						zCoord, 40, mod_BuildCraftCore.instance);
			}
		}	
	}
	
	@Override
	public void invalidate () {
		super.invalidate();
		
		if (BlockGenericPipe.isValid (pipe)) {
			BlockGenericPipe.removePipe (pipe);
		}
		
		// Clean the persistent world in case the tile is still here.
		PersistentWorld.getWorld(worldObj).removeTile(
				new BlockIndex(xCoord, yCoord, zCoord));
	}
	
	@Override
	public void validate () {
		bindPipe();
	}
	
	@Override
	public void updateEntity () {		
		bindPipe ();
		if (pipe != null) {
			pipe.initialize();
		}
		
		if (!BlockGenericPipe.isValid(pipe)) {
			return;
		}
		
		if (blockNeighborChange) {
			pipe.onNeighborBlockChange();
			blockNeighborChange = false;
		}
		
		PowerProvider provider = getPowerProvider();
		
		if (provider != null) {			
			provider.update(this);
		}
		
		if (pipe != null) {
			pipe.updateEntity ();
		}
	}

	private void bindPipe() {
		if (!pipeBound) {		
			if (pipe == null) {
				PersistentTile tile = PersistentWorld.getWorld(worldObj).getTile(new BlockIndex(xCoord,
						yCoord, zCoord));
				
				if (tile != null && tile instanceof Pipe) {
					pipe = (Pipe) tile;				
				}
			}
			
			if (pipe != null) {
				pipe.setTile(this);
				pipe.setWorld(worldObj);
				PersistentWorld.getWorld(worldObj).storeTile(pipe,
						new BlockIndex(xCoord, yCoord, zCoord));
				pipeId = pipe.itemID;				
				pipeBound = true;				
			}	
		}
		
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {		
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).setPowerProvider(provider);
		}
		
	}

	@Override
	public PowerProvider getPowerProvider() {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			return ((IPowerReceptor) pipe).getPowerProvider();
		} else {
			return null;
		}
	}

	@Override
	public void doWork() {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).doWork();
		}		
	}

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (BlockGenericPipe.isValid(pipe)
				&& pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).fill(from, quantity, id,
					doFill);
		} else {
			return 0;
		}
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		if (BlockGenericPipe.isValid(pipe)
				&& pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).empty(quantityMax,
					doEmpty);
		} else {
			return 0;
		}
	}

	@Override
	public int getLiquidQuantity() {
		if (BlockGenericPipe.isValid(pipe)
				&& pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getLiquidQuantity();
		} else {
			return 0;
		}
	}

	@Override
	public int getCapacity() {
		if (BlockGenericPipe.isValid(pipe)
				&& pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getCapacity();
		} else {
			return 0;
		}
	}

	@Override
	public int getLiquidId() {
		if (BlockGenericPipe.isValid(pipe)
				&& pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getLiquidId();
		} else {
			return 0;
		}
	}
	
	public void scheduleNeighborChange() {
		blockNeighborChange  = true;
	}

	@Override
	public int getSizeInventory() {
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			return pipe.logic.getSizeInventory();
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			return pipe.logic.getStackInSlot(i);
		} else {
			return null;
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			return pipe.logic.decrStackSize(i, j);
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			pipe.logic.setInventorySlotContents(i, itemstack);
		}
	}

	@Override
	public String getInvName() {
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			return pipe.logic.getInvName();
		} else {
			return "";
		}
	}

	@Override
	public int getInventoryStackLimit() {
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			return pipe.logic.getInventoryStackLimit();
		} else {
			return 0;
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}
		
		if (BlockGenericPipe.isFullyDefined(pipe)) {
			return pipe.logic.canInteractWith(entityplayer);
		} else {
			return false;
		}
	}

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.addItem(stack, doAdd, from);
		} else {
			return false;
		}
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.extractItem(doRemove, from);
		} else {
			return null;
		}
	}

	@Override
	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
		if (BlockGenericPipe.isValid(pipe)) {
			pipe.transport.entityEntering (item, orientation);
		}		
	}

	@Override
	public boolean acceptItems() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.transport.acceptItems();
		} else {
			return false;
		}
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		if (pipe == null && packet.payload.intPayload[0] != 0) {
			pipe = BlockGenericPipe.createPipe(packet.payload.intPayload[0]);
			pipeBound = false;
			bindPipe();
			
			if (pipe != null) {
				pipe.initialize();
			}
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {				
		if (BlockGenericPipe.isValid(pipe)) {
			pipe.handlePacket(packet);
		}
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Packet getUpdatePacket() {
		return new PacketTileUpdate(this).getPacket();
	}

	@Override
	public Packet getDescriptionPacket() {
		bindPipe();
	
		PacketPipeDescription packet;
		if(pipe != null)
			packet = new PacketPipeDescription(xCoord, yCoord, zCoord, pipe.itemID);
		else
			packet = new PacketPipeDescription(xCoord, yCoord, zCoord, 0);
	
		return packet.getPacket();
	}
	
	@Override
	public PacketPayload getPacketPayload() {
		return pipe.getNetworkPacket();
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1){
		return null;
	}

}
