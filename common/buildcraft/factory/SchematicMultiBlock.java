package buildcraft.factory;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.factory.TileMultiblockMaster;

public class SchematicMultiBlock extends SchematicTile {

	@Override
	public void postProcessing(IBuilderContext context, int x, int y, int z) {
		TileMultiblockMaster master = (TileMultiblockMaster) context.world().getTileEntity(x, y, z);

		if (master != null) {
			master.formMultiblock(null);
		}
	}
}
