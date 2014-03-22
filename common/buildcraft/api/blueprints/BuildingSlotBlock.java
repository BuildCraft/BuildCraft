package buildcraft.api.blueprints;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class BuildingSlotBlock extends BuildingSlot {

	public int x, y, z;
	public Schematic schematic;

	public enum Mode {
		ClearIfInvalid, Build
	};

	public Mode mode = Mode.Build;

	public Schematic getSchematic () {
		if (schematic == null) {
			return new SchematicMask(false);
		} else {
			return schematic;
		}
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		try {
			getSchematic().writeToWorld(context, x, y, z);

			// Once the schematic has been written, we're going to issue calls
			// to various functions, in particular updating the tile entity.
			// If these calls issue problems, in order to avoid corrupting
			// the world, we're logging the problem and setting the block to
			// air.

			TileEntity e = context.world().getTileEntity(x, y, z);

			if (e != null) {
				e.updateEntity();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			context.world().setBlockToAir(x, y, z);
		}
	}

	@Override
	public void postProcessing (IBuilderContext context) {
		getSchematic().postProcessing(context, x, y, z);
	}

	@Override
	public LinkedList<ItemStack> getRequirements (IBuilderContext context) {
		return getSchematic().getRequirements(context);
	}
}
