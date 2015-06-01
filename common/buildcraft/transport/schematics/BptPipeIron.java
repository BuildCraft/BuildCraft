/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.schematics;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.BuildCraftProperties;

public class BptPipeIron extends BptPipeExtension {

	public BptPipeIron(Item i) {
		super(i);
	}

	@Override
	public void rotateLeft(SchematicTile slot, IBuilderContext context) {
		// TODO: Convert Pipes to have states better defined. (Split PIPE_DATA into FACING and POWERED)
		int index = (Integer) slot.state.getValue(BuildCraftProperties.PIPE_DATA);
		int orientation = index % 6;
		int extra = index / 6;
		EnumFacing facing = EnumFacing.VALUES[orientation];
		EnumFacing newFacing = facing.rotateY();
		int newIndex = newFacing.getIndex() + extra * 6;
		slot.state = slot.state.withProperty(BuildCraftProperties.PIPE_DATA, newIndex);
	}
}
