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
import net.minecraft.world.WorldSettings.GameType;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.Translation;
import buildcraft.api.core.NetworkData;
import buildcraft.api.core.Position;
import buildcraft.api.gates.IAction;
import buildcraft.core.BlockIndex;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IMachine;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

public class TileBuilder extends TileAbstractBuilder implements IMachine {

	private static int POWER_ACTIVATION = 50;

	@NetworkData
	public Box box = new Box();
	public PathIterator currentPathIterator;

	private SimpleInventory inv = new SimpleInventory(28, "Builder", 64);
	private BptBuilderBase bluePrintBuilder;
	private LinkedList<BlockIndex> path;
	private LinkedList<ItemStack> requiredToBuild;
	private NBTTagCompound initNBT = null;
	private boolean done = true;

	private class PathIterator {

		public Iterator<BlockIndex> currentIterator;
		public double cx, cy, cz;
		public float ix, iy, iz;
		public BlockIndex to;
		public double lastDistance;
		AxisAlignedBB oldBoundingBox = null;
		ForgeDirection o = null;

		public PathIterator(BlockIndex from, Iterator<BlockIndex> it, ForgeDirection initialDir) {
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

			if (dx == 0 && dz == 0) {
				o = initialDir;
			} else if (Math.abs(dx) > Math.abs(dz)) {
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
				PathIterator next = new PathIterator(to, currentIterator, o);
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


		if (initNBT != null) {
			iterateBpt(true);

			if (initNBT.hasKey("iterator")) {
				BlockIndex expectedTo = new BlockIndex(initNBT.getCompoundTag("iterator"));

				while (!done && bluePrintBuilder != null && currentPathIterator != null) {
					BlockIndex bi = new BlockIndex((int) currentPathIterator.ix,
							(int) currentPathIterator.iy, (int) currentPathIterator.iz);

					if (bi.equals(expectedTo)) {
						break;
					}

					iterateBpt(true);
				}
			}

			if (bluePrintBuilder != null) {
				bluePrintBuilder.loadBuildStateToNBT(
						initNBT.getCompoundTag("builderState"), this);
			}

			initNBT = null;
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
			createLasersForPath();

			sendNetworkUpdate();
		}

		iterateBpt(false);
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
			bpt = ItemBlueprint.loadBlueprint(getStackInSlot(0));
		} catch (Throwable t) {
			setInventorySlotContents(0, null);
			t.printStackTrace();
			return null;
		}

		if (bpt == null) {
			return null;
		}

		if (bpt.buildingPermission == BuildingPermission.NONE
				|| (bpt.buildingPermission == BuildingPermission.CREATIVE_ONLY && worldObj
						.getWorldInfo().getGameType() != GameType.CREATIVE)) {
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

		Translation transform = new Translation();

		transform.x = x - bpt.anchorX;
		transform.y = y - bpt.anchorY;
		transform.z = z - bpt.anchorZ;

		bpt.translateToWorld(transform);

		if (getStackInSlot(0).getItem() instanceof ItemBlueprintStandard) {
			return new BptBuilderBlueprint((Blueprint) bpt, worldObj, x, y, z);
		} else if (getStackInSlot(0).getItem() instanceof ItemBlueprintTemplate) {
			return new BptBuilderTemplate(bpt, worldObj, x, y, z);
		} else {
			return null;
		}
	}

	public void iterateBpt(boolean forceIterate) {
		if (getStackInSlot(0) == null || !(getStackInSlot(0).getItem() instanceof ItemBlueprint)) {
			if (bluePrintBuilder != null) {
				bluePrintBuilder = null;
			}

			if (box.isInitialized()) {
				box.reset();
			}

			if (currentPathIterator != null) {
				currentPathIterator = null;
			}

			updateRequirements();

			sendNetworkUpdate();

			return;
		}

		if (bluePrintBuilder == null || (bluePrintBuilder.isDone(this) || forceIterate)) {
			if (path != null && path.size() > 1) {
				if (currentPathIterator == null) {
					Iterator<BlockIndex> it = path.iterator();
					BlockIndex start = it.next();
					currentPathIterator = new PathIterator(start, it,
							ForgeDirection.values()[worldObj.getBlockMetadata(
									xCoord, yCoord, zCoord)].getOpposite());
				}

				if (bluePrintBuilder != null && bluePrintBuilder.isDone(this)) {
					bluePrintBuilder.postProcessing(worldObj);
				}

				bluePrintBuilder = currentPathIterator.next();

				if (bluePrintBuilder != null) {
					box.reset();
					box.initialize(bluePrintBuilder);
					sendNetworkUpdate();
				}

				if (bluePrintBuilder == null) {
					currentPathIterator = currentPathIterator.iterate();
				}

				if (currentPathIterator == null) {
					done = true;
				} else {
					done = false;
				}
			} else {
				if (bluePrintBuilder != null && bluePrintBuilder.isDone(this)) {
					bluePrintBuilder.postProcessing(worldObj);

					done = true;
					bluePrintBuilder = null;
				} else {
					bluePrintBuilder = instanciateBluePrint(xCoord, yCoord, zCoord,
							ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite());

					if (bluePrintBuilder != null) {
						box.initialize(bluePrintBuilder);
						sendNetworkUpdate();
						done = false;
					}
				}
			}

			updateRequirements();
		}

		if (done) {
			boolean dropBlueprint = true;
			for (int i = 1; i < getSizeInventory(); ++i) {
				if (getStackInSlot(i) == null) {
					setInventorySlotContents(i, getStackInSlot(0));
					dropBlueprint = false;
					break;
				}
			}
			if (dropBlueprint) {
				InvUtils.dropItems(getWorld(), getStackInSlot(0), xCoord, yCoord, zCoord);
			}

			setInventorySlotContents(0, null);
			box.reset();
		}
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result = inv.decrStackSize(i, j);

		if (!worldObj.isRemote) {
			if (i == 0) {
				RPCHandler.rpcBroadcastPlayers(this, "setItemRequirements",
						null, null);
				iterateBpt(false);
			}
		}

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);

		if (!worldObj.isRemote) {
			if (i == 0) {
				iterateBpt(false);
				done = false;
			}
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
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

		inv.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		if (nbttagcompound.hasKey("path")) {
			path = new LinkedList<BlockIndex>();
			NBTTagList list = nbttagcompound.getTagList("path",
					Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); ++i) {
				path.add(new BlockIndex(list.getCompoundTagAt(i)));
			}
		}

		done = nbttagcompound.getBoolean("done");

		// The rest of load has to be done upon initialize.
		initNBT = (NBTTagCompound) nbttagcompound.getCompoundTag("bptBuilder").copy();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		inv.writeToNBT(nbttagcompound);

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

		NBTTagCompound bptNBT = new NBTTagCompound();

		if (bluePrintBuilder != null) {
			NBTTagCompound builderCpt = new NBTTagCompound();
			bluePrintBuilder.saveBuildStateToNBT(builderCpt, this);
			bptNBT.setTag("builderState", builderCpt);
		}

		if (currentPathIterator != null) {
			NBTTagCompound iteratorNBT = new NBTTagCompound();
			new BlockIndex((int) currentPathIterator.ix,
					(int) currentPathIterator.iy, (int) currentPathIterator.iz)
					.writeTo(iteratorNBT);
			bptNBT.setTag ("iterator", iteratorNBT);
		}

		nbttagcompound.setTag("bptBuilder", bptNBT);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
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

		if (worldObj.isRemote) {
			return;
		}

		if (bluePrintBuilder != null) {
			bluePrintBuilder.removeDoneBuilders(this);
		}

		if ((bluePrintBuilder == null || bluePrintBuilder.isDone(this))
				&& box.isInitialized()) {
			box.reset();

			sendNetworkUpdate();

			return;
		}

		iterateBpt(false);

		if (getWorld().getWorldInfo().getGameType() == GameType.CREATIVE) {
			build();
		} else {
			if (mjStored > POWER_ACTIVATION) {
				build();
			}
		}


		if (done) {
			return;
		} else if (mjStored < 25) {
			return;
		}
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
	public void setItemRequirements(LinkedList<ItemStack> rq, LinkedList<Integer> realSizes) {
		// Item stack serialized are represented through bytes, so 0-255. In
		// order to get the real amounts, we need to pass the real sizes of the
		// stacks as a separate list.

		requiredToBuild = rq;

		if (rq != null && rq.size() > 0) {
			Iterator<ItemStack> itStack = rq.iterator();
			Iterator<Integer> size = realSizes.iterator();

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
	public boolean isBuildingMaterialSlot(int i) {
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
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 0) {
			return stack.getItem() instanceof ItemBlueprint;
		} else {
			return true;
		}
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

		return renderBox.expand(50).getBoundingBox();
	}

	public void build () {
		if (!buildTracker.markTimeIfDelay(worldObj)) {
			return;
		}

		if (bluePrintBuilder != null) {
			bluePrintBuilder.buildNextSlot(worldObj, this, xCoord, yCoord, zCoord);

			updateRequirements();
		}
	}

	public void updateRequirements () {
		if (bluePrintBuilder instanceof BptBuilderBlueprint) {
			LinkedList<Integer> realSize = new LinkedList<Integer>();

			for (ItemStack stack : ((BptBuilderBlueprint) bluePrintBuilder).neededItems) {
				realSize.add(stack.stackSize);
				stack.stackSize = 0;
			}

			RPCHandler.rpcBroadcastPlayers(this, "setItemRequirements",
					((BptBuilderBlueprint) bluePrintBuilder).neededItems, realSize);
		} else {
			RPCHandler.rpcBroadcastPlayers(this, "setItemRequirements", null, null);
		}

	}

	public BptBuilderBase getBlueprint () {
		if (bluePrintBuilder != null) {
			return bluePrintBuilder;
		} else {
			return null;
		}
	}

}
