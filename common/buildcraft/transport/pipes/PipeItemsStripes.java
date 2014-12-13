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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.util.EnumFacing;
import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe<PipeTransportItems> implements IEnergyHandler, IStripesPipe {
	public PipeItemsStripes(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (container.getWorld().isRemote) {
			return;
		}
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		if (container.getWorld().isRemote) {
			return;
		}
		
		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, event.direction);
		p.moveForwards(1.0);

		ItemStack stack = event.entity.getEntityItem();
		EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(),
				(int) p.x, (int) p.y, (int) p.z).get();
		
		switch (event.direction) {
			case DOWN:
				player.rotationPitch = 90;
				player.rotationYaw = 0;
				break;
			case UP:
				player.rotationPitch = 270;
				player.rotationYaw = 0;
				break;
			case NORTH:
				player.rotationPitch = 0;
				player.rotationYaw = 180;
				break;
			case SOUTH:
				player.rotationPitch = 0;
				player.rotationYaw = 0;
				break;
			case WEST:
				player.rotationPitch = 0;
				player.rotationYaw = 90;
				break;
			case EAST:
				player.rotationPitch = 0;
				player.rotationYaw = 270;
				break;
			case UNKNOWN:
				break;
		}
		
		/**
		 * Check if there's a handler for this item type.
		 */
		
		for (IStripesHandler handler : PipeManager.stripesHandlers) {
			if (handler.getType() == StripesHandlerType.ITEM_USE
					&& handler.shouldHandle(stack)) {
				if (handler.handle(getWorld(), (int) p.x, (int) p.y, (int) p.z,
						event.direction, stack, player, this)) {
					return;
				}
			}
		}

		/**
		 * Special, generic actions not handled by the handler.
		 */
		
		if (convertPipe(transport, event.item)) {
			int moves = 0;
			while (stack.stackSize > 0) {
				if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) != Blocks.air) {
					break;
				}
				stack.getItem().onItemUse(new ItemStack(stack.getItem(), 1, stack.getItemDamage()),
						player, getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0, 0, 0
					);
				stack.stackSize--;
				p.moveForwards(1.0);
				moves++;
			}
			if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) != Blocks.air) {
				p.moveBackwards(1.0);
				stack.stackSize++;
				getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
			}
			
			BuildCraftTransport.pipeItemsStripes.onItemUse(new ItemStack(
					BuildCraftTransport.pipeItemsStripes, 1, this.container.getItemMetadata()), player, getWorld(), (int) p.x,
					(int) p.y, (int) p.z, 1, 0, 0, 0
				);
			this.container.initializeFromItemMetadata(stack.getItemDamage() - 1);
			
			if (stack.stackSize > 0) {
				TileEntity targetTile = getWorld().getTileEntity((int) p.x, (int) p.y, (int) p.z);
				if (targetTile instanceof TileGenericPipe) {
					TravelingItem newItem = TravelingItem.make(
							container.xCoord + 0.5,
							container.yCoord + TransportUtils.getPipeFloorOf(
									new ItemStack(BuildCraftTransport.pipeItemsStripes)),
							container.zCoord + 0.5, stack.copy());
					((PipeTransportItems) ((TileGenericPipe) targetTile).pipe.transport).injectItem(newItem, event.direction.getOpposite());

					stack.stackSize = 0;
				}
			}
		} else if (stack.getItem() instanceof ItemBlock) {
			if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) == Blocks.air) {
				stack.tryPlaceItemIntoWorld(
						player,
					getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
			}
		} else {
			stack.tryPlaceItemIntoWorld(
					player,
					getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
		}
	}
	
	@Override
	public void dropItem(ItemStack itemStack, EnumFacing direction) {
		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, direction);
		p.moveForwards(1.0);

		itemStack.tryPlaceItemIntoWorld(CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get(),
				getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
				0.0f);
	}
	
	@Override
	public void sendItem(ItemStack itemStack, EnumFacing direction) {
		TravelingItem newItem = TravelingItem.make(
				container.xCoord + 0.5,
				container.yCoord + TransportUtils.getPipeFloorOf(itemStack),
				container.zCoord + 0.5, itemStack);
		transport.injectItem(newItem, direction);
	}

	private boolean convertPipe(PipeTransportItems pipe, TravelingItem item) {
		if (item.getItemStack().getItem() instanceof ItemPipe) {
			if (!(item.getItemStack().getItem() == BuildCraftTransport.pipeItemsStripes)) {
				Pipe<?> newPipe = BlockGenericPipe.createPipe(item.getItemStack().getItem());
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
	public int getIconIndex(EnumFacing direction) {
		return PipeIconProvider.TYPE.Stripes.ordinal();
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		if (tile instanceof TileGenericPipe) {
			TileGenericPipe tilePipe = (TileGenericPipe) tile;

			if (tilePipe.pipe instanceof PipeItemsStripes) {
				return false;
			}
		}

		return super.canPipeConnect(tile, side);
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive,
			boolean simulate) {
		if (maxReceive == 0) {
			return 0;
		} else if (simulate) {
			return maxReceive;
		}

		EnumFacing o = getOpenOrientation();

		if (o != EnumFacing.UNKNOWN) {
			Position p = new Position(container.xCoord, container.yCoord,
					container.zCoord, o);
			p.moveForwards(1.0);

			if (!BlockUtils.isUnbreakableBlock(getWorld(), (int) p.x, (int) p.y, (int) p.z)) {
				Block block = getWorld().getBlock((int) p.x, (int) p.y, (int) p.z);
				int metadata = getWorld().getBlockMetadata((int) p.x, (int) p.y, (int) p.z);
				
				ItemStack stack = new ItemStack(block, 1, metadata);
				EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(),
						(int) p.x, (int) p.y, (int) p.z).get();
				
				for (IStripesHandler handler : PipeManager.stripesHandlers) {
					if (handler.getType() == StripesHandlerType.BLOCK_BREAK
							&& handler.shouldHandle(stack)) {
						if (handler.handle(getWorld(), (int) p.x, (int) p.y, (int) p.z,
								o, stack, player, this)) {
							return maxReceive;
						}
					}
				}
				
				ArrayList<ItemStack> stacks = block.getDrops(
						getWorld(), (int) p.x, (int) p.y, (int) p.z,
						metadata, 0
				);

				if (stacks != null) {
					for (ItemStack s : stacks) {
						if (s != null) {
							sendItem(s, o.getOpposite());
						}
					}
				}

				getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
			}
		}

		return maxReceive;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return 10;
	}
}
