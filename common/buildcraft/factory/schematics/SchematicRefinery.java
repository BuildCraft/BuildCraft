/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.schematics;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicRefinery extends SchematicTile {

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(BuildCraftFactory.refineryBlock));
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		meta = ForgeDirection.values()[meta].getRotation(ForgeDirection.UP).ordinal();
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, int x, int y, int z) {
		super.initializeFromObjectAt(context, x, y, z);

		tileNBT.removeTag("tank1");
		tileNBT.removeTag("tank2");
		tileNBT.removeTag("result");
		tileNBT.removeTag("mjStored");
	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		// to support refineries coming from older blueprints
		tileNBT.removeTag("tank1");
		tileNBT.removeTag("tank2");
		tileNBT.removeTag("result");
		tileNBT.removeTag("mjStored");

		super.placeInWorld(context, x, y, z, stacks);
	}

}
