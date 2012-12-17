package buildcraft.core;

import buildcraft.core.utils.StringUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockBuildCraft extends ItemBlock {

	protected String name;

	public ItemBlockBuildCraft(int id, String name) {
		super(id);
		this.name = name;
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.localize(getItemNameIS(itemstack));
	}

}
