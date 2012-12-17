package buildcraft.core;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {

	public ItemWrench(int i) {
		super(i);
		setCreativeTab(CreativeTabs.tabTools);
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {}
}
