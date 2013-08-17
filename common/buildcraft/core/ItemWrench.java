package buildcraft.core;

import buildcraft.api.tools.IToolWrench;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {
	
	private final Set<Block> bannedRotations = new HashSet<Block>();

	public ItemWrench(int i) {
		super(i);
		setFull3D();
		bannedRotations.add(Block.lever);
		bannedRotations.add(Block.stoneButton);
		bannedRotations.add(Block.woodenButton);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if(player.isSneaking())
			return false;
		
		int blockId = world.getBlockId(x, y, z);
		Block block = Block.blocksList[blockId];
		if(bannedRotations.contains(block))
			return false;
		
		if (block != null && block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side))) {
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
		player.swingItem();
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6) {
		return true;
	}
}
