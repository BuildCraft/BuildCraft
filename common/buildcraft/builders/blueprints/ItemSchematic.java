package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class ItemSchematic extends Schematic {

	public static ItemSchematic create(NBTTagCompound nbt) {
		return null;
	}

	public static ItemSchematic create(Item item) {
		return new ItemSchematic(item);
	}
	public final Item item;

	private ItemSchematic(Item item) {
		super(item.itemID);
		this.item = item;
	}

	private ItemSchematic(String itemName) {
//		String blockName = nbt.getString("blockName");
		this((Item) null); // TODO: Add item from name code
	}

	@Override
	public BlockHandler getHandler() {
		return BlockHandler.get(item);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("schematicType", "item");
		nbt.setString("itemName", item.getUnlocalizedName());
	}
}
