/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;

import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.statements.ActionPipeDirection;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe<PipeTransportItems> implements IEnergyHandler, IStripesPipe {
	private RFBattery battery = new RFBattery(320 * 50, 640, 0);
	private ForgeDirection actionDir = ForgeDirection.UNKNOWN;

	public PipeItemsStripes(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (container.getWorldObj().isRemote) {
			return;
		}

		if (battery.getEnergyStored() >= 10) {
			ForgeDirection o = actionDir;
			if (o == ForgeDirection.UNKNOWN) {
				o = getOpenOrientation();
			}

			if (o != ForgeDirection.UNKNOWN) {
				Position p = new Position(container.xCoord, container.yCoord,
						container.zCoord, o);
				p.moveForwards(1.0);

				if (!BlockUtils.isUnbreakableBlock(getWorld(), (int) p.x, (int) p.y, (int) p.z)) {
					Block block = getWorld().getBlock((int) p.x, (int) p.y, (int) p.z);
					int metadata = getWorld().getBlockMetadata((int) p.x, (int) p.y, (int) p.z);

					if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
						return;
					}

					ItemStack stack = new ItemStack(block, 1, metadata);
					EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(),
							(int) p.x, (int) p.y, (int) p.z).get();

					if (battery.useEnergy(10, 10, false) != 10) {
						return;
					}

					for (IStripesHandler handler : PipeManager.stripesHandlers) {
						if (handler.getType() == StripesHandlerType.BLOCK_BREAK
								&& handler.shouldHandle(stack)) {
							if (handler.handle(getWorld(), (int) p.x, (int) p.y, (int) p.z,
									o, stack, player, this)) {
								return;
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

			return;
		}
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		if (container.getWorldObj().isRemote) {
			return;
		}

		ForgeDirection direction = actionDir;
		if (direction == ForgeDirection.UNKNOWN) {
			direction = event.direction;
		}

		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, direction);
		p.moveForwards(1.0);

		ItemStack stack = event.entity.getEntityItem();
		EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(),
				(int) p.x, (int) p.y, (int) p.z).get();

		switch (direction) {
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
						direction, stack, player, this)) {
					event.entity = null;
					return;
				}
			}
		}
	}

	@Override
	public void dropItem(ItemStack itemStack, ForgeDirection direction) {
		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, direction);
		p.moveForwards(1.0);

		InvUtils.dropItems(getWorld(), itemStack, (int) p.x, (int) p.y, (int) p.z);
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		LinkedList<IActionInternal> action = super.getActions();
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (!container.isPipeConnected(direction)) {
				action.add(BuildCraftTransport.actionPipeDirection[direction.ordinal()]);
			}
		}
		return action;
	}

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		super.actionsActivated(actions);

		actionDir = ForgeDirection.UNKNOWN;

		for (StatementSlot action : actions) {
			if (action.statement instanceof ActionPipeDirection) {
				actionDir = ((ActionPipeDirection) action.statement).direction;
				break;
			}
		}
	}

	@Override
	public void sendItem(ItemStack itemStack, ForgeDirection direction) {
		Position pos = new Position(container.xCoord + 0.5,
				container.yCoord + TransportUtils.getPipeFloorOf(itemStack),
				container.zCoord + 0.5, direction);
		pos.moveBackwards(0.25D);

		TravelingItem newItem = TravelingItem.make(pos.x, pos.y, pos.z, itemStack);

		transport.injectItem(newItem, direction);
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
		if (tile instanceof IPipeTile) {
			IPipeTile tilePipe = (IPipeTile) tile;

			if (tilePipe.getPipe() instanceof PipeItemsStripes) {
				return false;
			}
		}

		return super.canPipeConnect(tile, side);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
							 boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
							 boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return 10;
	}
}
