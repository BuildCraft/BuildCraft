/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.IAction;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.BlockIndex;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IBoxProvider;
import buildcraft.core.IBuilderInventory;
import buildcraft.core.IMachine;
import buildcraft.core.LaserData;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.blueprints.BuildingSlot;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.Utils;

public class TileBuilder extends TileBuildCraft implements IBuilderInventory, IMachine, IBoxProvider {

	private final ItemStack items[] = new ItemStack[28];

	private BptBuilderBase bluePrintBuilder;

	@NetworkData
	public Box box = new Box();

	private LinkedList<BlockIndex> path;

	@NetworkData
	public LinkedList<LaserData> pathLasers = new LinkedList<LaserData> ();

	private EntityRobot builderRobot;

	private LinkedList <ItemStack> requiredToBuild;

	private SafeTimeTracker debugBuildTracker = new SafeTimeTracker(5);

	@MjBattery (maxReceivedPerCycle = 25)
	private double mjStored = 0;

	public LinkedList <BuildingItem> buildingItems = new LinkedList<BuildingItem>();

	private class PathIterator {

		public Iterator<BlockIndex> currentIterator;
		public double cx, cy, cz;
		public float ix, iy, iz;
		public BlockIndex to;
		public double lastDistance;
		AxisAlignedBB oldBoundingBox = null;
		ForgeDirection o = null;

		public PathIterator(BlockIndex from, Iterator<BlockIndex> it) {
			this.to = it.next();

			currentIterator = it;

			double dx = to.x - from.x;
			double dy = to.y - from.y;
			double dz = to.z - from.z;

			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);

			cx = dx / size / 10;
			cy = dy / size / 10;
			cz = dz / size / 10;

			ix = from.x;
			iy = from.y;
			iz = from.z;

			lastDistance = (ix - to.x) * (ix - to.x) + (iy - to.y)
					* (iy - to.y) + (iz - to.z) * (iz - to.z);

			if (Math.abs(dx) > Math.abs(dz)) {
				if (dx > 0) {
					o = ForgeDirection.EAST;
				} else {
					o = ForgeDirection.WEST;
				}
			} else {
				if (dz > 0) {
					o = ForgeDirection.SOUTH;
				} else {
					o = ForgeDirection.NORTH;
				}
			}
		}

		/**
		 * Return false when reached the end of the iteration
		 */
		public BptBuilderBase next() {
			while (true) {
				BptBuilderBase bpt;

				int newX = Math.round(ix);
				int newY = Math.round(iy);
				int newZ = Math.round(iz);

				bpt = instanciateBluePrint(newX, newY, newZ, o);

				if (bpt == null) {
					return null;
				}

				AxisAlignedBB boundingBox = bpt.getBoundingBox();

				if (oldBoundingBox == null || !collision(oldBoundingBox, boundingBox)) {

					oldBoundingBox = boundingBox;

					if (bpt != null) {
						return bpt;
					}
				}

				ix += cx;
				iy += cy;
				iz += cz;

				double distance = (ix - to.x) * (ix - to.x) + (iy - to.y)
						* (iy - to.y) + (iz - to.z) * (iz - to.z);

				if (distance > lastDistance) {
					return null;
				} else {
					lastDistance = distance;
				}
			}
		}

		public PathIterator iterate() {
			if (currentIterator.hasNext()) {
				PathIterator next = new PathIterator(to, currentIterator);
				next.oldBoundingBox = oldBoundingBox;

				return next;
			} else {
				return null;
			}
		}

