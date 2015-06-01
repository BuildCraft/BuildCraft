/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicSign extends SchematicTile {

	boolean isWall;

	public SchematicSign(boolean isWall) {
		this.isWall = isWall;
	}

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(Items.sign));
	}

	@Override
	public void storeRequirements(IBuilderContext context, BlockPos pos) {
		// cancel requirements reading
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		if (!isWall) {
			double angle = (getLevel() * 360.0) / 16.0;
			angle += 90.0;
			if (angle >= 360) {
				angle -= 360;
			}
			setMetaData((int) (angle / 360.0 * 16.0));
		} else {
			setMetaData(EnumFacing.values()[getLevel()].getIndex());
		}
	}
}