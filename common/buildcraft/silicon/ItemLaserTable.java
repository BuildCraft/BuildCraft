package buildcraft.silicon;

import buildcraft.core.ItemBlockBuildCraft;
import net.minecraft.item.ItemStack;

public class ItemLaserTable extends ItemBlockBuildCraft {
	public ItemLaserTable(int par1) {
		super(par1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return par1ItemStack.getItemDamage() == 0 ? "tile.assemblyTableBlock" : "tile.assemblyWorkbenchBlock";
	}

	@Override
	public int getMetadata(int par1) {
		return par1 == 1 ? 1 : 0;
	}
}
