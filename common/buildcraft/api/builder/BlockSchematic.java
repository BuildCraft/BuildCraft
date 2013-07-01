package buildcraft.api.builder;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class BlockSchematic {

	public final String blockName;
	public final int blockId;
	public int blockMeta = 0;
	public NBTTagCompound blockData = null;
	public int x, y, z;

	public BlockSchematic(Block block) {
		this(block.getUnlocalizedName(), block.blockID);
	}

	public BlockSchematic(String blockName) {
		this(blockName, 0); // TODO: Add block id from name
	}

	public BlockSchematic(String blockName, int blockId) {
		this.blockName = blockName;
		this.blockId = blockId;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("blockName", blockName);
		nbt.setByte("blockMeta", (byte) blockMeta);
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setCompoundTag("blockData", blockData);
	}

	public static BlockSchematic readFromNBT(NBTTagCompound nbt) {
		BlockSchematic block = new BlockSchematic(nbt.getString("blockName"));
		block.blockMeta = nbt.getInteger("blockMeta");
		block.x = nbt.getInteger("x");
		block.y = nbt.getInteger("y");
		block.z = nbt.getInteger("z");
		if (nbt.hasKey("blockData")) {
			block.blockData = nbt.getCompoundTag("blockData");
		}
		return block;
	}
}
