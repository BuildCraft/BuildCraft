/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.JavaTools;

public class SchematicJukebox extends SchematicTile {
	public SchematicJukebox() {
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		super.storeRequirements(context, x, y, z);
		if (tileNBT != null && tileNBT.hasKey("RecordItem")) {
			ItemStack recordStack = ItemStack.loadItemStackFromNBT(tileNBT.getCompoundTag("RecordItem"));
			storedRequirements = JavaTools.concat(storedRequirements, new ItemStack[] { recordStack });
		}
	}
}