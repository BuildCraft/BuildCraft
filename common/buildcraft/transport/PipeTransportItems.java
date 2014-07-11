/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.Position;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.MathUtils;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeTransportItems extends PipeTransport {

	public static final int MAX_PIPE_STACKS = 64;
	public static final int MAX_PIPE_ITEMS = 1024;
	public boolean allowBouncing = false;
	public final TravelerSet items = new TravelerSet(this);

	@Override
	public PipeType getPipeType() {
		return PipeType.ITEM;
	}

	public void readjustSpeed(TravelingItem item) {
		PipeEventItem.AdjustSpeed event = new PipeEventItem.AdjustSpeed(item);
		container.pipe.handlePipeEvent(event);
		if (!event.handled) {
			defaultReajustSpeed(item);
		}
	}

	public void defaultReajustSpeed(TravelingItem item) {
		float speed = item.getSpeed();

		if (speed > TransportConstants.PIPE_NORMAL_SPEED) {
			speed -= TransportConstants.PIPE_NORMAL_SPEED;
		}

		if (speed < TransportConstants.PIPE_NORMAL_SPEED) {
			speed = TransportConstants.PIPE_NORMAL_SPEED;
		}

		item.setSpeed(speed);
	}

	private void readjustPosition(TravelingItem item) {
		double x = MathUtils.clamp(item.xCoord, container.xCoord + 0.01, container.xCoord + 0.99);
		double y = MathUtils.clamp(item.yCoord, container.yCoord + 0.01, container.yCoord + 0.99);
		double z = MathUtils.clamp(item.zCoord, container.zCoord + 0.01, container.zCoord + 0.99);

		if (item.input != ForgeDirection.UP && item.input != ForgeDirection.DOWN) {
			y = container.yCoord + TransportUtils.getPipeFloorOf(item.getItemStack());
		}

		item.setPosition(x, y, z);
	}

	public void injectItem(TravelingItem item, ForgeDirection inputOrientation) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		}

		item.reset();
		item.input = inputOrientation;

		readjustSpeed(item);
		readjustPosition(item);


		if (!container.getWorldObj().isRemote) {
			item.output = resolveDestination(item);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		container.pipe.handlePipeEvent(event);
		if (event.cancelled) {
			return;
		}

		items.add(item);

		if (!container.getWorldObj().isRemote) {
			sendTravelerPacket(item, false);

			if (items.size() > BuildCraftTransport.groupItemsTrigger) {
				groupEntities();
			}

			if (items.size() > MAX_PIPE_STACKS) {
				BCLog.logger.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many stacks: %d", container.xCoord, container.yCoord, container.zCoord, items.size()));
				destroyPipe();
				return;
			}

			int numItems = 0;
			for (TravelingItem travellingItem : items) {
				ItemStack stack = travellingItem.getItemStack();
				if (stack != null && stack.stackSize > 0) {
					numItems += stack.stackSize;
				}
			}

			if (numItems > MAX_PIPE_ITEMS) {
				BCLog.logger.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many items: %d", container.xCoord, container.yCoord, container.zCoord, numItems));
				destroyPipe();
			}
		}
	}

	private void destroyPipe() {
		BlockUtil.explodeBlock(container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
		container.getWorldObj().setBlockToAir(container.xCoord, container.yCoord, container.zCoord);
	}

	/**
	 * Bounces the item back into the pipe without changing the items map.
	 *
	 * @param item
	 */
	protected void reverseItem(TravelingItem item) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		}

		item.toCenter = true;
		item.input = item.output.getOpposite();

		readjustSpeed(item);
		readjustPosition(item);

		if (!container.getWorldObj().isRemote) {
			item.output = resolveDestination(item);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		container.pipe.handlePipeEvent(event);
		if (event.cancelled) {
			return;
		}

		items.unscheduleRemoval(item);

		if (!container.getWorldObj().isRemote) {
			sendTravelerPacket(item, true);
		}
	}

	public ForgeDirection resolveDestination(TravelingItem data) {
		List<ForgeDirection> validDestinations = getPossibleMovements(data);

		if (validDestinations.isEmpty()) {
			return ForgeDirection.UNKNOWN;
		}

		return validDestinations.get(0);
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent
	 * implementers of IPipeEntry or TileEntityChest.
	 */
	public List<ForgeDirection> getPossibleMovements(TravelingItem item) {
		LinkedList<ForgeDirection> result = new LinkedList<ForgeDirection>();

		item.blacklist.add(item.input.getOpposite());

		EnumSet<ForgeDirection> sides = EnumSet.complementOf(item.blacklist);
		sides.remove(ForgeDirection.UNKNOWN);

		for (ForgeDirection o : sides) {
			if (container.pipe.outputOpen(o) && canReceivePipeObjects(o, item)) {
				result.add(o);
			}
		}

		PipeEventItem.FindDest event = new PipeEventItem.FindDest(item, result);
		container.pipe.handlePipeEvent(event);

		if (allowBouncing && result.isEmpty()) {
			if (canReceivePipeObjects(item.input.getOpposite(), item)) {
				result.add(item.input.getOpposite());
			}
		}

		Collections.shuffle(result);

		return result;
	}

	public boolean canReceivePipeObjects(ForgeDirection o, TravelingItem item) {
		TileEntity entity = container.getTile(o);

		if (!container.isPipeConnected(o)) {
			return false;
		}

		if (entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) entity;

			return pipe.pipe.transport instanceof PipeTransportItems;
		} else if (entity instanceof IInventory && item.getInsertionHandler().canInsertItem(item, (IInventory) entity)) {
			if (Transactor.getTransactorFor(entity).add(item.getItemStack(), o.getOpposite(), false).stackSize > 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void updateEntity() {
		moveSolids();
	}

	private void moveSolids() {
		items.flush();

		if (!container.getWorldObj().isRemote) {
			items.purgeCorruptedItems();
		}

		items.iterating = true;
		for (TravelingItem item : items) {
			if (item.getContainer() != this.container) {
				items.scheduleRemoval(item);
				continue;
			}

			Position motion = new Position(0, 0, 0, item.toCenter ? item.input : item.output);
			motion.moveForwards(item.getSpeed());

			item.movePosition(motion.x, motion.y, motion.z);

			if ((item.toCenter && middleReached(item)) || outOfBounds(item)) {
				item.toCenter = false;

				// Reajusting to the middle
				item.setPosition(container.xCoord + 0.5, container.yCoord + TransportUtils.getPipeFloorOf(item.getItemStack()), container.zCoord + 0.5);

				if (item.output == ForgeDirection.UNKNOWN) {
					if (items.scheduleRemoval(item)) {
						dropItem(item);
					}
				} else {
					PipeEventItem.ReachedCenter event = new PipeEventItem.ReachedCenter(item);
					container.pipe.handlePipeEvent(event);
				}

			} else if (!item.toCenter && endReached(item)) {
				TileEntity tile = container.getTile(item.output);

				PipeEventItem.ReachedEnd event = new PipeEventItem.ReachedEnd(item, tile);
				container.pipe.handlePipeEvent(event);
				boolean handleItem = !event.handled;

				// If the item has not been scheduled to removal by the hook
				if (handleItem && items.scheduleRemoval(item)) {
					handleTileReached(item, tile);
				}

			}
		}
		items.iterating = false;
		items.flush();
	}

	private boolean passToNextPipe(TravelingItem item, TileEntity tile) {
		if (tile instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) tile;
			if (BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportItems) {
				((PipeTransportItems) pipe.pipe.transport).injectItem(item, item.output);
				return true;
			}
		}
		return false;
	}

	private void handleTileReached(TravelingItem item, TileEntity tile) {
		if (passToNextPipe(item, tile)) {
			// NOOP
		} else if (tile instanceof IInventory) {
			if (!container.getWorldObj().isRemote) {
				if (item.getInsertionHandler().canInsertItem(item, (IInventory) tile)) {
					ItemStack added = Transactor.getTransactorFor(tile).add(item.getItemStack(), item.output.getOpposite(), true);
					item.getItemStack().stackSize -= added.stackSize;
				}

				if (item.getItemStack().stackSize > 0) {
					reverseItem(item);
				}
			}
		} else {
			dropItem(item);
		}
	}

	private void dropItem(TravelingItem item) {
		if (container.getWorldObj().isRemote) {
			return;
		}

		PipeEventItem.DropItem event = new PipeEventItem.DropItem(item, item.toEntityItem());
		container.pipe.handlePipeEvent(event);

		if (event.entity == null) {
			return;
		}

		final EntityItem entity = event.entity;
		ForgeDirection direction = item.input;
		entity.setPosition(entity.posX + direction.offsetX * 0.5d,
				entity.posY + direction.offsetY * 0.5d,
				entity.posZ + direction.offsetZ * 0.5d);

		entity.motionX = direction.offsetX * item.speed * 5
				+ getWorld().rand.nextGaussian() * 0.1d;
		entity.motionY = direction.offsetY * item.speed * 5
				+ getWorld().rand.nextGaussian() * 0.1d;
		entity.motionZ = direction.offsetZ * item.speed * 5
				+ getWorld().rand.nextGaussian() * 0.1d;

		container.getWorldObj().spawnEntityInWorld(entity);
	}

	protected boolean middleReached(TravelingItem item) {
		float middleLimit = item.getSpeed() * 1.01F;
		return Math.abs(container.xCoord + 0.5 - item.xCoord) < middleLimit
				&& Math.abs(container.yCoord + TransportUtils.getPipeFloorOf(item.getItemStack()) - item.yCoord) < middleLimit
				&& Math
						.abs(container.zCoord + 0.5 - item.zCoord) < middleLimit;
	}

	protected boolean endReached(TravelingItem item) {
		return item.xCoord > container.xCoord + 1 || item.xCoord < container.xCoord || item.yCoord > container.yCoord + 1 || item.yCoord < container.yCoord || item.zCoord > container.zCoord + 1 || item.zCoord < container.zCoord;
	}

	protected boolean outOfBounds(TravelingItem item) {
		return item.xCoord > container.xCoord + 2 || item.xCoord < container.xCoord - 1 || item.yCoord > container.yCoord + 2 || item.yCoord < container.yCoord - 1 || item.zCoord > container.zCoord + 2 || item.zCoord < container.zCoord - 1;
	}

	public Position getPosition() {
		return new Position(container.xCoord, container.yCoord, container.zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList nbttaglist = nbt.getTagList("travelingEntities", Constants.NBT.TAG_COMPOUND);

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			try {
				NBTTagCompound dataTag = nbttaglist.getCompoundTagAt(j);

				TravelingItem item = TravelingItem.make(dataTag);

				if (item.isCorrupted()) {
					continue;
				}

				items.scheduleLoad(item);
			} catch (Throwable t) {
				// It may be the case that entities cannot be reloaded between
				// two versions - ignore these errors.
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList nbttaglist = new NBTTagList();

		for (TravelingItem item : items) {
			NBTTagCompound dataTag = new NBTTagCompound();
			nbttaglist.appendTag(dataTag);
			item.writeToNBT(dataTag);
		}

		nbt.setTag("travelingEntities", nbttaglist);
	}

	protected void doWork() {
	}

	/**
	 * Handles a packet describing a stack of items inside a pipe.
	 *
	 * @param packet
	 */
	public void handleTravelerPacket(PacketPipeTransportTraveler packet) {
		TravelingItem item = TravelingItem.clientCache.get(packet.getTravelingEntityId());

		if (item == null) {
			item = TravelingItem.make(packet.getTravelingEntityId());
		}

		if (item.getContainer() != container) {
			items.add(item);
		}

		if (packet.forceStackRefresh() || item.getItemStack() == null) {
			BuildCraftTransport.instance.sendToServer(new PacketPipeTransportItemStackRequest(packet.getTravelingEntityId()));
		}

		item.setPosition(packet.getItemX(), packet.getItemY(), packet.getItemZ());

		item.setSpeed(packet.getSpeed());

		item.toCenter = true;
		item.input = packet.getInputOrientation();
		item.output = packet.getOutputOrientation();
		item.color = packet.getColor();

	}

	private void sendTravelerPacket(TravelingItem data, boolean forceStackRefresh) {
		PacketPipeTransportTraveler packet = new PacketPipeTransportTraveler(data, forceStackRefresh);
		BuildCraftTransport.instance.sendToPlayers(packet, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
	}

	public int getNumberOfStacks() {
		return items.size();
	}

	public int getNumberOfItems() {
		int num = 0;
		for (TravelingItem item : items) {
			if (item.getItemStack() == null) {
				continue;
			}
			num += item.getItemStack().stackSize;
		}
		return num;
	}

	protected void neighborChange() {
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportItems)) {
				return false;
			}
		}

		if (tile instanceof ISidedInventory) {
			int[] slots = ((ISidedInventory) tile).getAccessibleSlotsFromSide(side.getOpposite().ordinal());
			return slots != null && slots.length > 0;
		}

		return tile instanceof TileGenericPipe || (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0)
				|| (tile instanceof IMachine && ((IMachine) tile).manageSolids());
	}

	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}

	/**
	 * Group all items that are similar, that is to say same dmg, same id, same
	 * nbt and no contribution controlling them
	 */
	public void groupEntities() {
		for (TravelingItem item : items) {
			if (item.isCorrupted()) {
				continue;
			}
			for (TravelingItem otherItem : items) {
				if (item.tryMergeInto(otherItem)) {
					break;
				}
			}
		}
	}

	@Override
	public void dropContents() {
		groupEntities();

		for (TravelingItem item : items) {
			if (!item.isCorrupted()) {
				container.pipe.dropItem(item.getItemStack());
			}
		}

		items.clear();
	}

	@Override
	public boolean delveIntoUnloadedChunks() {
		return true;
	}
}
