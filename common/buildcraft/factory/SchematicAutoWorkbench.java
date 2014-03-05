/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicUtils;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicAutoWorkbench extends Schematic {

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		super.addRequirements(context, requirements);

		SchematicUtils.requestInventoryContents(this, context, requirements);
	}

	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		IInventory inventory = (IInventory) context.world().getTileEntity(x, y, z);

		SchematicUtils.initializeInventoryContents(this, context, inventory);
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		super.writeToWorld(context);

		IInventory inventory = (IInventory) context.world().getTileEntity(x, y, z);

		SchematicUtils.buildInventoryContents(this, context, inventory);
	}

}
