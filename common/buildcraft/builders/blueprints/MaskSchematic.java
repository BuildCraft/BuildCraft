package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class MaskSchematic extends Schematic {

	public static MaskSchematic create(NBTTagCompound nbt) {
		return new MaskSchematic();
	}

	public static MaskSchematic create() {
		return new MaskSchematic();
	}

	private MaskSchematic() {
		super(0);
	}

	@Override
	public BlockHandler getHandler() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("schematicType", "mask");
	}
}
