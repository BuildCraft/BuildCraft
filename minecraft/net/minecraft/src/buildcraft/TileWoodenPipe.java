package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;

public class TileWoodenPipe extends TilePipe {
	
	long lastMining = 0;
	boolean lastPower = false;
	
	public TileWoodenPipe () {
		
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
		
		Position entityPos = new Position(chestPos.i + 0.5, chestPos.j
				+ Utils.getPipeFloorOf(stack), chestPos.k + 0.5,
				chestPos.orientation.reverse());
				
		entityPos.moveForwards(0.5);
				
		EntityPassiveItem entity = new EntityPassiveItem(w, entityPos.i,
				entityPos.j, entityPos.k, stack);
		
		w.entityJoinedWorld(entity);
		entityEntering(entity, entityPos.orientation);		
	}

}
