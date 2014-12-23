/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.utils.BlockUtils;
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
		double x = MathUtils.clamp(item.xCoord, container.getPos().getX() + 0.01, container.getPos().getX() + 0.99);
		double y = MathUtils.clamp(item.yCoord, container.getPos().getY() + 0.01, container.getPos().getY() + 0.99);
		double z = MathUtils.clamp(item.zCoord, container.getPos().getZ() + 0.01, container.getPos().getZ() + 0.99);

		if (item.input != EnumFacing.UP && item.input != EnumFacing.DOWN) {
			y = container.getPos().getY() + TransportUtils.getPipeFloorOf(item.getItemStack());
		}

		item.setPosition(x, y, z);
	}

	public void injectItem(TravelingItem item, EnumFacing inputOrientation) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		}

		item.reset();
		item.input = inputOrientation;

		readjustSpeed(item);
		readjustPosition(item);


		if (!container.getWorld().isRemote) {
			item.output = resolveDestination(item);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		container.pipe.handlePipeEvent(event);
		if (event.cancelled) {
			return;
		}

		items.add(item);

		if (!container.getWorld().isRemote) {
			sendTravelerPacket(item, false);

			if (items.size() > BuildCraftTransport.groupItemsTrigger) {
				groupEntities();
			}

			if (items.size() > MAX_PIPE_STACKS) {
				BCLog.logger.log(Level.WARN, String.format("Pipe exploded at %d,%d,%d because it had too many stacks: %d", container.getPos().getX(), container.getPos().getY(), container.getPos().getZ(), items.size()));
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
				BCLog.logger.log(Level.WARN, String.format("Pipe exploded at %d,%d,%d because it had too many items: %d", container.getPos().getX(), container.getPos().getY(), container.getPos().getZ(), numItems));
				destroyPipe();
			}
		}
	}

	private void destroyPipe() {
		BlockUtils.explodeBlock(container.getWorld(), container.getPos());
		container.getWorld().setBlockToAir(container.getPos());
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

		if (!container.getWorld().isRemote) {
			item.output = resolveDestination(item);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		container.pipe.handlePipeEvent(event);
		if (event.cancelled) {
			return;
		}

		items.unscheduleRemoval(item);

		if (!container.getWorld().isRemote) {
			sendTravelerPacket(item, true);
		}
	}

	public EnumFacing resolveDestination(TravelingItem data) {
		List<EnumFacing> validDestinations = getPossibleMovements(data);

		if (validDestinations.isEmpty()) {
			return null;
		}

		return validDestinations.get(0);
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent
	 * implementers of IPipeEntry or TileEntityChest.
	 */
	public List<EnumFacing> getPossibleMovements(TravelingItem item) {
		LinkedList<EnumFacing> result = new LinkedList<EnumFacing>();

		item.blacklist.add(item.input.getOpposite());

		EnumSet<EnumFacing> sides = EnumSet.complementOf(item.blacklist);

		for (EnumFacing o : sides) {
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

	private boolean canReceivePipeObjects(EnumFacing o, TravelingItem item) {
		TileEntity entity = container.getTile(o);

		if (!container.isPipeConnected(o)) {
			return false;
		}

		if (entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) entity;

			//return !pipe.pipe.isClosed() && pipe.pipe.transport instanceof PipeTransportItems;
			return pipe.pipe.inputOpen(o.getOpposite()) && pipe.pipe.transport instanceof PipeTransportItems;
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

		if (!container.getWorld().isRemote) {
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
				item.setPosition(container.getPos().getX() + 0.5, container.getPos().getY() + TransportUtils.getPipeFloorOf(item.getItemStack()), container.getPos().getZ() + 0.5);

				if (item.output == null) {
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
			if (!container.getWorld().isRemote) {
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
		if (container.getWorld().isRemote) {
			return;
		}

		PipeEventItem.DropItem event = new PipeEventItem.DropItem(item, item.toEntityItem());
		container.pipe.handlePipeEvent(event);

		if (event.entity == null) {
			return;
		}

		final EntityItem entity = event.entity;
		EnumFacing direction = item.input;
		entity.setPosition(entity.posX + direction.getFrontOffsetX() * 0.5d,
				entity.posY + direction.getFrontOffsetY() * 0.5d,
				entity.posZ + direction.getFrontOffsetZ() * 0.5d);

		entity.motionX = direction.getFrontOffsetX() * item.speed * 5
				+ getWorld().rand.nextGaussian() * 0.1d;
		entity.motionY = direction.getFrontOffsetY() * item.speed * 5
				+ getWorld().rand.nextGaussian() * 0.1d;
		entity.motionZ = direction.getFrontOffsetZ() * item.speed * 5
				+ getWorld().rand.nextGaussian() * 0.1d;

		container.getWorld().spawnEntityInWorld(entity);
	}

	protected boolean middleReached(TravelingItem item) {
		float middleLimit = item.getSpeed() * 1.01F;
		return Math.abs(container.getPos().getX() + 0.5 - item.xCoord) < middleLimit
				&& Math.abs(container.getPos().getY() + TransportUtils.getPipeFloorOf(item.getItemStack()) - item.yCoord) < middleLimit
				&& Math
						.abs(container.getPos().getZ() + 0.5 - item.zCoord) < middleLimit;
	}

	protected boolean endReached(TravelingItem item) {
		return item.xCoord > container.getPos().getX() + 1 || item.xCoord < container.getPos().getX() || item.yCoord > container.getPos().getY() + 1 || item.yCoord < container.getPos().getY() || item.zCoord > container.getPos().getZ() + 1 || item.zCoord < container.getPos().getZ();
	}

	protected boolean outOfBounds(TravelingItem item) {
		return item.xCoord > container.getPos().getX() + 2 || item.xCoord < container.getPos().getX() - 1 || item.yCoord > container.getPos().getY() + 2 || item.yCoord < container.getPos().getY() - 1 || item.zCoord > container.getPos().getZ() + 2 || item.zCoord < container.getPos().getZ() - 1;
	}

	public Position getPosition() {
		return new Position(container.getPos().getX(), container.getPos().getY(), container.getPos().getZ());
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
		BuildCraftTransport.instance.sendToPlayersNear(packet, container, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
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
	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		if (tile instanceof TileGenericPipe) {
			Pipe<?> pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportItems)) {
				return false;
			}
		}

		if (tile instanceof ISidedInventory) {
			int[] slots = ((ISidedInventory) tile).getSlotsForFace(side.getOpposite());
			return slots != null && slots.length > 0;
		}

		return tile instanceof TileGenericPipe || (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0);
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
	
	public List<ItemStack> getDroppedItems() {
		groupEntities();

		ArrayList<ItemStack> itemsDropped = new ArrayList<ItemStack>(items.size());

		for (TravelingItem item : items) {
			if (!item.isCorrupted()) {
				itemsDropped.add(item.getItemStack());
			}
		}

		return itemsDropped;
	}

	@Override
	public boolean delveIntoUnloadedChunks() {
		return true;
	}
}
