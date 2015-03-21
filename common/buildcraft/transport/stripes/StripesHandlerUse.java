package buildcraft.transport.stripes;

import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

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

		boolean done = stack.getItem().onItemUseFirst(stack, player, world,
				(int) target.x, (int) target.y, (int) target.z,
				direction.getOpposite().ordinal(), 0.5F, 0.5F, 0.5F);

		if (!done) {
			done = stack.getItem().onItemUse(stack, player, world,
					(int) target.x, (int) target.y, (int) target.z,
					direction.getOpposite().ordinal(), 0.5F, 0.5F, 0.5F);
		}

		if (stack.stackSize > 0) {
			activator.sendItem(stack, direction.getOpposite());
		}

		return done;
	}

}
