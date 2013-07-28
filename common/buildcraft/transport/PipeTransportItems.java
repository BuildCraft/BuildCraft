/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;
import buildcraft.transport.network.PacketPipeTransportContent;
import buildcraft.transport.network.PacketPipeTransportNBT;
import buildcraft.transport.network.PacketSimpleId;
import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.HashBiMap;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeTransportItems extends PipeTransport {

	public static final int MAX_PIPE_STACKS = 64;
	public static final int MAX_PIPE_ITEMS = 1024;
	public boolean allowBouncing = false;
	// TODO: generalize the use of this hook in particular for obsidian pipe
	public IItemTravelingHook travelHook;
	public final TravelerSet items = new TravelerSet();

	public class TravelerSet extends ForwardingSet<TravelingItem> {

		private final BiMap<Integer, TravelingItem> delegate = HashBiMap.create();
		private final Set<TravelingItem> toLoad = new HashSet<TravelingItem>();
		private final Set<TravelingItem> toRemove = new HashSet<TravelingItem>();
		private int delay = 0;

		@Override
		protected Set<TravelingItem> delegate() {
			return delegate.values();
		}

		@Override
		public boolean add(TravelingItem item) {
			if (delegate.containsValue(item))
				return false;
			item.setContainer(container);
			delegate.put(item.id, item);
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends TravelingItem> collection) {
			boolean changed = false;
			for (TravelingItem item : collection) {
				changed |= add(item);
			}
			return changed;
		}

		public TravelingItem get(int id) {
			return delegate.get(id);
		}

		private void scheduleLoad(TravelingItem item) {
			delay = 10;
			toLoad.add(item);
		}

		private void performLoad() {
			if (delay > 0) {
				delay--;
				return;
			}
			addAll(toLoad);
			toLoad.clear();
		}

		public boolean scheduleRemoval(TravelingItem item) {
			return toRemove.add(item);
		}

		public boolean unscheduleRemoval(TravelingItem item) {
			return toRemove.remove(item);
		}

		private void performRemoval() {
			removeAll(toRemove);
			toRemove.clear();
		}
	};

	@Override
	public PipeType getPipeType() {
		return PipeType.ITEM;
	}

	public void readjustSpeed(TravelingItem item) {
		if (container.pipe instanceof IPipeTransportItemsHook)
			((IPipeTransportItemsHook) container.pipe).readjustSpeed(item);
		else
			defaultReajustSpeed(item);
	}

	public void defaultReajustSpeed(TravelingItem item) {
		float speed = item.getSpeed();

		if (speed > Utils.pipeNormalSpeed) {
			speed -= Utils.pipeNormalSpeed;
		}

		if (speed < Utils.pipeNormalSpeed) {
			speed = Utils.pipeNormalSpeed;
		}

		item.setSpeed(speed);
	}

	private void readjustPosition(TravelingItem item) {
		double x = item.xCoord;
		double y = item.yCoord;
		double z = item.zCoord;

		x = Math.max(x, container.xCoord + 0.01);
		y = Math.max(y, container.yCoord + 0.01);
		z = Math.max(z, container.zCoord + 0.01);

		x = Math.min(x, container.xCoord + 0.99);
		y = Math.min(y, container.yCoord + 0.99);
		z = Math.min(z, container.zCoord + 0.99);

		if (item.input != ForgeDirection.UP && item.input != ForgeDirection.DOWN) {
			y = container.yCoord + Utils.getPipeFloorOf(item.getItemStack());
		}

		item.setPosition(x, y, z);
	}

	public void injectItem(TravelingItem item, ForgeDirection inputOrientation) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		item.reset();
		item.input = inputOrientation;

		items.add(item);

		readjustSpeed(item);
		readjustPosition(item);


		if (!container.worldObj.isRemote) {
			item.output = resolveDestination(item);
		}

		if (container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) container.pipe).entityEntered(item, inputOrientation);
		}

		if (!container.worldObj.isRemote) {
			sendItemPacket(item);

			if (items.size() > BuildCraftTransport.groupItemsTrigger) {
				groupEntities();
			}

			if (items.size() > MAX_PIPE_STACKS) {
				BuildCraftCore.bcLog.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many stacks: %d", container.xCoord, container.yCoord, container.zCoord, items.size()));
				destroyPipe();
				return;
			}

			int numItems = 0;
			for (TravelingItem travellingItem : items) {
				ItemStack stack = travellingItem.getItemStack();
				if (stack != null && stack.stackSize > 0)
					numItems += stack.stackSize;
			}

			if (numItems > MAX_PIPE_ITEMS) {
				BuildCraftCore.bcLog.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many items: %d", container.xCoord, container.yCoord, container.zCoord, numItems));
				destroyPipe();
			}
		}
	}

	private void destroyPipe() {
		BlockUtil.explodeBlock(container.worldObj, container.xCoord, container.yCoord, container.zCoord);
		container.worldObj.setBlockToAir(container.xCoord, container.yCoord, container.zCoord);
	}

	/**
	 * Bounces the item back into the pipe without changing the items map.
	 *
	 * @param item
	 */
	protected void reverseItem(TravelingItem item) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		items.unscheduleRemoval(item);

		item.toCenter = true;
		item.input = item.output.getOpposite();

		readjustSpeed(item);
		readjustPosition(item);

		if (!container.worldObj.isRemote) {
			item.output = resolveDestination(item);
		}

		if (container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) container.pipe).entityEntered(item, item.input);
		}

		if (!container.worldObj.isRemote) {
			sendItemPacket(item);
		}
	}

	public ForgeDirection resolveDestination(TravelingItem data) {
		LinkedList<ForgeDirection> listOfPossibleMovements = getPossibleMovements(data);

		if (listOfPossibleMovements.size() == 0)
			return ForgeDirection.UNKNOWN;
		else {
			int i = container.worldObj.rand.nextInt(listOfPossibleMovements.size());
			return listOfPossibleMovements.get(i);
		}
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent
	 * implementers of IPipeEntry or TileEntityChest.
	 */
	public LinkedList<ForgeDirection> getPossibleMovements(TravelingItem item) {
		LinkedList<ForgeDirection> result = new LinkedList<ForgeDirection>();

		item.blacklist.add(item.input.getOpposite());

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!item.blacklist.contains(o) && container.pipe.outputOpen(o))
				if (canReceivePipeObjects(o, item)) {
					result.add(o);
				}
		}

		if (result.size() == 0 && allowBouncing) {
			if (canReceivePipeObjects(item.input.getOpposite(), item)) {
				result.add(item.input.getOpposite());
			}
		}

		if (this.container.pipe instanceof IPipeTransportItemsHook) {
			Position pos = new Position(container.xCoord, container.yCoord, container.zCoord, item.input);
			result = ((IPipeTransportItemsHook) this.container.pipe).filterPossibleMovements(result, pos, item);
		}

		return result;
	}

	public boolean canReceivePipeObjects(ForgeDirection o, TravelingItem item) {
		TileEntity entity = container.getTile(o);

		if (!Utils.checkPipesConnections(entity, container))
			return false;

		if (entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) entity;

			return pipe.pipe.transport instanceof PipeTransportItems;
		} else if (entity instanceof IInventory && item.canSinkTo(entity))
			if (Transactor.getTransactorFor(entity).add(item.getItemStack(), o.getOpposite(), false).stackSize > 0)
				return true;

		return false;
	}

	@Override
	public void updateEntity() {
		moveSolids();
	}

	private void moveSolids() {
		items.performLoad();
		items.performRemoval();

		for (TravelingItem item : items) {
			if (item.isCorrupted()) {
				items.scheduleRemoval(item);
				continue;
			}

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
				item.setPosition(container.xCoord + 0.5, container.yCoord + Utils.getPipeFloorOf(item.getItemStack()), container.zCoord + 0.5);

				if (item.output == ForgeDirection.UNKNOWN) {
					if (travelHook != null) {
						travelHook.drop(this, item);
					}

					EntityItem dropped = null;

					if (items.scheduleRemoval(item)) {
						dropped = item.toEntityItem(item.input);
					}

					if (dropped != null) {
						onDropped(dropped);
					}
				} else {
					if (travelHook != null) {
						travelHook.centerReached(this, item);
					}
				}

			} else if (!item.toCenter && endReached(item)) {
				TileEntity tile = container.getTile(item.output);

				if (travelHook != null) {
					travelHook.endReached(this, item, tile);
				}

				// If the item has not been scheduled to removal by the hook
				if (items.scheduleRemoval(item)) {
					handleTileReached(item, tile);
				}

			}
		}

		items.performRemoval();
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
			if (!CoreProxy.proxy.isRenderWorld(container.worldObj)) {
				ItemStack added = Transactor.getTransactorFor(tile).add(item.getItemStack(), item.output.getOpposite(), true);

				item.getItemStack().stackSize -= added.stackSize;

				if (item.getItemStack().stackSize > 0) {
					reverseItem(item);
				}
			}
		} else {
			if (travelHook != null) {
				travelHook.drop(this, item);
			}

			EntityItem dropped = item.toEntityItem(item.output);

			if (dropped != null) {
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
			}
		}
	}

	protected boolean middleReached(TravelingItem item) {
		float middleLimit = item.getSpeed() * 1.01F;
		return (Math.abs(container.xCoord + 0.5 - item.xCoord) < middleLimit && Math.abs(container.yCoord + Utils.getPipeFloorOf(item.getItemStack()) - item.yCoord) < middleLimit && Math
				.abs(container.zCoord + 0.5 - item.zCoord) < middleLimit);
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

		NBTTagList nbttaglist = nbt.getTagList("travelingEntities");

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			try {
				NBTTagCompound dataTag = (NBTTagCompound) nbttaglist.tagAt(j);

				TravelingItem item = new TravelingItem();
				item.readFromNBT(dataTag);

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
	public void handleItemPacket(PacketPipeTransportContent packet) {

		if (packet.getID() != PacketIds.PIPE_CONTENTS)
			return;

		TravelingItem item = items.get(packet.getTravellingItemId());
		if (item == null) {
			item = new TravelingItem(packet.getTravellingItemId());
			items.add(item);
		}

		if (item.getItemStack() == null) {
			item.setItemStack(new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage()));
			if (packet.hasNBT()) {
				PacketDispatcher.sendPacketToServer(new PacketSimpleId(PacketIds.REQUEST_ITEM_NBT, container.xCoord, container.yCoord, container.zCoord, packet.getTravellingItemId()).getPacket());
			}
		} else {
			if (item.getItemStack().itemID != packet.getItemId() || item.getItemStack().stackSize != packet.getStackSize() || item.getItemStack().getItemDamage() != packet.getItemDamage() || item.getItemStack().hasTagCompound() != packet.hasNBT()) {
				item.setItemStack(new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage()));
				if (packet.hasNBT()) {
					PacketDispatcher.sendPacketToServer(new PacketSimpleId(PacketIds.REQUEST_ITEM_NBT, container.xCoord, container.yCoord, container.zCoord, packet.getTravellingItemId()).getPacket());
				}
			}
		}

		item.setPosition(packet.getItemX(), packet.getItemY(), packet.getItemZ());

		item.setSpeed(packet.getSpeed());

		item.input = packet.getInputOrientation();
		item.output = packet.getOutputOrientation();
		item.color = packet.getColor();
	}

	/**
	 * Handles the NBT tag Request from player of the id
	 */
	public void handleNBTRequestPacket(EntityPlayer player, int entityId) {
		TravelingItem item = items.get(entityId);
		if (item == null || item.item == null || item.getItemStack() == null)
			return;
		PacketDispatcher.sendPacketToPlayer(new PacketPipeTransportNBT(PacketIds.PIPE_ITEM_NBT, container.xCoord, container.yCoord, container.zCoord, entityId, item.getItemStack().getTagCompound()).getPacket(), (Player) player);
	}

	/**
	 * Handles the Item NBT tag information of the packet
	 */
	public void handleNBTPacket(PacketPipeTransportNBT packet) {
		TravelingItem item = items.get(packet.getEntityId());
		if (item == null || item.item == null || item.getItemStack() == null)
			return;
		item.getItemStack().setTagCompound(packet.getTagCompound());
	}

	/**
	 * Creates a packet describing a stack of items inside a pipe.
	 *
	 * @param data
	 * @return
	 */
	public Packet createItemPacket(TravelingItem data) {
		PacketPipeTransportContent packet = new PacketPipeTransportContent(data);
		return packet.getPacket();
	}

	private void sendItemPacket(TravelingItem data) {
		int dimension = container.worldObj.provider.dimensionId;
		PacketDispatcher.sendPacketToAllAround(container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST, dimension, createItemPacket(data));
	}

	public int getNumberOfItems() {
		return items.size();
	}

	public void onDropped(EntityItem item) {
		this.container.pipe.onDropped(item);
	}

	protected void neighborChange() {
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportItems))
				return false;
		}

		return tile instanceof TileGenericPipe || tile instanceof ISpecialInventory || (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0)
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
		// determine groupable entities
		List<TravelingItem> entities = new ArrayList<TravelingItem>();

		for (TravelingItem item : items) {
			if (!item.hasExtraData() && item.getItemStack().stackSize < item.getItemStack().getMaxStackSize()) {
				entities.add(item);
			}
		}

		if (entities.isEmpty())
			return; // nothing groupable

		// sort the groupable entities to have all entities with the same id:dmg next to each other (contiguous range)
		Collections.sort(entities, new Comparator<TravelingItem>() {
			@Override
			public int compare(TravelingItem a, TravelingItem b) {
				// the item id is always less than 2^15 so the int won't overflow
				int itemA = (a.getItemStack().itemID << 16) | a.getItemStack().getItemDamage();
				int itemB = (b.getItemStack().itemID << 16) | b.getItemStack().getItemDamage();

				return itemA - itemB;
			}
		});

		// group the entities
		int matchStart = 0;
		int lastId = (entities.get(0).getItemStack().itemID << 16) | entities.get(0).getItemStack().getItemDamage();

		for (int i = 1; i < entities.size(); i++) {
			int id = (entities.get(i).getItemStack().itemID << 16) | entities.get(i).getItemStack().getItemDamage();

			if (id != lastId) {
				// merge within the last matching ID range
				groupEntityRange(entities, matchStart, i);

				// start of the next matching ID range
				matchStart = i;
				lastId = id;
			}
		}

		// merge last matching ID range
		groupEntityRange(entities, matchStart, entities.size());
	}

	/**
	 * Group a range of items with matching IDs (item id + meta/dmg)
	 *
	 * @param entities entity list to group
	 * @param start start index (inclusive)
	 * @param end end index (exclusive)
	 */
	private void groupEntityRange(List<TravelingItem> entities, int start, int end) {
		for (int j = start; j < end; j++) {
			TravelingItem target = entities.get(j);
			if (target == null)
				continue;

			for (int k = j + 1; k < end; k++) {
				TravelingItem source = entities.get(k);
				if (source == null)
					continue;

				// only merge if the ItemStack tags match
				if (ItemStack.areItemStackTagsEqual(source.getItemStack(), target.getItemStack())) {
					// merge source to target
					int amount = source.getItemStack().stackSize;
					int space = target.getItemStack().getMaxStackSize() - target.getItemStack().stackSize;

					if (amount <= space) {
						// source fits completely into target
						target.getItemStack().stackSize += amount;

						items.remove(source);
						entities.set(k, null);
					} else {
						target.getItemStack().stackSize += space;

						source.getItemStack().stackSize -= space;
					}

					if (amount >= space) {
						// target not usable for further additions, no need to check more sources
						break;
					}
				}
			}
		}
	}

	@Override
	public void dropContents() {
		groupEntities();

		for (TravelingItem item : items) {
			container.pipe.dropItem(item.getItemStack());
		}

		items.clear();
	}

	@Override
	public boolean delveIntoUnloadedChunks() {
		return true;
	}
}
