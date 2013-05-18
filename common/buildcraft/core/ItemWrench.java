package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.tools.IToolWrench;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {

	public ItemWrench(int i) {
		super(i);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		int blockId = world.getBlockId(x, y, z);
		if (Block.blocksList[blockId].rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side))) {
			player.swingItem();
			return !world.isRemote;
		}
		return false;
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6) {
		return true;
	}
}
