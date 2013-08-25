package buildcraft.core;

import buildcraft.api.tools.IToolWrench;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLever;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {

	private final Set<Class<? extends Block>> shiftRotations = new HashSet<Class<? extends Block>>();

	public ItemWrench(int i) {
		super(i);
		setFull3D();
		shiftRotations.add(BlockLever.class);
		shiftRotations.add(BlockButton.class);
		shiftRotations.add(BlockChest.class);
	}

	private boolean isShiftRotation(Class<? extends Block> cls) {
		for (Class<? extends Block> shift : shiftRotations) {
			if (shift.isAssignableFrom(cls))
				return true;
		}
		return false;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		int blockId = world.getBlockId(x, y, z);
		Block block = Block.blocksList[blockId];
		
		if(block == null)
			return false;

		if (player.isSneaking() != isShiftRotation(block.getClass()))
			return false;

		if (block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side))) {
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
