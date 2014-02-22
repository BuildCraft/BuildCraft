/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import buildcraft.api.builder.BlockHandler;
import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;

public class MaskHandler extends BlockHandler {

	@Override
	public boolean buildBlockFromSchematic(World world, SchematicBuilder builder, IBlueprintBuilderAgent builderAgent) {
		MaskSchematic mask = (MaskSchematic) builder.schematic;

		if (mask.isPlain) {
			return builderAgent.buildBlock(builder.getX(), builder.getY(), builder.getZ());
		} else {
			return builderAgent.breakBlock(builder.getX(), builder.getY(), builder.getZ());
		}
	}

	@Override
	public boolean isComplete(World world, SchematicBuilder builder) {
		MaskSchematic mask = (MaskSchematic) builder.schematic;

		if (mask.isPlain) {
			return world.getBlock(builder.getX(), builder.getY(), builder.getZ()) != Blocks.air;
		} else {
			return world.getBlock(builder.getX(), builder.getY(), builder.getZ()) == Blocks.air;
		}
	}

}
