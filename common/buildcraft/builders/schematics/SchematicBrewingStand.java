package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicBrewingStand extends SchematicTile {
	@Override
	public void initializeFromObjectAt(IBuilderContext context, int x, int y, int z) {
		super.initializeFromObjectAt(context, x, y, z);

		if (tileNBT != null) {
			tileNBT.removeTag("BrewTime");
		}
	}
}
