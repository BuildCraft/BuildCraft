package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.IMachine;

public class Pipe {
	
	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	
	public final PipeTransport transport;
	public final PipeLogic logic;
	public final int itemID;
	
	public Pipe (PipeTransport transport, PipeLogic logic, int itemID) {
		this.transport = transport;
		this.logic = logic;
		this.itemID = itemID;
	}
	
	public void initialize (int xCoord, int yCoord, int zCoord, World worldObj) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.worldObj = worldObj;
	}
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		return logic.blockActivated(world, i, j, k, entityplayer);
	}
	
	public int getTextureForConnection (IBlockAccess blockAccess, int i, int j, int k, Orientations connection) {
		return 0;
	}
	
	public void onBlockPlaced() {
		
	}
	
	public void onNeighborBlockChange() {
		
	}
	
	public boolean isPipeConnected(TileEntity tile) {	   
    	return tile instanceof TileGenericPipe 
    	    || tile instanceof IPipeEntry
			|| tile instanceof IInventory
			|| tile instanceof IMachine
			|| tile instanceof ILiquidContainer;
	}
	
	public int getBlockTexture() {
		return 1 * 16 + 0;
	}
}
