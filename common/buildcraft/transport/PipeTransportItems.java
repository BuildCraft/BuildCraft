/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.network.PacketDispatcher;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.network.PacketPipeTransportContent;

import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ISidedInventory;

public class PipeTransportItems extends PipeTransport {

	public boolean allowBouncing = false;
	public Map<Integer, EntityData> travelingEntities = new TreeMap<Integer, EntityData>();
	private final List<EntityData> entitiesToLoad = new ArrayList<EntityData>();

	// TODO: generalize the use of this hook in particular for obsidian pipe
	public IItemTravelingHook travelHook;

	public void readjustSpeed(IPipedItem item) {
		if (container.pipe instanceof IPipeTransportItemsHook)
			((IPipeTransportItemsHook) container.pipe).readjustSpeed(item);
		else
			defaultReajustSpeed(item);
	}

	public void defaultReajustSpeed(IPipedItem item) {

		if (item.getSpeed() > Utils.pipeNormalSpeed)
			item.setSpeed(item.getSpeed() - Utils.pipeNormalSpeed);

		if (item.getSpeed() < Utils.pipeNormalSpeed)
			item.setSpeed(Utils.pipeNormalSpeed);
	}

	@Override
	public void entityEntering(IPipedItem item, Orientations orientation) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		readjustSpeed(item);

		EntityData data = travelingEntities.get(item.getEntityId());

		if (data == null) {
			data = new EntityData(item, orientation);
			travelingEntities.put(item.getEntityId(), data);

			if (item.getContainer() != null && item.getContainer() != this.container)
				((PipeTransportItems) ((TileGenericPipe) item.getContainer()).pipe.transport).scheduleRemoval(item);

			item.setContainer(container);
		}

		// Reajusting Ypos to make sure the object looks like sitting on the
		// pipe.
		if (orientation != Orientations.YPos && orientation != Orientations.YNeg)
			item.setPosition(item.getPosition().x, yCoord + Utils.getPipeFloorOf(item.getItemStack()), item.getPosition().z);

		  if (!worldObj.isRemote)
				data.output = resolveDestination(data);

		if (container.pipe instanceof IPipeTransportItemsHook)
			((IPipeTransportItemsHook) container.pipe).entityEntered(item, orientation);

		if (!worldObj.isRemote) {
			sendItemPacket(data);
		}

