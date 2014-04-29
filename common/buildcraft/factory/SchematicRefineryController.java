package buildcraft.factory;

import buildcraft.api.blueprints.IBuilderContext;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.LinkedList;

public class SchematicRefineryController extends SchematicMultiBlock {

	@Override
	public void rotateLeft(IBuilderContext context) {
		cpt.setInteger("orientation", ForgeDirection.getOrientation(cpt.getInteger("orientation")).getRotation(ForgeDirection.UP).ordinal());
	}

	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		TileRefineryController tile = (TileRefineryController) context.world().getTileEntity(x, y, z);

		cpt.setInteger("orientation", tile.orientation);
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		context.world().setBlock(x, y, z, block, meta, 3);

		TileRefineryController tile = (TileRefineryController) context.world().getTileEntity(x, y, z);

		if (tile != null) {
			tile.orientation = cpt.getInteger("orientation");
		}
	}

}
