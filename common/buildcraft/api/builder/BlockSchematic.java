package buildcraft.api.builder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class BlockSchematic {

	public final String blockName;
	public int metadata = 0;
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public NBTTagCompound blockData = null;

	public BlockSchematic(String blockName) {
		this.blockName = blockName;
	}
}
