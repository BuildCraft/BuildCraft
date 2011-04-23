package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;

public class TileExtractor extends TileEntity {
	
	long lastMining = 0;
	boolean lastPower = false;
	
	public TileExtractor () {
		
	}
	
	public void checkPower () {
		World w = ModLoader.getMinecraftInstance().theWorld;
		boolean currentPower = w.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		
		if (lastPower != currentPower) {
			extract ();
		}
		
		lastPower = currentPower;
	}
	
	/** 
	 * Extracts a random piece of item outside of a nearby chest.
	 */
	public void extract () {						
		World w = ModLoader.getMinecraftInstance().theWorld;
		
		if (w.getWorldTime() - lastMining < 50) {
			return;
		}
		
		lastMining = w.getWorldTime();
		
		LinkedList<Position> chests = new LinkedList<Position>();
		
		for (int j = 0; j < 6; ++j) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[j]);
			pos.moveForwards(1.0);
			
			TileEntity tile = w.getBlockTileEntity((int) pos.i, (int) pos.j,
					(int) pos.k);
			
			if (tile instanceof TileEntityChest) {
				TileEntityChest chest = (TileEntityChest) tile;
				
				for (int k = 0; k < chest.getSizeInventory(); ++k) {
					if (chest.getStackInSlot(k) != null
							&& chest.getStackInSlot(k).stackSize > 0) {
						chests.add(pos);
					}
				}
			}
		}
		
		if (chests.size() == 0) {
			return;
		}
		
		Position chestPos = chests.get(w.rand.nextInt(chests.size()));
		TileEntityChest chest = (TileEntityChest) w.getBlockTileEntity(
				(int) chestPos.i, (int) chestPos.j, (int) chestPos.k);
		
		ItemStack stack = null;
		
		for (int k = 0; k < chest.getSizeInventory(); ++k) {
			ItemStack slot = chest.getStackInSlot(k);
			if (slot != null && slot.stackSize > 0) {
				stack = new ItemStack (slot.getItem (), 1);
				
				chest.decrStackSize(k, 1);
				break;
			}
		}	
		
		if (Utils.addToRandomPipeEntry(this, Orientations.Unknown, stack)) {
			//  The object has been added to a nearby pipe.
			return;
		}		
		
		// Throw the object away.
		// TODO: factorize that code
		
		Position objectPos = new Position(xCoord + 0.5, yCoord
				+ Utils.getPipeFloorOf(stack), zCoord + 0.5,
				chestPos.orientation.reverse());
		objectPos.moveForwards(0.5);
				
		EntityItem entityitem = new EntityItem(w, objectPos.i,
				objectPos.j, objectPos.k, stack);

		objectPos.i = 0;
		objectPos.j = 0;
		objectPos.k = 0;
		
		objectPos.moveForwards(0.04);
		
		float f3 = 0.05F;
		entityitem.motionX = (float) w.rand.nextGaussian() * f3 + objectPos.i;
		entityitem.motionY = (float) w.rand.nextGaussian() * f3 + objectPos.j;
		entityitem.motionZ = (float) w.rand.nextGaussian() * f3 + objectPos.k;
		w.entityJoinedWorld(entityitem);		
	}

}
