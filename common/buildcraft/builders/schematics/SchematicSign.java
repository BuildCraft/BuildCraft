/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

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
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		// cancel requirements reading
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		if (!isWall) {
			double angle = (meta * 360.0) / 16.0;
			angle += 90.0;
			if (angle >= 360) {
				angle -= 360;
			}
			meta = (int) (angle / 360.0 * 16.0);
		} else {
			meta = ForgeDirection.values()[meta].getRotation(ForgeDirection.UP).ordinal();
		}
	}
}
