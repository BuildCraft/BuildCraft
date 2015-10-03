package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.crops.CropManager;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerPlant implements IStripesHandler {
	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return CropManager.isSeed(stack);
	}

	@Override
	public boolean handle(World world, int x, int y, int z, ForgeDirection direction,
						  ItemStack stack, EntityPlayer player, IStripesActivator activator) {
		if (CropManager.canSustainPlant(world, stack, x, y - 1, z)) {
			if (CropManager.plantCrop(world, player, stack, x, y - 1, z)) {
				if (stack.stackSize > 0) {
					activator.sendItem(stack, direction.getOpposite());
				}
				return true;
			}
		}
		return false;
	}
}
