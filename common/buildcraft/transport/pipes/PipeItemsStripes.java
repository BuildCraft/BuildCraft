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

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe <PipeTransportItems> {

	@MjBattery (maxCapacity = 1, maxReceivedPerCycle = 1, minimumConsumption = 0)
	public double mjStored = 0;

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

				ArrayList<ItemStack> stacks = getWorld().getBlock(
						(int) p.x, (int) p.y, (int) p.z)
						.getDrops(
								getWorld(),
								(int) p.x,
								(int) p.y,
								(int) p.z,
								getWorld().getBlockMetadata((int) p.x,
										(int) p.y, (int) p.z), 0);

				if (stacks != null) {
					for (ItemStack s : stacks) {
						if (s != null) {
							TravelingItem newItem = TravelingItem.make(
									container.xCoord + 0.5, container.yCoord
											+ TransportUtils.getPipeFloorOf(s),
									container.zCoord + 0.5, s);

							transport.injectItem(newItem, o.getOpposite());
						}
					}
				}

				getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
			}
		}

		mjStored = 0;
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, event.direction);
		Position from = new Position (p);
		p.moveForwards(1.0);

		if (convertPipe(transport, event.item)) {
			BuildCraftTransport.pipeItemsStripes.onItemUse(new ItemStack(
					BuildCraftTransport.pipeItemsStripes), CoreProxy
					.proxy.getBuildCraftPlayer(getWorld()), getWorld(), (int) p.x,
					(int) p.y, (int) p.z, 1, 0, 0, 0);
		} else if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) == Blocks.air) {
			event.entity.getEntityItem().tryPlaceItemIntoWorld(
					CoreProxy.proxy.getBuildCraftPlayer(getWorld()),
					getWorld(), (int) p.x, (int) p.y - 1, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
		} else {
			event.entity.getEntityItem().tryPlaceItemIntoWorld(
					CoreProxy.proxy.getBuildCraftPlayer(getWorld()),
					getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean convertPipe(PipeTransportItems pipe, TravelingItem item) {
		if (item.getItemStack().getItem() instanceof ItemPipe) {
			if (!(item.getItemStack().getItem() == BuildCraftTransport.pipeItemsStripes)) {
				Pipe newPipe = BlockGenericPipe.createPipe(item.getItemStack().getItem());
				newPipe.setTile(this.container);
				this.container.pipe = newPipe;

				item.getItemStack().stackSize--;

				if (item.getItemStack().stackSize <= 0) {
					((PipeTransportItems) newPipe.transport).items.remove(item);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return  PipeIconProvider.TYPE.Stripes.ordinal();
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
