package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;

public class Pipe {
	
	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;
	
	public final PipeTransport transport;
	public final PipeLogic logic;
	public final int itemID;
	
	public Pipe (PipeTransport transport, PipeLogic logic, int itemID) {
		this.transport = transport;
		this.logic = logic;
		this.itemID = itemID;
	}
	
	public void setPosition (int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		
		transport.setPosition(xCoord, yCoord, zCoord);
		logic.setPosition(xCoord, yCoord, zCoord);
	}
	
	public void setWorld (World worldObj) {
		this.worldObj = worldObj;
		transport.setWorld(worldObj);
		logic.setWorld(worldObj);
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
		
	}
	
	public void onNeighborBlockChange() {
		
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
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		transport.writeToNBT(nbttagcompound);
		logic.writeToNBT(nbttagcompound);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		transport.readFromNBT(nbttagcompound);
		logic.readFromNBT(nbttagcompound);
	}	
}
