/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.schematics;

import net.minecraft.inventory.IInventory;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicRotateInventory extends SchematicRotateMeta {

	public SchematicRotateInventory(int[] rotations, boolean rotateForward) {
		super(rotations, rotateForward);

	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		super.writeToWorld(context);

		IInventory inv = (IInventory) context.world().getTileEntity(x, y, z);

		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			inv.setInventorySlotContents(i, null);
		}
	}

}
