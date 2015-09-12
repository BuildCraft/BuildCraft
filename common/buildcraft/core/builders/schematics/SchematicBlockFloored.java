package buildcraft.core.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicBlockFloored extends SchematicBlock {
	@Override
	public boolean canPlaceInWorld(IBuilderContext context, int x, int y, int z) {
		return y > 0 && !context.world().isAirBlock(x, y - 1, z);
	}
}
