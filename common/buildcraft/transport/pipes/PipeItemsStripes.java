/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.events.PipeEventItem;

public class PipeItemsStripes extends Pipe {

	public PipeItemsStripes(Item item) {
		super(new PipeTransportItems(), item);

		//((PipeTransportItems) transport).travelHook = this;
	}

	/*@Override
	public void doWork() {
		if (powerProvider.useEnergy(1, 1, true) == 1) {
			ForgeDirection o = getOpenOrientation();

			if (o != ForgeDirection.Unknown) {
				Position p = new Position(xCoord, yCoord, zCoord, o);
				p.moveForwards(1.0);

				ArrayList<ItemStack> stacks = BuildCraftBlockUtil
						.getItemStackFromBlock(worldObj, (int) p.x, (int) p.y,
								(int) p.z);

				if (stacks != null) {
					for (ItemStack s : stacks) {
						if (s != null) {
							IPipedItem newItem = new EntityPassiveItem(
									worldObj, xCoord + 0.5, yCoord
											+ Utils.getPipeFloorOf(s),
									zCoord + 0.5, s);

							this.container.entityEntering(newItem, o.reverse());
						}
					}
				}

				worldObj.setBlock((int) p.x, (int) p.y, (int) p.z, 0);
			}
		}

	}*/

	public void eventHandler(PipeEventItem.DropItem event) {
		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, event.direction);
		Position from = new Position (p);
		p.moveForwards(1.0);

		if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) == Blocks.air) {
			event.entity.getEntityItem().tryPlaceItemIntoWorld(
					CoreProxy.proxy.getBuildCraftPlayer(getWorld()),
					getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
		}

		/*if (convertPipe(pipe, data)) {
			BuildCraftTransport.pipeItemsStipes.onItemUse(new ItemStack(
					BuildCraftTransport.pipeItemsStipes), CoreProxy
					.getBuildCraftPlayer(worldObj), worldObj, (int) p.x,
					(int) p.y - 1, (int) p.z, 1);
		} else else {
			data.item
					.getItemStack()
					.getItem()
					.tryPlaceIntoWorld(data.item.getItemStack(),
							CoreProxy.getBuildCraftPlayer(worldObj), worldObj,
							(int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
							0.0f);
		}*/
	}

	/*@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {
		convertPipe(pipe, data);
	}*/

	/*@SuppressWarnings("unchecked")
	public boolean convertPipe(PipeTransportItems pipe, EntityItem data) {
		if (data.getEntityItem().getItem() instanceof ItemPipe) {
			if (!(data.getEntityItem().getItem() == BuildCraftTransport.pipeItemsStripes)) {

				Pipe newPipe = BlockGenericPipe.createPipe(data.getEntityItem().getItem());
				newPipe.setTile(this.container);
				this.container.pipe = newPipe;
				((PipeTransportItems) newPipe.transport).travelingEntities = (TreeMap<Integer, EntityData>) pipe.travelingEntities
						.clone();

				data.getEntityItem().stackSize--;

				if (data.getEntityItem().stackSize <= 0) {
					((PipeTransportItems) newPipe.transport).travelingEntities
							.remove(data.getEntityId());
				}

				pipe.scheduleRemoval(data.item);

				return true;
			}
		}

		return false;
	}*/

	/*
	@Override
	public void endReached(PipeTransportItems pipe, EntityData data,
			TileEntity tile) {

	}*/

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
