/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe<PipeTransportItems> implements IStripesPipe {

	@MjBattery(maxCapacity = 1, maxReceivedPerCycle = 1, minimumConsumption = 0)
	private double mjStored = 0;

	public PipeItemsStripes(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (container.getWorldObj().isRemote) {
			return;
		}

		if (mjStored > 0) {
			ForgeDirection o = getOpenOrientation();

			if (o != ForgeDirection.UNKNOWN) {
				Position p = new Position(container.xCoord, container.yCoord,
						container.zCoord, o);
				p.moveForwards(1.0);

				if (!BlockUtil.isUnbreakableBlock(getWorld(), (int) p.x, (int) p.y, (int) p.z)) {
					ArrayList<ItemStack> stacks = getWorld().getBlock(
							(int) p.x, (int) p.y, (int) p.z).getDrops(
							getWorld(),
							(int) p.x,
							(int) p.y,
							(int) p.z,
							getWorld().getBlockMetadata((int) p.x, (int) p.y,
									(int) p.z), 0
					);

					if (stacks != null) {
						for (ItemStack s : stacks) {
							if (s != null) {
								rollbackItem(s, o);
							}
						}
					}

					getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
				}
			}
		}

		mjStored = 0;
	}

	@Override
	public void setPipeContainer(Object newPipe) {
		if (newPipe instanceof Pipe) {
			container.pipe = (Pipe) newPipe;
		}
	}

	@Override
	public void rollbackItem(ItemStack itemStack, ForgeDirection direction) {
		if (itemStack != null && itemStack.stackSize > 0) {
			TravelingItem newItem = TravelingItem.make(
					container.xCoord + 0.5,
					container.yCoord + TransportUtils.getPipeFloorOf(itemStack),
					container.zCoord + 0.5, itemStack);
			transport.injectItem(newItem, direction.getOpposite());
		}
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		Position p = new Position(container.xCoord, container.yCoord, container.zCoord, event.direction);
		p.moveForwards(1.0);

		ItemStack itemStack = event.entity.getEntityItem();

		for (IStripesHandler handler : PipeManager.getStripesHandlers()) {
			if (handler.handleStripesEvent(getWorld(), event.entity.getEntityItem(), p, this)) {
				itemStack.stackSize = 0;
				transport.items.remove(event.item);
				return;
			}
		}

		itemStack.tryPlaceItemIntoWorld(
				CoreProxy.proxy.getBuildCraftPlayer(getWorld()),
				getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f, 0.0f);
	}

	@Override
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.Stripes.ordinal();
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			TileGenericPipe tilePipe = (TileGenericPipe) tile;

			if (tilePipe.pipe instanceof PipeItemsStripes) {
				return false;
			}
		}

		return super.canPipeConnect(tile, side);
	}
}
