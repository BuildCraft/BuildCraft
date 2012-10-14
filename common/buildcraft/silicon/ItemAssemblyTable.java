package buildcraft.silicon;

import buildcraft.core.proxy.CoreProxy;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemAssemblyTable extends ItemBlock {
	public ItemAssemblyTable(int par1) {
		super(par1);
		setHasSubtypes(true);
	}

	@Override
	public String getItemNameIS(ItemStack par1ItemStack) {
		return par1ItemStack.getItemDamage() == 0 ? "block.assemblyTableBlock" : "block.assemblyWorkbenchBlock";
	}

	@Override
	public int getMetadata(int par1) {
		return par1 > 0 && par1 < 2 ? par1 : 0;
	}
}