		if (travelingEntities.size() > BuildCraftTransport.groupItemsTrigger) {
			groupEntities();

			if (travelingEntities.size() > BuildCraftTransport.maxItemsInPipes)
				worldObj.createExplosion(null, xCoord, yCoord, zCoord, 1);
		}
	}

	public Orientations resolveDestination(EntityData data) {
		LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(data);

		if (listOfPossibleMovements.size() == 0)
			return Orientations.Unknown;
		else {
			int i = worldObj.rand.nextInt(listOfPossibleMovements.size());
			return listOfPossibleMovements.get(i);
		}
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent
	 * implementers of IPipeEntry or TileEntityChest.
	 */
	public LinkedList<Orientations> getPossibleMovements(EntityData data) {
		LinkedList<Orientations> result = new LinkedList<Orientations>();

		data.blacklist.add(data.input.reverse());

		for (Orientations o : Orientations.dirs())
			if (!data.blacklist.contains(o) && container.pipe.outputOpen(o))
				if (canReceivePipeObjects(o, data.item))
					result.add(o);

		if (result.size() == 0 && allowBouncing) {
			if (canReceivePipeObjects(data.input.reverse(), data.item))
				result.add(data.input.reverse());
		}

		if (this.container.pipe instanceof IPipeTransportItemsHook) {
			Position pos = new Position(xCoord, yCoord, zCoord, data.input);
			result = ((IPipeTransportItemsHook) this.container.pipe).filterPossibleMovements(result, pos, data.item);
		}

		return result;
	}

	public boolean canReceivePipeObjects(Orientations o, IPipedItem item) {
		TileEntity entity = container.getTile(o);

		if (!Utils.checkPipesConnections(entity, container))
			return false;

		if (entity instanceof IPipeEntry) {
			return true;
		} else if (entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe) entity;
			return pipe.pipe.transport instanceof PipeTransportItems;
		} else if (entity instanceof IInventory) {
			if(Transactor.getTransactorFor(entity).add(item.getItemStack(), o.reverse(), false).stackSize > 0)
				return true;
		}

		return false;
	}

	@Override
	public void updateEntity() {
		moveSolids();
	}

	HashSet<Integer> toRemove = new HashSet<Integer>();

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
		for (EntityData data : entitiesToLoad) {
			data.item.setWorld(worldObj);
			travelingEntities.put(data.item.getEntityId(), data);
		}

		entitiesToLoad.clear();
		performRemoval();

		for (EntityData data : travelingEntities.values()) {
			if (data.item.isCorrupted()) {
				scheduleRemoval(data.item);
				data.item.remove();
				continue;
			}

			Position motion = new Position(0, 0, 0, data.toCenter ? data.input : data.output);
			motion.moveForwards(data.item.getSpeed());

			data.item.setPosition(data.item.getPosition().x + motion.x, data.item.getPosition().y + motion.y, data.item.getPosition().z + motion.z);

			if ((data.toCenter && middleReached(data)) || outOfBounds(data)) {
				data.toCenter = false;

				// Reajusting to the middle
				data.item.setPosition(xCoord + 0.5, yCoord + Utils.getPipeFloorOf(data.item.getItemStack()), zCoord + 0.5);

				if (data.output == Orientations.Unknown) {
					if (travelHook != null)
						travelHook.drop(this, data);

					EntityItem dropped = null;

					if (!toRemove.contains(data.item.getEntityId()))
						dropped = data.item.toEntityItem(data.input);

					scheduleRemoval(data.item);

					if (dropped != null)
						onDropped(dropped);
				} else {
					if (travelHook != null)
						travelHook.centerReached(this, data);
				}

			} else if (!data.toCenter && endReached(data)) {
				Position destPos = new Position(xCoord, yCoord, zCoord, data.output);

				destPos.moveForwards(1.0);

				TileEntity tile = worldObj.getBlockTileEntity((int) destPos.x, (int) destPos.y, (int) destPos.z);

				if (travelHook != null)
					travelHook.endReached(this, data, tile);

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
		if (tile instanceof IPipeEntry)
			((IPipeEntry) tile).entityEntering(data.item, data.output);
		else if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			TileGenericPipe pipe = (TileGenericPipe) tile;
			((PipeTransportItems) pipe.pipe.transport).entityEntering(data.item, data.output);
		} else if (tile instanceof IInventory) {
			if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
				ItemStack added = Transactor.getTransactorFor(tile).add(data.item.getItemStack(), data.output.reverse(), true);

				data.item.getItemStack().stackSize -= added.stackSize;

				if(data.item.getItemStack().stackSize > 0) {
					data.toCenter = true;
					data.input = data.output.reverse();
					unscheduleRemoval(data.item);
					entityEntering(data.item, data.output.reverse());
				}
			}
		} else {
			if (travelHook != null)
				travelHook.drop(this, data);

			EntityItem dropped = data.item.toEntityItem(data.output);

			if (dropped != null)
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
		}
	}

	public boolean middleReached(EntityData entity) {
		float middleLimit = entity.item.getSpeed() * 1.01F;
		return (Math.abs(xCoord + 0.5 - entity.item.getPosition().x) < middleLimit
				&& Math.abs(yCoord + Utils.getPipeFloorOf(entity.item.getItemStack()) - entity.item.getPosition().y) < middleLimit && Math.abs(zCoord
				+ 0.5 - entity.item.getPosition().z) < middleLimit);
	}

	public boolean endReached(EntityData entity) {
		return entity.item.getPosition().x > xCoord + 1.0 || entity.item.getPosition().x < xCoord || entity.item.getPosition().y > yCoord + 1.0
				|| entity.item.getPosition().y < yCoord || entity.item.getPosition().z > zCoord + 1.0 || entity.item.getPosition().z < zCoord;
	}

	public boolean outOfBounds(EntityData entity) {
		return entity.item.getPosition().x > xCoord + 2.0 || entity.item.getPosition().x < xCoord - 1.0 || entity.item.getPosition().y > yCoord + 2.0
				|| entity.item.getPosition().y < yCoord - 1.0 || entity.item.getPosition().z > zCoord + 2.0 || entity.item.getPosition().z < zCoord - 1.0;
	}

	public Position getPosition() {
		return new Position(xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList nbttaglist = nbt.getTagList("travelingEntities");

		for (int j = 0; j < nbttaglist.tagCount(); ++j)
			try {
				NBTTagCompound dataTag = (NBTTagCompound) nbttaglist.tagAt(j);

				IPipedItem entity = new EntityPassiveItem(null);
				entity.readFromNBT(dataTag);

				if (entity.isCorrupted()) {
					entity.remove();
					continue;
				}

				entity.setContainer(container);

				EntityData data = new EntityData(entity, Orientations.values()[dataTag.getInteger("input")]);
				data.output = Orientations.values()[dataTag.getInteger("output")];
				data.toCenter = dataTag.getBoolean("toCenter");

				entitiesToLoad.add(data);
			} catch (Throwable t) {
				t.printStackTrace();
				// It may be the case that entities cannot be reloaded between
				// two versions - ignore these errors.
			}
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

	protected void doWork() {}

	/**
	 * Handles a packet describing a stack of items inside a pipe.
	 *
	 * @param packet
	 */
	public void handleItemPacket(PacketPipeTransportContent packet) {

		if (packet.getID() != PacketIds.PIPE_CONTENTS)
			return;

		EntityData data = travelingEntities.remove(packet.getEntityId());

		IPipedItem item = null;
		if(data == null) {
			item = EntityPassiveItem.getOrCreate(worldObj, packet.getEntityId());
		} else {
			item = data.item;
		}

		item.setItemStack(new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage()));

		if(item.getPosition() == null)
			item.setPosition(packet.getPosX(), packet.getPosY(), packet.getPosZ());

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
	 * Creates a packet describing a stack of items inside a pipe.
	 *
	 * @param data
	 * @return
	 */
	public Packet createItemPacket(EntityData data) {
		PacketPipeTransportContent packet = new PacketPipeTransportContent(container.xCoord, container.yCoord, container.zCoord, data);
		return packet.getPacket();
	}

	private void sendItemPacket(EntityData data) {
		int dimension = worldObj.provider.dimensionId;
		PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE, dimension, createItemPacket(data));
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
	public boolean isPipeConnected(TileEntity tile) {
		if( tile instanceof ISidedInventory ){
			Orientations or = Utils.get3dOrientation(new Position(container), new Position(tile));
			return ((ISidedInventory) tile).getSizeInventorySide(or.toDirection()) > 0;
		}
		return tile instanceof TileGenericPipe
			|| tile instanceof IPipeEntry
			|| tile instanceof ISpecialInventory
			|| (tile instanceof IInventory && ((IInventory)tile).getSizeInventory() > 0)
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
	 * Group all items that are similar, that is to say same dmg, same id and no
	 * contribution controlling them
	 */
	public void groupEntities() {
		EntityData[] entities = travelingEntities.values().toArray(new EntityData[travelingEntities.size()]);

		TreeSet<Integer> toRemove = new TreeSet<Integer>();

		for (int i = 0; i < entities.length; ++i) {
			EntityData data1 = entities[i];

			for (int j = i + 1; j < entities.length; ++j) {
				EntityData data2 = entities[j];

				if (data1.item.getItemStack().itemID == data2.item.getItemStack().itemID
						&& data1.item.getItemStack().getItemDamage() == data2.item.getItemStack().getItemDamage()
						&& !toRemove.contains(data1.item.getEntityId()) && !toRemove.contains(data2.item.getEntityId())
						&& !data1.item.hasContributions() && !data2.item.hasContributions()
						&& data1.item.getItemStack().stackSize + data2.item.getItemStack().stackSize < data1.item.getItemStack().getMaxStackSize()) {

					data1.item.getItemStack().stackSize += data2.item.getItemStack().stackSize;
					toRemove.add(data2.item.getEntityId());
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
			Utils.dropItems(worldObj, data.item.getItemStack(), xCoord, yCoord, zCoord);

		travelingEntities.clear();
	}

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportItems;
	}
}
