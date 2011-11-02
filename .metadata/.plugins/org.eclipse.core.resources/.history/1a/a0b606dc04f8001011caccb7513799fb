/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.TilePacketWrapper;

public abstract class Pipe {
	
	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;
	
	public final PipeTransport transport;
	public final PipeLogic logic;
	public final int itemID;
	
	private TilePacketWrapper networkPacket;
	
	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> networkWrappers = new HashMap<Class, TilePacketWrapper>();
	
	
	public Pipe(PipeTransport transport, PipeLogic logic, int itemID) {
		this.transport = transport;
		this.logic = logic;
		this.itemID = itemID;

		if (!networkWrappers.containsKey(this.getClass())) {
			networkWrappers.put(
					this.getClass(),
					new TilePacketWrapper(new Class[] {
							this.transport.getClass(), this.logic.getClass() },
							PacketIds.TileUpdate));
		}

		this.networkPacket = networkWrappers.get(this.getClass());
	}

	
	public void setPosition (int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		
		transport.setPosition(xCoord, yCoord, zCoord);
		logic.setPosition(xCoord, yCoord, zCoord);
	}
	
	public void setWorld (World worldObj) {
		if (worldObj != null && this.worldObj == null) {
			this.worldObj = worldObj;
			transport.setWorld(worldObj);
			logic.setWorld(worldObj);
		}
	}
	
	public void setTile (TileGenericPipe tile) {
		this.container = tile;
		
		transport.setTile (tile);
		logic.setTile (tile);
	}

	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}
	
	public void onBlockPlaced() {
		logic.onBlockPlaced();
		transport.onBlockPlaced ();
	}
	
	public void onNeighborBlockChange() {
		logic.onNeighborBlockChange ();
		transport.onNeighborBlockChange ();
	}
	
	public boolean isPipeConnected(TileEntity tile) {	   
    	return logic.isPipeConnected(tile) && transport.isPipeConnected (tile);
	}
	
	public int getBlockTexture() {
		return 1 * 16 + 0;
	}

	public void prepareTextureFor(Orientations connection) {

	}

	public void updateEntity() {
		transport.updateEntity ();		
		logic.updateEntity ();
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		transport.writeToNBT(nbttagcompound);
		logic.writeToNBT(nbttagcompound);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		transport.readFromNBT(nbttagcompound);
		logic.readFromNBT(nbttagcompound);
	}

	public void initialize () {
		transport.initialize ();
		logic.initialize ();
	}

	public boolean inputOpen(Orientations from) {
		return transport.inputOpen (from) && logic.inputOpen (from);
	}

	public boolean outputOpen(Orientations to) {
		return transport.outputOpen (to) && logic.outputOpen (to);
	}

	public void onEntityCollidedWithBlock(Entity entity) {
				
	}
	
	public Packet230ModLoader getNetworkPacket() {
		return networkPacket.toPacket(xCoord, yCoord, zCoord, new Object[] {
				transport, logic });
	}
	
	public void handlePacket (Packet230ModLoader packet) {
		networkPacket.updateFromPacket(new Object [] {transport, logic}, packet);
	}
}
