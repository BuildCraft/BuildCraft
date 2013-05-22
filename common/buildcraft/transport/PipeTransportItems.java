/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;
import buildcraft.transport.network.PacketPipeTransportContent;
import buildcraft.transport.network.PacketPipeTransportNBT;
import buildcraft.transport.network.PacketSimpleId;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PipeTransportItems extends PipeTransport {

	public boolean allowBouncing = false;
	public Map<Integer, EntityData> travelingEntities = new HashMap<Integer, EntityData>();
	private final List<EntityData> entitiesToLoad = new LinkedList<EntityData>();

	private final List<EntityData> delayedEntitiesToLoad = new LinkedList<EntityData>();
	private int delay = -1;

	// TODO: generalize the use of this hook in particular for obsidian pipe
	public IItemTravelingHook travelHook;

	public void readjustSpeed(IPipedItem item) {
		if (container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) container.pipe).readjustSpeed(item);
		} else {
			defaultReajustSpeed(item);
		}
	}

	public void defaultReajustSpeed(IPipedItem item) {
		float speed = item.getSpeed();

		if (speed > Utils.pipeNormalSpeed) {
			speed -= Utils.pipeNormalSpeed;
		}

		if (speed < Utils.pipeNormalSpeed) {
			speed = Utils.pipeNormalSpeed;
		}

		item.setSpeed(speed);
	}

	@Override
	public void entityEntering(IPipedItem item, ForgeDirection orientation) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		readjustSpeed(item);

		EntityData data = travelingEntities.get(item.getEntityId());

		if (data == null) {
			data = new EntityData(item, orientation);
			travelingEntities.put(item.getEntityId(), data);

			if (item.getContainer() != null && item.getContainer() != this.container) {
				((PipeTransportItems) ((TileGenericPipe) item.getContainer()).pipe.transport).scheduleRemoval(item);
			}

			item.setContainer(container);
		}

		// Reajusting Ypos to make sure the object looks like sitting on the
		// pipe.
		if (orientation != ForgeDirection.UP && orientation != ForgeDirection.DOWN) {
			item.setPosition(item.getPosition().x, yCoord + Utils.getPipeFloorOf(item.getItemStack()), item.getPosition().z);
		}

		if (!worldObj.isRemote) {
			data.output = resolveDestination(data);
		}

		if (container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) container.pipe).entityEntered(item, orientation);
		}

		if (!worldObj.isRemote) {
			sendItemPacket(data);
		}

		if (!worldObj.isRemote && travelingEntities.size() > BuildCraftTransport.groupItemsTrigger) {
			groupEntities();

			if (travelingEntities.size() > BuildCraftTransport.maxItemsInPipes) {
				BlockUtil.explodeBlock(worldObj, xCoord, yCoord, zCoord);
				return;
			}
		}
	}

	/**
	 * Bounces the item back into the pipe without changing the travelingEntities map.
	 *
	 * @param data
	 */
	protected void reverseItem(EntityData data) {
		if (data.item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		unscheduleRemoval(data.item);

		data.toCenter = true;
		data.input = data.output.getOpposite();

		readjustSpeed(data.item);

		// Reajusting Ypos to make sure the object looks like sitting on the
		// pipe.
		if (data.input != ForgeDirection.UP && data.input != ForgeDirection.DOWN) {
			data.item.setPosition(data.item.getPosition().x, yCoord + Utils.getPipeFloorOf(data.item.getItemStack()), data.item.getPosition().z);
		}

		if (!worldObj.isRemote) {
			data.output = resolveDestination(data);
		}

		if (container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) container.pipe).entityEntered(data.item, data.input);
		}

		if (!worldObj.isRemote) {
			sendItemPacket(data);
		}
	}

	public ForgeDirection resolveDestination(EntityData data) {
		LinkedList<ForgeDirection> listOfPossibleMovements = getPossibleMovements(data);

		if (listOfPossibleMovements.size() == 0)
			return ForgeDirection.UNKNOWN;
		else {
			int i = worldObj.rand.nextInt(listOfPossibleMovements.size());
			return listOfPossibleMovements.get(i);
		}
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent implementers of IPipeEntry or TileEntityChest.
	 */
	public LinkedList<ForgeDirection> getPossibleMovements(EntityData data) {
		LinkedList<ForgeDirection> result = new LinkedList<ForgeDirection>();

		data.blacklist.add(data.input.getOpposite());

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!data.blacklist.contains(o) && container.pipe.outputOpen(o))
				if (canReceivePipeObjects(o, data.item)) {
					result.add(o);
				}
		}

		if (result.size() == 0 && allowBouncing) {
			if (canReceivePipeObjects(data.input.getOpposite(), data.item)) {
				result.add(data.input.getOpposite());
			}
		}

		if (this.container.pipe instanceof IPipeTransportItemsHook) {
			Position pos = new Position(xCoord, yCoord, zCoord, data.input);
			result = ((IPipeTransportItemsHook) this.container.pipe).filterPossibleMovements(result, pos, data.item);
		}

		return result;
	}

	public boolean canReceivePipeObjects(ForgeDirection o, IPipedItem item) {
		TileEntity entity = container.getTile(o);

		if (!Utils.checkPipesConnections(entity, container))
			return false;

		if (entity instanceof IPipeEntry)
			return true;
		else if (entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) entity;

			return pipe.pipe.transport instanceof PipeTransportItems;
		} else if (entity instanceof IInventory)
			if (Transactor.getTransactorFor(entity).add(item.getItemStack(), o.getOpposite(), false).stackSize > 0)
				return true;

		return false;
	}

	@Override
	public void updateEntity() {
		moveSolids();
	}

	Set<Integer> toRemove = new HashSet<Integer>();

	public void scheduleRemoval(IPipedItem item) {
		toRemove.add(item.getEntityId());
	}

	public void unscheduleRemoval(IPipedItem item) {
		toRemove.remove(item.getEntityId());
	}

	public void performRemoval() {
		travelingEntities.keySet().removeAll(toRemove);
		toRemove.clear();
	}

	private void moveSolids() {
		if(delay > 0) {
			delay--;
			if(delay == 0) {
				entitiesToLoad.addAll(delayedEntitiesToLoad);
				delayedEntitiesToLoad.clear();
				delay = -1;
			}
		}
		if (!entitiesToLoad.isEmpty()) {
			for (EntityData data : entitiesToLoad) {
				data.item.setWorld(worldObj);
				travelingEntities.put(data.item.getEntityId(), data);
			}
			entitiesToLoad.clear();
		}
		performRemoval();

		for (EntityData data : travelingEntities.values()) {
			if (data.item.isCorrupted()) {
				scheduleRemoval(data.item);
				data.item.remove();
				continue;
			}

			Position motion = new Position(0, 0, 0, data.toCenter ? data.input : data.output);
			motion.moveForwards(data.item.getSpeed());

			Position pos = data.item.getPosition();
			data.item.setPosition(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);
			pos = data.item.getPosition();

			if ((data.toCenter && middleReached(data, pos)) || outOfBounds(pos)) {
				data.toCenter = false;

				// Reajusting to the middle
				data.item.setPosition(xCoord + 0.5, yCoord + Utils.getPipeFloorOf(data.item.getItemStack()), zCoord + 0.5);

				if (data.output == ForgeDirection.UNKNOWN) {
					if (travelHook != null) {
						travelHook.drop(this, data);
					}

					EntityItem dropped = null;

					if (!toRemove.contains(data.item.getEntityId())) {
						dropped = data.item.toEntityItem(data.input);
					}

					scheduleRemoval(data.item);

					if (dropped != null) {
						onDropped(dropped);
					}
				} else {
					if (travelHook != null) {
						travelHook.centerReached(this, data);
					}
				}

			} else if (!data.toCenter && endReached(pos)) {
				TileEntity tile = container.getTile(data.output);

				if (travelHook != null) {
					travelHook.endReached(this, data, tile);
				}

				// If the item has not been scheduled to removal by the hook
				if (!toRemove.contains(data.item.getEntityId())) {
					scheduleRemoval(data.item);
					handleTileReached(data, tile);
				}

			}
		}

		performRemoval();
	}

	private void handleTileReached(EntityData data, TileEntity tile) {
		if (tile instanceof IPipeEntry) {
			((IPipeEntry) tile).entityEntering(data.item, data.output);
		} else if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			TileGenericPipe pipe = (TileGenericPipe) tile;
			((PipeTransportItems) pipe.pipe.transport).entityEntering(data.item, data.output);
		} else if (tile instanceof IInventory) {
			if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
				ItemStack added = Transactor.getTransactorFor(tile).add(data.item.getItemStack(), data.output.getOpposite(), true);

				data.item.getItemStack().stackSize -= added.stackSize;

				if (data.item.getItemStack().stackSize > 0) {
					reverseItem(data);
				}
			}
		} else {
			if (travelHook != null) {
				travelHook.drop(this, data);
			}

			EntityItem dropped = data.item.toEntityItem(data.output);

			if (dropped != null) {
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
			}
		}
	}

	protected boolean middleReached(EntityData entity, Position pos) {
		float middleLimit = entity.item.getSpeed() * 1.01F;
		return (Math.abs(xCoord + 0.5 - pos.x) < middleLimit && Math.abs(yCoord + Utils.getPipeFloorOf(entity.item.getItemStack()) - pos.y) < middleLimit && Math
				.abs(zCoord + 0.5 - pos.z) < middleLimit);
	}

	protected boolean endReached(Position pos) {
		return pos.x > xCoord + 1 || pos.x < xCoord || pos.y > yCoord + 1 || pos.y < yCoord || pos.z > zCoord + 1 || pos.z < zCoord;
	}

	protected boolean outOfBounds(Position pos) {
		return pos.x > xCoord + 2 || pos.x < xCoord - 1 || pos.y > yCoord + 2 || pos.y < yCoord - 1 || pos.z > zCoord + 2 || pos.z < zCoord - 1;
	}

	public Position getPosition() {
		return new Position(xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList nbttaglist = nbt.getTagList("travelingEntities");

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			try {
				NBTTagCompound dataTag = (NBTTagCompound) nbttaglist.tagAt(j);

				IPipedItem entity = new EntityPassiveItem(null);
				entity.readFromNBT(dataTag);

				if (entity.isCorrupted()) {
					entity.remove();
					continue;
				}

				entity.setContainer(container);

				EntityData data = new EntityData(entity, ForgeDirection.getOrientation(dataTag.getInteger("input")));
				data.output = ForgeDirection.getOrientation(dataTag.getInteger("output"));
				data.toCenter = dataTag.getBoolean("toCenter");

				delayedEntitiesToLoad.add(data);
			} catch (Throwable t) {
				t.printStackTrace();
				// It may be the case that entities cannot be reloaded between
				// two versions - ignore these errors.
			}
		}
		delay = 2;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList nbttaglist = new NBTTagList();

		for (EntityData data : travelingEntities.values()) {
			NBTTagCompound dataTag = new NBTTagCompound();
			nbttaglist.appendTag(dataTag);
			data.item.writeToNBT(dataTag);
			dataTag.setBoolean("toCenter", data.toCenter);
			dataTag.setInteger("input", data.input.ordinal());
			dataTag.setInteger("output", data.output.ordinal());
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

		EntityData data = travelingEntities.remove(packet.getEntityId());

		IPipedItem item;
		if (data == null) {
			item = EntityPassiveItem.getOrCreate(worldObj, packet.getEntityId());
		} else {
			item = data.item;
		}

		if(item.getItemStack() == null) {
			item.setItemStack(new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage()));
			if(packet.hasNBT()) {
				PacketDispatcher.sendPacketToServer(new PacketSimpleId(PacketIds.REQUEST_ITEM_NBT, this.xCoord, this.yCoord, this.zCoord, packet.getEntityId()).getPacket());
			}
		} else {
			if(item.getItemStack().itemID != packet.getItemId() || item.getItemStack().stackSize != packet.getStackSize() || item.getItemStack().getItemDamage() != packet.getItemDamage() || item.getItemStack().hasTagCompound() != packet.hasNBT()) {
				item.setItemStack(new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage()));
				if(packet.hasNBT()) {
					PacketDispatcher.sendPacketToServer(new PacketSimpleId(PacketIds.REQUEST_ITEM_NBT, this.xCoord, this.yCoord, this.zCoord, packet.getEntityId()).getPacket());
				}		
			}
		}

		if (item.getPosition() == null) {
			item.setPosition(packet.getPosX(), packet.getPosY(), packet.getPosZ());
		}

		item.setSpeed(packet.getSpeed());

		if (item.getContainer() != null && item.getContainer() != container) {
			((PipeTransportItems) ((TileGenericPipe) item.getContainer()).pipe.transport).scheduleRemoval(item);
			item.setContainer(container);
		}

		data = new EntityData(item, packet.getInputOrientation());
		data.output = packet.getOutputOrientation();
		travelingEntities.put(item.getEntityId(), data);
	}

	/**
	 * Handles the NBT tag Request from player of the entityId
	 */
	public void handleNBTRequestPacket(EntityPlayer player, int entityId) {
		EntityData data = travelingEntities.get(entityId);
		if(data == null || data.item == null || data.item.getItemStack() == null) return;
		PacketDispatcher.sendPacketToPlayer(new PacketPipeTransportNBT(PacketIds.PIPE_ITEM_NBT, this.xCoord, this.yCoord, this.zCoord, entityId, data.item.getItemStack().getTagCompound()).getPacket(), (Player) player);
	}

	/**
	 * Handles the Item NBT tag information of the packet
	 */
	public void handleNBTPacket(PacketPipeTransportNBT packet) {
		EntityData data = travelingEntities.get(packet.getEntityId());
		if(data == null || data.item == null || data.item.getItemStack() == null) return;
		data.item.getItemStack().setTagCompound(packet.getTagCompound());
	}
	
	/**
	 * Creates a packet describing a stack of items inside a pipe.
	 *
	 * @param data
	 * @return
	 */
	public Packet createItemPacket(EntityData data) {
		PacketPipeTransportContent packet = new PacketPipeTransportContent(data);
		return packet.getPacket();
	}

	private void sendItemPacket(EntityData data) {
		int dimension = worldObj.provider.dimensionId;
		PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST, dimension, createItemPacket(data));
	}

	public int getNumberOfItems() {
		return travelingEntities.size();
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

		return tile instanceof TileGenericPipe || tile instanceof IPipeEntry || tile instanceof ISpecialInventory || (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0)
				|| (tile instanceof IMachine && ((IMachine) tile).manageSolids());
	}

	@Override
	public boolean acceptItems() {
		return true;
	}

	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}

	/**
	 * Group all items that are similar, that is to say same dmg, same id, same nbt and no contribution controlling them
	 */
	public void groupEntities() {
		// determine groupable entities
		List<EntityData> entities = new ArrayList<EntityData>();

		for (EntityData entityData : travelingEntities.values()) {
			if (!entityData.item.hasContributions() &&
				entityData.item.getItemStack().stackSize < entityData.item.getItemStack().getMaxStackSize()) {
				entities.add(entityData);
			}
		}

		if (entities.isEmpty()) return; // nothing groupable

		// sort the groupable entities to have all entities with the same id:dmg next to each other (contiguous range)
		Collections.sort(entities, new Comparator<EntityData>() {
			@Override
			public int compare(EntityData a, EntityData b) {
				// the item id is always less than 2^15 so the int won't overflow
				int itemA = (a.item.getItemStack().itemID << 16) | a.item.getItemStack().getItemDamage();
				int itemB = (b.item.getItemStack().itemID << 16) | b.item.getItemStack().getItemDamage();

				return itemA - itemB;
			}
		});

		// group the entities
		int matchStart = 0;
		int lastId = (entities.get(0).item.getItemStack().itemID << 16) | entities.get(0).item.getItemStack().getItemDamage();

		for (int i = 1; i < entities.size(); i++) {
			int id = (entities.get(i).item.getItemStack().itemID << 16) | entities.get(i).item.getItemStack().getItemDamage();

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
	private void groupEntityRange(List<EntityData> entities, int start, int end) {
		for (int j = start; j < end; j++) {
			EntityData target = entities.get(j);
			if (target == null) continue;

			for (int k = j + 1; k < end; k++) {
				EntityData source = entities.get(k);
				if (source == null) continue;

				// only merge if the ItemStack tags match
				if (ItemStack.areItemStackTagsEqual(source.item.getItemStack(), target.item.getItemStack())) {
					// merge source to target
					int amount = source.item.getItemStack().stackSize;
					int space = target.item.getItemStack().getMaxStackSize() - target.item.getItemStack().stackSize;

					if (amount <= space) {
						// source fits completely into target
						target.item.getItemStack().stackSize += amount;

						source.item.remove();
						travelingEntities.remove(source.item.getEntityId());
						entities.set(k, null);
					} else {
						target.item.getItemStack().stackSize += space;

						source.item.getItemStack().stackSize -= space;
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

		for (EntityData data : travelingEntities.values()) {
			Utils.dropItems(worldObj, data.item.getItemStack(), xCoord, yCoord, zCoord);
		}

		travelingEntities.clear();
	}
	
	@Override
	public boolean delveIntoUnloadedChunks() {
		return true;
	}
}
