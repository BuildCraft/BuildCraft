package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.BlockUtils;

public class StripesHandlerUse implements IStripesHandler {
	public static final List<Item> items = new ArrayList<Item>();

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return items.contains(stack.getItem());
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
						  ForgeDirection direction, ItemStack stack, EntityPlayer player,
						  IStripesActivator activator) {
		Position target = new Position(x, y, z, direction);
		target.moveForwards(1.0D);

		if (BlockUtils.useItemOnBlock(world, player, stack, MathHelper.floor_double(target.x),
				MathHelper.floor_double(target.y), MathHelper.floor_double(target.z),
				direction.getOpposite())) {
			if (stack.stackSize > 0) {
				activator.sendItem(stack, direction.getOpposite());
			}
			return true;
		}
		return false;
	}

}
