package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.transport.ItemPipeWire;
import buildcraft.transport.TileGenericPipe;

public class StripesHandlerPipeWires implements IStripesHandler {
	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemPipeWire;
	}

	@Override
	public boolean handle(World world, int x, int y, int z, ForgeDirection direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
		int pipesToTry = 8;
		int pipeWireColor = stack.getItemDamage();

		Position p = new Position(x, y, z);
		p.orientation = direction;

		while (pipesToTry > 0) {
			p.moveBackwards(1.0);

			TileEntity tile = world.getTileEntity((int) p.x, (int) p.y, (int) p.z);
			if (tile instanceof TileGenericPipe) {
				TileGenericPipe pipeTile = (TileGenericPipe) tile;

				if (!pipeTile.pipe.wireSet[pipeWireColor]) {
					pipeTile.pipe.wireSet[pipeWireColor] = true;
					pipeTile.pipe.wireSignalStrength[pipeWireColor] = 0;

					pipeTile.pipe.updateSignalState();
					pipeTile.scheduleRenderUpdate();
					world.notifyBlocksOfNeighborChange(pipeTile.xCoord, pipeTile.yCoord, pipeTile.zCoord, pipeTile.getBlock());
					return true;
				} else {
					pipesToTry--;
					continue;
				}
			} else {
				// Not a pipe, don't follow chain
				break;
			}
		}

		return false;
	}
}
