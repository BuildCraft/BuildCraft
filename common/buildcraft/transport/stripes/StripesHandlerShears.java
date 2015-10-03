package buildcraft.transport.stripes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerShears implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemShears;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
						  ForgeDirection direction, ItemStack stack, EntityPlayer player,
						  IStripesActivator activator) {
		Block block = world.getBlock(x, y, z);

		if (block instanceof IShearable) {
			IShearable shearableBlock = (IShearable) block;
			if (shearableBlock.isShearable(stack, world, x, y, z)) {
				world.playSoundEffect(x, y, z, Block.soundTypeGrass.getBreakSound(), 1, 1);
				List<ItemStack> drops = shearableBlock.onSheared(stack, world, x, y, z,
						EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));
				world.setBlockToAir(x, y, z);
				if (stack.attemptDamageItem(1, player.getRNG())) {
					stack.stackSize--;
				}
				if (stack.stackSize > 0) {
					activator.sendItem(stack, direction.getOpposite());
				}
				for (ItemStack dropStack : drops) {
					activator.sendItem(dropStack, direction.getOpposite());
				}
				return true;
			}
		}

		return false;
	}

}
