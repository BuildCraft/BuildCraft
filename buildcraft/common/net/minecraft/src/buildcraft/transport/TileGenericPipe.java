package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.PacketIds;

public class TileGenericPipe extends TileEntity implements IPowerReceptor,
		ILiquidContainer, ISpecialInventory, IPipeEntry, ISynchronizedTile {
	
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
			initialized = true;
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

	@Override
	public int getSizeInventory() {
		return pipe.logic.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return pipe.logic.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return pipe.logic.decrStackSize(i, j);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		pipe.logic.setInventorySlotContents(i, itemstack);		
	}

	@Override
	public String getInvName() {
		return pipe.logic.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return pipe.logic.getInventoryStackLimit();
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return pipe.logic.canInteractWith(entityplayer);
	}

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		return pipe.logic.addItem(stack, doAdd, from);
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		return pipe.logic.extractItem(doRemove, from);
	}

	@Override
	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
		pipe.transport.entityEntering (item, orientation);
		
	}

	@Override
	public boolean acceptItems() {
		return pipe.transport.acceptItems();
	}

	@Override
	public void handleDescriptionPacket(Packet230ModLoader packet) {
		pipe.handlePacket(packet);
		
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		if (pipe == null) {
			pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, packet.dataInt [3]);
			pipe.setTile(this);	
		}
	}

	@Override
	public void postPacketHandling(Packet230ModLoader packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Packet230ModLoader getUpdatePacket() {
		return pipe.getNetworkPacket();
	}

	@Override
	public Packet getDescriptionPacket() {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = mod_BuildCraftCore.instance.getId();
		packet.isChunkDataPacket = true;
		packet.packetType = PacketIds.TileDescription.ordinal();
		
		packet.dataInt = new int [4];
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		packet.dataInt [3] = pipe.itemID;
		
		return packet;
	}

}
