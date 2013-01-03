package buildcraft.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;

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
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z) {
		return true;
	}
}
