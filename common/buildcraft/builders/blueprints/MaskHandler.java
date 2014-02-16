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

	public boolean isComplete(World world, SchematicBuilder builder) {
		MaskSchematic mask = (MaskSchematic) builder.schematic;

		if (mask.isPlain) {
			return world.getBlock(builder.getX(), builder.getY(), builder.getZ()) != Blocks.air;
		} else {
			return world.getBlock(builder.getX(), builder.getY(), builder.getZ()) == Blocks.air;
		}
	}

}
