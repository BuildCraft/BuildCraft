package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class BlockSchematic {

	public final Block block;
	public NBTTagCompound blockData = new NBTTagCompound();
	public int x, y, z;

	public BlockSchematic(Block block) {
		this.block = block;
	}

	public BlockSchematic(String blockName) {
		this((Block) null); // TODO: Add block from name code
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("blockName", block.getUnlocalizedName());
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setCompoundTag("blockData", blockData);
	}

	public static BlockSchematic readFromNBT(NBTTagCompound nbt) {
		BlockSchematic block = new BlockSchematic(nbt.getString("blockName"));
		block.x = nbt.getInteger("x");
		block.y = nbt.getInteger("y");
		block.z = nbt.getInteger("z");
		block.blockData = nbt.getCompoundTag("blockData");
		return block;
	}
}
