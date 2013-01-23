package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {

	public ItemWrench(int i) {
		super(i);
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
	{
	    return true;
	}
}
