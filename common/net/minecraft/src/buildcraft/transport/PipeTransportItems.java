/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketPipeTransportContent;

public class PipeTransportItems extends PipeTransport {

	public boolean allowBouncing = false;
	public TreeMap<Integer, EntityData> travelingEntities = new TreeMap<Integer, EntityData> ();
	private final Vector <EntityData> entitiesToLoad = new Vector <EntityData> ();

	// TODO: generalize the use of this hook in particular for obsidian pipe
	public IItemTravelingHook travelHook;

	public void readjustSpeed (EntityPassiveItem item) {
		if (container.pipe instanceof IPipeTransportItemsHook)
			((IPipeTransportItemsHook) container.pipe).readjustSpeed(item);
		else
			defaultReajustSpeed(item);
	}

	public void defaultReajustSpeed (EntityPassiveItem item) {

		if (item.speed > Utils.pipeNormalSpeed)
			item.speed = item.speed - Utils.pipeNormalSpeed;

		if (item.speed < Utils.pipeNormalSpeed)
			item.speed = Utils.pipeNormalSpeed;
	}

	@Override
	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		readjustSpeed(item);

		if (!travelingEntities.containsKey(new Integer(item.entityId))) {
			travelingEntities.put(new Integer(item.entityId), new EntityData(
					item, orientation));

			if (item.container != null && item.container != this.container)
				((PipeTransportItems) ((TileGenericPipe) item.container).pipe.transport)
						.scheduleRemoval(item);

			item.container = container;
		}

		// Reajusting Ypos to make sure the object looks like sitting on the
		// pipe.
		if (orientation != Orientations.YPos && orientation != Orientations.YNeg)
			item.setPosition(item.posX, yCoord + Utils.getPipeFloorOf(item.item), item.posZ);

		if (container.pipe instanceof IPipeTransportItemsHook)
			((IPipeTransportItemsHook) container.pipe).entityEntered(item,
					orientation);

		if (APIProxy.isServerSide())
			if (item.synchroTracker.markTimeIfDelay(worldObj, 6 * BuildCraftCore.updateFactor))
				CoreProxy.sendToPlayers(createItemPacket(item, orientation),
						worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE,
						mod_BuildCraftTransport.instance);

		if (travelingEntities.size() > BuildCraftTransport.groupItemsTrigger) {
			groupEntities();

			if (travelingEntities.size() > BuildCraftTransport.maxItemsInPipes)
				worldObj.createExplosion(null, xCoord, yCoord, zCoord, 1);
		}
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent
	 * implementers of IPipeEntry or TileEntityChest.
	 */
	public LinkedList<Orientations> getPossibleMovements(Position pos,
			EntityPassiveItem item) {
		LinkedList<Orientations> result = new LinkedList<Orientations>();

		for (Orientations o : Orientations.dirs())
			if (o != pos.orientation.reverse()
					&& container.pipe.outputOpen(o))
				if (canReceivePipeObjects(o, item))
					result.add(o);

		if (result.size() == 0 && allowBouncing) {
			Position newPos = new Position(pos);
			newPos.orientation = newPos.orientation.reverse();

			if (canReceivePipeObjects(pos.orientation.reverse (), item))
				result.add(pos.orientation.reverse ());

		}

		if (this.container.pipe instanceof IPipeTransportItemsHook)
			result = ((IPipeTransportItemsHook) this.container.pipe)
					.filterPossibleMovements(result, pos, item);

		return result;
	}

	public boolean canReceivePipeObjects(Orientations o,
			EntityPassiveItem item) {
		TileEntity entity = container.getTile(o);

		if (!Utils.checkPipesConnections(entity, container))
			return false;

		if (entity instanceof IPipeEntry)
			return true;
		else if (entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) entity;

			return pipe.pipe.transport instanceof PipeTransportItems;
		} else if (entity instanceof IInventory)
			if (new StackUtil(item.item).checkAvailableSlot((IInventory) entity,
					 false, o.reverse()))
				return true;

