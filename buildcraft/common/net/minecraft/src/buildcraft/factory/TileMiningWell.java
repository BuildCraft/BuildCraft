package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.IPowerReceptor;
import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;

public class TileMiningWell extends TileBuildCraft implements IMachine, IPowerReceptor {
	boolean isDigging = true;
	
	PowerProvider powerProvider;

	
	public TileMiningWell () {		
		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(50, 25, 25, 25, 1000);
	}
	
	/** 
	 * Dig the next available piece of land if not done. As soon as it 
	 * reaches bedrock, lava or goes below 0, it's considered done.
	 */
	@Override
	public void doWork () {	
		if (powerProvider.useEnergy(25, 25, true) < 25) {
			return;
		}
		
		World w = worldObj;
		
		int depth = yCoord - 1;
		
		while (w.getBlockId(xCoord, depth, zCoord) == BuildCraftFactory.plainPipeBlock.blockID) {
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
		
		ItemStack stack = BuildCraftBlockUtil.getItemStackFromBlock(w, xCoord,
				depth, zCoord);
		
		w.setBlockWithNotify((int) xCoord, (int) depth, (int) zCoord,
				BuildCraftFactory.plainPipeBlock.blockID);
		
		if (blockId == 0) {
			return;
		}							
		
		if (stack == null) {
			return;
		}
		
		StackUtil stackUtil = new StackUtil(stack);
		
		if (stackUtil.addToRandomInventory(this, Orientations.Unknown)
				&& stackUtil.items.stackSize == 0) {
			//  The object has been added to a nearby chest.
			return;
		}
		
		if (Utils.addToRandomPipeEntry(this, Orientations.Unknown, stack)
				&& stackUtil.items.stackSize == 0) {
			//  The object has been added to a nearby pipe.
			return;
		}
		
		// Throw the object away.
		// TODO: factorize that code
		
		float f = w.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = w.rand.nextFloat() * 0.8F + 0.1F;
		float f2 = w.rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityitem = new EntityItem(w, (float) xCoord + f,
				(float) yCoord + f1 + 0.5F, (float) zCoord + f2,
				stackUtil.items);

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

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;		
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}
	

}
