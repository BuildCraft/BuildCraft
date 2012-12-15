/** 
 * Copyright (c) SpaceToad, 2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptBlockUtils;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.core.Box;

public class BptBlockFiller extends BptBlock {

	public BptBlockFiller(int blockId) {
		super(blockId);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		ItemStack[] recipeStack = BptBlockUtils.getItemStacks(slot, context);

		for (int i = 0; i < recipeStack.length; ++i) {
			if (recipeStack[i] != null) {
				requirements.add(recipeStack[i]);
			}
		}

		requirements.add(new ItemStack(BuildCraftBuilders.fillerBlock));
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		super.rotateLeft(slot, context);

		Box box = new Box();
		box.initialize(slot.cpt.getCompoundTag("box"));

		Position pMin = context.rotatePositionLeft(box.pMin());
		Position pMax = context.rotatePositionLeft(box.pMax());

		box.xMin = (int) pMin.x;
		box.yMin = (int) pMin.y;
		box.zMin = (int) pMin.z;

		box.xMax = (int) pMax.x;
		box.yMax = (int) pMax.y;
		box.zMax = (int) pMax.z;

		box.reorder();

		NBTTagCompound cpt = new NBTTagCompound();

		box.writeToNBT(cpt);
		slot.cpt.setTag("box", cpt);
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		context.world().setBlockWithNotify(slot.x, slot.y, slot.z, slot.blockId);

		TileFiller filler = (TileFiller) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);

		Box box = new Box();
		box.initialize(slot.cpt.getCompoundTag("box"));

		box.xMin += context.surroundingBox().pMin().x;
		box.yMin += context.surroundingBox().pMin().y;
		box.zMin += context.surroundingBox().pMin().z;

		box.xMax += context.surroundingBox().pMin().x;
		box.yMax += context.surroundingBox().pMin().y;
		box.zMax += context.surroundingBox().pMin().z;

		box.reorder();

		filler.box.deleteLasers();
		filler.box = box;
		filler.box.createLasers(context.world(), LaserKind.Stripes);

		ItemStack[] recipeStack = BptBlockUtils.getItemStacks(slot, context);

		for (int i = 0; i < recipeStack.length; ++i) {
			filler.setInventorySlotContents(i, recipeStack[i]);
		}
	}

	@Override
	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {
		TileFiller filler = (TileFiller) context.world().getBlockTileEntity(x, y, z);

		NBTTagCompound cpt = new NBTTagCompound();

		Box fillerBox = new Box();
		fillerBox.initialize(filler.box);

		fillerBox.xMin -= context.surroundingBox().pMin().x;
		fillerBox.yMin -= context.surroundingBox().pMin().y;
		fillerBox.zMin -= context.surroundingBox().pMin().z;

		fillerBox.xMax -= context.surroundingBox().pMin().x;
		fillerBox.yMax -= context.surroundingBox().pMin().y;
		fillerBox.zMax -= context.surroundingBox().pMin().z;

		fillerBox.reorder();

		fillerBox.writeToNBT(cpt);
		slot.cpt.setTag("box", cpt);

		ItemStack[] recipeStack = new ItemStack[9];

		for (int i = 0; i < 9; ++i) {
			recipeStack[i] = filler.getStackInSlot(i);
		}

		BptBlockUtils.setItemStacks(slot, context, recipeStack);
	}

}