		return false;
	}

	@Override
	public void updateEntity() {
		moveSolids();
	}

	HashSet <Integer> toRemove = new HashSet <Integer> ();

	public void scheduleRemoval (EntityPassiveItem item) {
		if (!toRemove.contains(item.entityId))
			toRemove.add(item.entityId);
	}

	public void performRemoval () {
		travelingEntities.keySet().removeAll(toRemove);
		toRemove = new HashSet <Integer> ();
	}

	private void moveSolids () {
		for (EntityData data : entitiesToLoad) {
			data.item.setWorld(worldObj);
			travelingEntities.put(new Integer(data.item.entityId), data);
		}

		entitiesToLoad.clear();
		performRemoval();

		for (EntityData data : travelingEntities.values()) {
			if (data.item.isCorrupted()) {
				scheduleRemoval(data.item);
				data.item.remove();
				continue;
			}

			Position motion = new Position (0, 0, 0, data.orientation);
			motion.moveForwards(data.item.speed);

			data.item.setPosition(data.item.posX + motion.x, data.item.posY
					+ motion.y, data.item.posZ + motion.z);

			if ((data.toCenter && middleReached(data)) || outOfBounds(data)) {
				data.toCenter = false;

				// Reajusting to the middle

				data.item.setPosition(xCoord + 0.5,
						yCoord + Utils.getPipeFloorOf(data.item.item),
						zCoord + + 0.5);

				Orientations nextOrientation = resolveDestination (data);

				if (nextOrientation == Orientations.Unknown) {
					if (travelHook != null)
						travelHook.drop(this, data);

					EntityItem dropped = null;

					if (!toRemove.contains(data.item.entityId))
						dropped = data.item.toEntityItem(data.orientation);

					scheduleRemoval(data.item);

					if (dropped != null)
						onDropped(dropped);
				} else {
					data.orientation = nextOrientation;

					if (travelHook != null)
						travelHook.centerReached(this, data);
				}


		    } else if (!data.toCenter && endReached (data)) {
				Position destPos = new Position(xCoord, yCoord, zCoord,
						data.orientation);

				destPos.moveForwards(1.0);

				TileEntity tile = worldObj.getBlockTileEntity((int) destPos.x,
						(int) destPos.y, (int) destPos.z);

				if (travelHook != null)
					travelHook.endReached (this, data, tile);

				// If the item has not been scheduled to removal by the hook
				if (!toRemove.contains(data.item.entityId)) {
					scheduleRemoval(data.item);
					handleTileReached (data, tile);
				}

		    }
		}

		performRemoval();
	}

	private void handleTileReached (EntityData data, TileEntity tile) {
		if (tile instanceof IPipeEntry)
			((IPipeEntry) tile).entityEntering(data.item,
					data.orientation);
		else if (tile instanceof TileGenericPipe
				&& ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			TileGenericPipe pipe = (TileGenericPipe) tile;

			((PipeTransportItems) pipe.pipe.transport).entityEntering(
					data.item, data.orientation);
		} else if (tile instanceof IInventory) {
			StackUtil utils = new StackUtil(data.item.item);

			if (!APIProxy.isClient(worldObj))
				if (utils.checkAvailableSlot((IInventory) tile, true,
						data.orientation.reverse())
						&& utils.items.stackSize == 0)
					data.item.remove();
				else {
					data.item.item = utils.items;
					EntityItem dropped = data.item.toEntityItem(data.orientation);

					if (dropped != null)
						// On SMP, the client side doesn't actually drops
						// items
						onDropped(dropped);
				}
		} else {
			if (travelHook != null)
				travelHook.drop (this, data);

			EntityItem dropped = data.item
					.toEntityItem(data.orientation);

			if (dropped != null)
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
		}
	}

	public boolean middleReached(EntityData entity) {
		float middleLimit = entity.item.speed * 1.01F;
		return (Math.abs(xCoord + 0.5 - entity.item.posX) < middleLimit
				&& Math.abs(yCoord + Utils.getPipeFloorOf(entity.item.item)
						- entity.item.posY) < middleLimit && Math.abs(zCoord + 0.5
				- entity.item.posZ) < middleLimit);
	}

	public boolean endReached (EntityData entity) {
		return entity.item.posX > xCoord + 1.0
		|| entity.item.posX < xCoord
		|| entity.item.posY > yCoord + 1.0
		|| entity.item.posY < yCoord
		|| entity.item.posZ > zCoord + 1.0
		|| entity.item.posZ < zCoord;
	}

	public boolean outOfBounds (EntityData entity) {
		return entity.item.posX > xCoord + 2.0
		|| entity.item.posX < xCoord - 1.0
		|| entity.item.posY > yCoord + 2.0
		|| entity.item.posY < yCoord - 1.0
		|| entity.item.posZ > zCoord + 2.0
		|| entity.item.posZ < zCoord - 1.0;
	}

	public Position getPosition() {
		return new Position (xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("travelingEntities");

		for (int j = 0; j < nbttaglist.tagCount(); ++j)
			try {
				NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist
				.tagAt(j);

				EntityPassiveItem entity = new EntityPassiveItem (null);
				entity.readFromNBT(nbttagcompound2);

				if (entity.isCorrupted()) {
					entity.remove();
					continue;
				}

				entity.container = container;

				EntityData data = new EntityData(entity,
						Orientations.values()[nbttagcompound2.getInteger("orientation")]);
				data.toCenter = nbttagcompound2.getBoolean("toCenter");

				entitiesToLoad.add(data);
			} catch (Throwable t) {
				t.printStackTrace();
				//  It may be the case that entities cannot be reloaded between
				//  two versions - ignore these errors.
			}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);

    	NBTTagList nbttaglist = new NBTTagList();

    	for (EntityData data : travelingEntities.values()) {
    		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
    		nbttaglist.appendTag(nbttagcompound2);
    		data.item.writeToNBT(nbttagcompound2);
    		nbttagcompound2.setBoolean("toCenter", data.toCenter);
    		nbttagcompound2.setInteger("orientation", data.orientation.ordinal());
    	}

    	nbttagcompound.setTag("travelingEntities", nbttaglist);
    }

    public Orientations resolveDestination (EntityData data) {
    	LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
				xCoord, yCoord, zCoord, data.orientation), data.item);

		if (listOfPossibleMovements.size() == 0)
			return Orientations.Unknown;
		else {
			int i;

			if (APIProxy.isClient(worldObj) || APIProxy.isServerSide())
				i = Math.abs(data.item.entityId + xCoord + yCoord + zCoord
						+ data.item.deterministicRandomization)
						% listOfPossibleMovements.size();
			else
				i = worldObj.rand.nextInt(listOfPossibleMovements.size());


			return listOfPossibleMovements.get(i);
		}
    }

    protected void doWork () {}

    /**
     * Handles a packet describing a stack of items inside a pipe.
     * @param packet
     */
	public void handleItemPacket(PacketPipeTransportContent packet) {

		if (packet.getID() != PacketIds.PIPE_CONTENTS)
			return;
		
		EntityPassiveItem item = EntityPassiveItem.getOrCreate(worldObj, packet.getEntityId());

		item.item = new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage());

		item.setPosition(packet.getPosX(), packet.getPosY(), packet.getPosZ());
		item.speed = packet.getSpeed();
		item.deterministicRandomization = packet.getRandomization();

		if (item.container != this.container
				|| !travelingEntities.containsKey(item.entityId)) {
			
			if (item.container != null)
				((PipeTransportItems) ((TileGenericPipe) item.container).pipe.transport)
						.scheduleRemoval(item);

			travelingEntities.put(new Integer(item.entityId), new EntityData(
					item, packet.getOrientation()));
			item.container = container;
			
		} else
			travelingEntities.get(new Integer(item.entityId)).orientation = packet.getOrientation();

	}

	/**
	 * Creates a packet describing a stack of items inside a pipe.
	 * @param item
	 * @param orientation
	 * @return
	 */
	public Packet createItemPacket (EntityPassiveItem item, Orientations orientation) {

		item.deterministicRandomization += worldObj.rand.nextInt(6);
		PacketPipeTransportContent packet = new PacketPipeTransportContent(container.xCoord, container.yCoord, container.zCoord, item, orientation);
		
		return packet.getPacket();
	}

	public int getNumberOfItems () {
		return travelingEntities.size();
	}

	public void onDropped (EntityItem item) {
		this.container.pipe.onDropped (item);
	}

	protected void neighborChange() {

	}

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		return tile instanceof TileGenericPipe
    	    || tile instanceof IPipeEntry
			|| tile instanceof IInventory
			|| (tile instanceof IMachine && ((IMachine) tile).manageSolids());
	}

	@Override
	public boolean acceptItems() {
		return true;
	}

	public boolean isTriggerActive (Trigger trigger) {
		return false;
	}

	/**
	 * Group all items that are similar, that is to say same dmg, same id and
	 * no contribution controlling them
	 */
	public void groupEntities () {
    	EntityData [] entities = travelingEntities.values().toArray(new EntityData [travelingEntities.size()]);

    	TreeSet <Integer> toRemove = new TreeSet<Integer>();

		for (int i = 0; i < entities.length; ++i) {
			EntityData data1 = entities [i];

			for (int j = i + 1; j < entities.length; ++j) {
				EntityData data2 = entities [j];

				if (data1.item.item.itemID == data2.item.item.itemID
						&& data1.item.item.getItemDamage() == data2.item.item
								.getItemDamage()
						&& !toRemove.contains(data1.item.entityId)
						&& !toRemove.contains(data2.item.entityId)
						&& !data1.item.hasContributions()
						&& !data2.item.hasContributions()
						&& data1.item.item.stackSize
								+ data2.item.item.stackSize < data1.item.item
								.getMaxStackSize()) {

					data1.item.item.stackSize += data2.item.item.stackSize;
					toRemove.add(data2.item.entityId);
				}
			}
		}

		for (Integer i : toRemove) {
			travelingEntities.get(i).item.remove();
			travelingEntities.remove(i);
		}
    }

	@Override
	public void dropContents() {
		groupEntities();

		for (EntityData data : travelingEntities.values())
			Utils.dropItems(worldObj, data.item.item, xCoord, yCoord, zCoord);

    	travelingEntities.clear();
	}

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportItems;
	}
}