		public boolean collision(AxisAlignedBB left, AxisAlignedBB right) {
			if (left.maxX < right.minX || left.minX > right.maxX) {
				return false;
			}
			if (left.maxY < right.minY || left.minY > right.maxY) {
				return false;
			}
			if (left.maxZ < right.minZ || left.minZ > right.maxZ) {
				return false;
			}
			return true;
		}
	}

	public PathIterator currentPathIterator;

	private boolean done = true;

	public TileBuilder() {
		super();

		box.kind = Kind.STRIPES;
	}

	@Override
	public void initialize() {
		super.initialize();

		if (worldObj.isRemote) {
			return;
		}

		box.kind = Kind.STRIPES;

		for (int x = xCoord - 1; x <= xCoord + 1; ++x) {
			for (int y = yCoord - 1; y <= yCoord + 1; ++y) {
				for (int z = zCoord - 1; z <= zCoord + 1; ++z) {
					TileEntity tile = worldObj.getTileEntity(x, y, z);

					if (tile instanceof TilePathMarker) {
						path = ((TilePathMarker) tile).getPath();

						for (BlockIndex b : path) {
							worldObj.setBlockToAir(b.x, b.y, b.z);

							BuildCraftBuilders.pathMarkerBlock.dropBlockAsItem(
									worldObj, b.x, b.y, b.z,
									0, 0);
						}

						break;
					}
				}
			}
		}

		if (path != null && pathLasers.size() == 0) {
			path.getFirst().x = xCoord;
			path.getFirst().y = yCoord;
			path.getFirst().z = zCoord;

			createLasersForPath();

			sendNetworkUpdate();
		}

		iterateBpt();
	}

	public void createLasersForPath() {
		pathLasers = new LinkedList<LaserData>();
		BlockIndex previous = null;

		for (BlockIndex b : path) {
			if (previous != null) {
				LaserData laser = new LaserData(new Position(previous.x + 0.5,
						previous.y + 0.5, previous.z + 0.5), new Position(
						b.x + 0.5, b.y + 0.5, b.z + 0.5));

				pathLasers.add(laser);
			}

			previous = b;
		}
	}

	public BptBuilderBase instanciateBluePrint(int x, int y, int z, ForgeDirection o) {
		BlueprintBase bpt = null;

		try {
			bpt = ItemBlueprint.loadBlueprint(items [0]);
		} catch (Throwable t) {
			setInventorySlotContents(0, null);
			t.printStackTrace();
			return null;
		}

		if (bpt == null) {
			return null;
		}

		BptContext context = bpt.getContext(worldObj, bpt.getBoxForPos(x, y, z));

		if (bpt.rotate) {
			if (o == ForgeDirection.EAST) {
				// Do nothing
			} else if (o == ForgeDirection.SOUTH) {
				bpt.rotateLeft(context);
			} else if (o == ForgeDirection.WEST) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			} else if (o == ForgeDirection.NORTH) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			}
		}

		BptBuilderBase result = null;

		if (items[0].getItem() instanceof ItemBlueprintStandard) {
			result = new BptBuilderBlueprint((Blueprint) bpt, worldObj, x, y, z);
		} else if (items[0].getItem() instanceof ItemBlueprintTemplate) {
			return new BptBuilderTemplate(bpt, worldObj, x, y, z);
		} else {
			result = null;
		}

		return result;
	}

	public void iterateBpt() {
		if (items[0] == null || !(items[0].getItem() instanceof ItemBlueprint)) {
			if (bluePrintBuilder != null) {
				bluePrintBuilder = null;
			}

			if (builderRobot != null) {
				builderRobot.setDead();
				builderRobot = null;
			}

			if (box.isInitialized()) {
				box.reset();
			}

			if (currentPathIterator != null) {
				currentPathIterator = null;
			}

			return;
		}

		if (bluePrintBuilder == null || bluePrintBuilder.isDone()) {
			if (path != null && path.size() > 1) {
				if (currentPathIterator == null) {
					Iterator<BlockIndex> it = path.iterator();
					BlockIndex start = it.next();
					currentPathIterator = new PathIterator(start, it);
				}

				if (bluePrintBuilder != null && builderRobot != null) {
					//builderRobot.markEndOfBlueprint(bluePrintBuilder);
				}

				bluePrintBuilder = currentPathIterator.next();

				if (bluePrintBuilder != null) {
					box.reset();
					box.initialize(bluePrintBuilder);
					sendNetworkUpdate();
				}

				if (builderRobot != null) {
					//builderRobot.setBox(box);
				}

				if (bluePrintBuilder == null) {
					currentPathIterator = currentPathIterator.iterate();
				}

				if (currentPathIterator == null) {
					done = true;
				}
			} else {
				if (bluePrintBuilder != null && bluePrintBuilder.isDone()) {
					if (builderRobot != null) {
						//builderRobot.markEndOfBlueprint(bluePrintBuilder);
					}

					done = true;
					bluePrintBuilder = null;
				} else {
					bluePrintBuilder = instanciateBluePrint(xCoord, yCoord, zCoord,
							ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite());

					if (bluePrintBuilder != null) {
						box.initialize(bluePrintBuilder);
						sendNetworkUpdate();
					}
				}
			}
		}
	}

	@Override
	public int getSizeInventory() {
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result;
		if (items[i] == null) {
			result = null;
		} else if (items[i].stackSize > j) {
			result = items[i].splitStack(j);
		} else {
			ItemStack tmp = items[i];
			items[i] = null;
			result = tmp;
		}

		if (!worldObj.isRemote) {
			if (i == 0) {
				RPCHandler.rpcBroadcastPlayers(this, "setItemRequirements",
						null, null);
				iterateBpt();
			}
		}

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items[i] = itemstack;

		if (!worldObj.isRemote) {
			if (i == 0) {
				RPCHandler.rpcBroadcastPlayers(this, "setItemRequirements", null, null);
				iterateBpt();
				done = false;
			}
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (items[slot] == null) {
			return null;
		}
		ItemStack toReturn = items[slot];
		items[slot] = null;
		return toReturn;
	}

	@Override
	public String getInventoryName() {
		return "Builder";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		Utils.readStacksFromNBT(nbttagcompound, "Items", items);

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		if (nbttagcompound.hasKey("path")) {
			path = new LinkedList<BlockIndex>();
			NBTTagList list = nbttagcompound.getTagList("path",
					Utils.NBTTag_Types.NBTTagCompound.ordinal());

			for (int i = 0; i < list.tagCount(); ++i) {
				path.add(new BlockIndex(list.getCompoundTagAt(i)));
			}
		}

		done = nbttagcompound.getBoolean("done");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		Utils.writeStacksToNBT(nbttagcompound, "Items", items);

		if (box.isInitialized()) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}

		if (path != null) {
			NBTTagList list = new NBTTagList();

			for (BlockIndex i : path) {
				NBTTagCompound c = new NBTTagCompound();
				i.writeTo(c);
				list.appendTag(c);
			}

			nbttagcompound.setTag("path", list);
		}

		nbttagcompound.setBoolean("done", done);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		if (builderRobot != null) {
			builderRobot.setDead();
			builderRobot = null;
		}
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		BuildingItem toRemove = null;

		for (BuildingItem i : buildingItems) {
			i.update();

			if (i.isDone) {
				toRemove = i;
			}
		}

		if (toRemove != null) {
			buildingItems.remove(toRemove);
		}

		if (worldObj.isRemote) {
			return;
		}

		if (bluePrintBuilder != null) {
			bluePrintBuilder.removeDoneBuilders();
		}

		if ((bluePrintBuilder == null || bluePrintBuilder.isDone())
				&& box.isInitialized()
				//&& (builderRobot == null || builderRobot.done())
				) {

			box.reset();

			sendNetworkUpdate();

			return;
		}

		if (!box.isInitialized() && bluePrintBuilder == null && builderRobot != null) {
			builderRobot.setDead();
			builderRobot = null;
		}

		iterateBpt();
		debugForceBlueprintCompletion();

		if (done) {
			return;
		//}// else if (builderRobot != null && !builderRobot.readyToBuild()) {
		//	return;
		} else if (mjStored < 25) {
			return;
		}

		/* Temp fix to make Builders impotent as the World Destroyers they are
		if (bluePrintBuilder != null && !bluePrintBuilder.done) {
			if (!box.isInitialized()) {
				box.initialize(bluePrintBuilder);
			}

			if (builderRobot == null) {
				builderRobot = new EntityRobot(worldObj, box);
				worldObj.spawnEntityInWorld(builderRobot);
			}

			box.createLasers(worldObj, LaserKind.Stripes);

			builderRobot.scheduleContruction(bluePrintBuilder.getNextBlock(worldObj, new SurroundingInventory(worldObj, xCoord, yCoord, zCoord)),
					bluePrintBuilder.getContext());
		}
		*/

		mjStored = 0;
	}

	@Override
	public boolean isActive() {
		return !done;
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	public boolean isBuildingBlueprint() {
		return getStackInSlot(0) != null && getStackInSlot(0).getItem() instanceof ItemBlueprint;
	}

	public Collection<ItemStack> getNeededItems() {
		return requiredToBuild;
	}

	@RPC (RPCSide.CLIENT)
	public void setItemRequirements (LinkedList <ItemStack> rq, LinkedList <Integer> realSizes) {
		// Item stack serialized are represented through bytes, so 0-255. In
		// order to get the real amounts, we need to pass the real sizes of the
		// stacks as a separate list.

		requiredToBuild = rq;

		if (rq != null && rq.size() > 0) {
			Iterator <ItemStack> itStack = rq.iterator();
			Iterator <Integer> size = realSizes.iterator();

			while (true) {
				ItemStack stack = itStack.next();
				stack.stackSize = size.next();

				if (stack.stackSize > 999) {
					stack.stackSize = 999;
				}

				if (!itStack.hasNext()) {
					break;
				}
			}
		}
	}

	@Override
	public boolean isBuildingMaterial(int i) {
		return i != 0;
	}

	@Override
	public boolean allowAction(IAction action) {
		return false;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public Box getBox() {
		return box;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		Box renderBox = new Box (this).extendToEncompass(box);

		for (LaserData l : pathLasers) {
			renderBox = renderBox.extendToEncompass(l.head);
			renderBox = renderBox.extendToEncompass(l.tail);
		}

		return renderBox.getBoundingBox();
	}

	public void debugForceBlueprintCompletion () {
		if (!debugBuildTracker.markTimeIfDelay(worldObj)) {
			return;
		}

		if (bluePrintBuilder != null) {
			BuildingSlot slot = bluePrintBuilder.getNextBlock(worldObj, this);

			if (slot != null) {
				BuildingItem i = new BuildingItem();
				i.origin = new Position (xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
				i.destination = slot.getDestination();
				i.slotToBuild = slot;
				i.context = bluePrintBuilder.getContext();
				i.stacksToBuild = slot.getRequirements(bluePrintBuilder.getContext());
				buildingItems.add(i);
				RPCHandler.rpcBroadcastPlayers(this, "launchItem", i);
				bluePrintBuilder.registerBuilder(i);
			}

			if (bluePrintBuilder.isDone()) {
				// TODO: find a way to confirm that all agents are done before
				// calling post processing.
				bluePrintBuilder.postProcessing(worldObj);
				bluePrintBuilder = null;

				for (int i = 1; i < items.length; ++i) {
					if (items [i] == null) {
						items [i] = items [0];
						break;
					}
				}

				items [0] = null;
			}

			if (bluePrintBuilder instanceof BptBuilderBlueprint) {
				LinkedList <Integer> realSize = new LinkedList<Integer>();

				for (ItemStack stack : ((BptBuilderBlueprint) bluePrintBuilder).neededItems) {
					realSize.add(stack.stackSize);
					stack.stackSize = 0;
				}

				RPCHandler.rpcBroadcastPlayers(this, "setItemRequirements",
						((BptBuilderBlueprint) bluePrintBuilder).neededItems, realSize);
			}
		}
	}

	public BptBuilderBase getBlueprint () {
		if (bluePrintBuilder != null) {
			return bluePrintBuilder;
		} else {
			return null;
		}
	}

	@RPC (RPCSide.CLIENT)
	public void launchItem (BuildingItem item) {
		buildingItems.add(item);
	}

}