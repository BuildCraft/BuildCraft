package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;

public class StripesHandlerArrow implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}
	
	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() == Items.arrow;
	}

	@Override
	public boolean handle(World world, BlockPos pos,
			EnumFacing direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		stack.stackSize--;

		EntityArrow entityArrow = new EntityArrow(world, player, 0);
		entityArrow.setPosition(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
		entityArrow.setDamage(3);
		entityArrow.setKnockbackStrength(1);
		entityArrow.motionX = direction.getFrontOffsetX() * 1.8d + world.rand.nextGaussian() * 0.007499999832361937D;
		entityArrow.motionY = direction.getFrontOffsetY() * 1.8d + world.rand.nextGaussian() * 0.007499999832361937D;
		entityArrow.motionZ = direction.getFrontOffsetZ() * 1.8d + world.rand.nextGaussian() * 0.007499999832361937D;
		world.spawnEntityInWorld(entityArrow);

		return true;
	}

}
