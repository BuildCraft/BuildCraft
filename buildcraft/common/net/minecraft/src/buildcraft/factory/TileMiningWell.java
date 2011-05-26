package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftFactory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.Utils;

public class TileMiningWell extends TileEntity implements IMachine {
	
	long lastMining = 0;
	boolean lastPower = false;
	boolean isDigging = true;
	
	EntityModel model = null;
	
	public TileMiningWell () {
		
	}
	
	boolean init = false;
	
	public void updateEntity() {
		if (!init) {
			 model = new EntityModel(worldObj, xCoord, yCoord,
					zCoord);
			worldObj.entityJoinedWorld(model);
			
			init = true;
		}
	}
	
	public void checkPower () {
		World w = CoreProxy.getWorld();
		boolean currentPower = w.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);		
		if (lastPower != currentPower) {
			dig ();
		}
		
		lastPower = currentPower;
	}
	
	/** Dig the next available piece of land if not done. As soon as it 
	 * reaches bedrock, lava or goes below 0, it's considered done.
	 */
	public void dig () {						
		World w = CoreProxy.getWorld();
		
		if (w.getWorldTime() - lastMining < 50) {
			return;
		}
		
		lastMining = w.getWorldTime();
		
		int depth = yCoord - 1;
		
		while (w.getBlockId(xCoord, depth, zCoord) == mod_BuildCraftFactory.plainPipeBlock.blockID) {
			depth = depth - 1;
		}
		
		if (depth < 0
			|| w.getBlockId(xCoord, depth, zCoord) == Block.bedrock.blockID
			|| w.getBlockId(xCoord, depth, zCoord) == Block.lavaMoving.blockID
			||w.getBlockId(xCoord, depth, zCoord) == Block.lavaStill.blockID) {
			
			    isDigging = false;
			    
				return;
			}
		
		int blockId = w.getBlockId(xCoord, depth, zCoord);
		
		ItemStack stack = BuildCraftBlockUtil.getItemStackFromBlock(w, xCoord, depth, zCoord);
		
		w.setBlockWithNotify((int) xCoord, (int) depth, (int) zCoord,
				mod_BuildCraftFactory.plainPipeBlock.blockID);
		
		if (blockId == 0) {
			return;
		}							
		
		if (stack == null) {
			return;
		}
		
		StackUtil stackUtil = new StackUtil(stack);
		
		if (stackUtil.addToRandomInventory(this, Orientations.Unknown)) {
			//  The object has been added to a nearby chest.
			return;
		}
		
		if (Utils.addToRandomPipeEntry(this, Orientations.Unknown, stack)) {
			//  The object has been added to a nearby pipe.
			return;
		}
		
		// Throw the object away.
		// TODO: factorize that code
		
		float f = w.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = w.rand.nextFloat() * 0.8F + 0.1F;
		float f2 = w.rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityitem = new EntityItem(w, (float) xCoord + f,
				(float) yCoord + f1 + 0.5F, (float) zCoord + f2, stack);

		float f3 = 0.05F;
		entityitem.motionX = (float) w.rand.nextGaussian() * f3;
		entityitem.motionY = (float) w.rand.nextGaussian() * f3
				+ 1.0F;
		entityitem.motionZ = (float) w.rand.nextGaussian() * f3;
		w.entityJoinedWorld(entityitem);
	}

	@Override
	public boolean isActive() {
		return isDigging;
	}
	
	public void destroy () {
		if (model != null) {
			model.setEntityDead();
		}
	}

}
