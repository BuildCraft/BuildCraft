package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerDispenser implements IStripesHandler {
	public static final List<Object> items = new ArrayList<Object>();

	public class Source implements IBlockSource {
		private final World world;
		private final int x, y, z;
		private final ForgeDirection side;

		public Source(World world, int x, int y, int z, ForgeDirection side) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.side = side;
		}

		@Override
		public double getX() {
			return (double) x + 0.5D;
		}

		@Override
		public double getY() {
			return (double) y + 0.5D;
		}

		@Override
		public double getZ() {
			return (double) z + 0.5D;
		}

		@Override
		public int getXInt() {
			return x;
		}

		@Override
		public int getYInt() {
			return y;
		}

		@Override
		public int getZInt() {
			return z;
		}

		@Override
		public int getBlockMetadata() {
			return side.ordinal();
		}

		@Override
		public TileEntity getBlockTileEntity() {
			return world.getTileEntity(x, y, z);
		}

		@Override
		public World getWorld() {
			return world;
		}
	}

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		if (items.contains(stack.getItem())) {
			return true;
		}

		Class<?> c = stack.getItem().getClass();
		while (c != Item.class) {
			if (items.contains(c)) {
				return true;
			}
			c = c.getSuperclass();
		}
		return false;
	}

	@Override
	public boolean handle(World world, int x, int y, int z, ForgeDirection direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
		Position origin = new Position(x, y, z, direction);
		origin.moveBackwards(1.0D);

		IBlockSource source = new Source(world, (int) origin.x, (int) origin.y, (int) origin.z, direction);
		IBehaviorDispenseItem behaviour = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(stack.getItem());
		if (behaviour != null) {
			ItemStack output = behaviour.dispense(source, stack.copy());
			if (output.stackSize > 0) {
				activator.sendItem(output, direction.getOpposite());
			}
			return true;
		}
		return false;
	}
}
