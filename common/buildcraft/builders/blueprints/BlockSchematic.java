package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class BlockSchematic extends Schematic {

	public static BlockSchematic create(NBTTagCompound nbt) {
		return null;
	}

	public static BlockSchematic create(Block block) {
		return new BlockSchematic(block);
	}
	public final Block block;

	private BlockSchematic(Block block) {
		super(block.blockID);
		this.block = block;
	}

	private BlockSchematic(String nbt) {
//		String blockName = nbt.getString("blockName");
		this((Block) null); // TODO: Add block from name code
	}

	@Override
	public BlockHandler getHandler() {
		return BlockHandler.get(block);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("schematicType", "block");
		nbt.setString("blockName", block.getUnlocalizedName());
	}
}
