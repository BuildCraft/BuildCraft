package buildcraft.api.builder;

import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class BlockSchematic {

	public final String blockName;
	public int metadata = 0;
	public NBTTagCompound blockData = null;
	public int x, y, z;

	public BlockSchematic(String blockName) {
		this.blockName = blockName;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("blockName", blockName);
		nbt.setByte("metadata", (byte) metadata);
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setCompoundTag("blockData", blockData);
	}

	public static BlockSchematic readFromNBT(NBTTagCompound nbt) {
		BlockSchematic block = new BlockSchematic(nbt.getString("blockName"));
		block.metadata = nbt.getInteger("metadata");
		block.x = nbt.getInteger("x");
		block.y = nbt.getInteger("y");
		block.z = nbt.getInteger("z");
		if (nbt.hasKey("blockData")) {
			block.blockData = nbt.getCompoundTag("blockData");
		}
		return block;
	}
}
