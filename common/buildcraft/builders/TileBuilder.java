/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldSettings.GameType;

import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.builders.blueprints.RecursiveBlueprintBuilder;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.RequirementItemStack;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankManager;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;

public class TileBuilder extends TileAbstractBuilder implements IHasWork, IFluidHandler, IRequestProvider, IControllable {

	private static int POWER_ACTIVATION = 500;

	public Box box = new Box();
	public PathIterator currentPathIterator;
	public Tank[] fluidTanks = new Tank[]{
			new Tank("fluid1", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
			new Tank("fluid2", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
			new Tank("fluid3", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
			new Tank("fluid4", FluidContainerRegistry.BUCKET_VOLUME * 8, this)
	};
	public TankManager<Tank> fluidTank = new TankManager<Tank>(fluidTanks);

	private SafeTimeTracker networkUpdateTracker = new SafeTimeTracker(BuildCraftCore.updateFactor / 2);
	private boolean shouldUpdateRequirements;

	private SimpleInventory inv = new SimpleInventory(28, "Builder", 64);
	private BptBuilderBase currentBuilder;
	private RecursiveBlueprintBuilder recursiveBuilder;
	private List<BlockIndex> path;
	private ArrayList<RequirementItemStack> requiredToBuild;
	private NBTTagCompound initNBT = null;
	private boolean done = true;
	private boolean isBuilding = false;

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

				bpt = instanciateBluePrintBuilder(newX, newY, newZ, o);

				if (bpt == null) {
					return null;
				}

				AxisAlignedBB boundingBox = bpt.getBoundingBox();

				if (oldBoundingBox == null || !collision(oldBoundingBox, boundingBox)) {
					oldBoundingBox = boundingBox;
					return bpt;
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

				while (!done && currentBuilder != null && currentPathIterator != null) {
					BlockIndex bi = new BlockIndex((int) currentPathIterator.ix,
							(int) currentPathIterator.iy, (int) currentPathIterator.iz);

					if (bi.equals(expectedTo)) {
						break;
					}

					iterateBpt(true);
				}
			}

			if (currentBuilder != null) {
				currentBuilder.loadBuildStateToNBT(
						initNBT.getCompoundTag("builderState"), this);
			}

			initNBT = null;
		}

		box.kind = Kind.STRIPES;

		for (int x = xCoord - 1; x <= xCoord + 1; ++x) {
			for (int y = yCoord - 1; y <= yCoord + 1; ++y) {
				for (int z = zCoord - 1; z <= zCoord + 1; ++z) {
					TileEntity tile = worldObj.getTileEntity(x, y, z);

					if (tile instanceof IPathProvider) {
						path = ((IPathProvider) tile).getPath();
						((IPathProvider) tile).removeFromWorld();

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

	public BlueprintBase instanciateBlueprint() {
		BlueprintBase bpt;

		try {
			bpt = ItemBlueprint.loadBlueprint(getStackInSlot(0));
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

		return bpt;
	}

	@Deprecated
	public BptBuilderBase instanciateBluePrintBuilder(int x, int y, int z, ForgeDirection o) {
		BlueprintBase bpt = instanciateBlueprint();
		if (bpt == null) {
			return null;
		}

		bpt = bpt.adjustToWorld(worldObj, x, y, z, o);

		if (bpt != null) {
			if (getStackInSlot(0).getItem() instanceof ItemBlueprintStandard) {
				return new BptBuilderBlueprint((Blueprint) bpt, worldObj, x, y, z);
			} else if (getStackInSlot(0).getItem() instanceof ItemBlueprintTemplate) {
				return new BptBuilderTemplate(bpt, worldObj, x, y, z);
			}
		}
		return null;
	}

	public void iterateBpt(boolean forceIterate) {
		if (getStackInSlot(0) == null || !(getStackInSlot(0).getItem() instanceof ItemBlueprint)) {
			if (box.isInitialized()) {
				if (currentBuilder != null) {
					currentBuilder = null;
				}

				if (box.isInitialized()) {
					box.reset();
				}

				if (currentPathIterator != null) {
					currentPathIterator = null;
				}

				scheduleRequirementUpdate();

				sendNetworkUpdate();

				return;
			}
		}

		if (currentBuilder == null || (currentBuilder.isDone(this) || forceIterate)) {
			if (path != null && path.size() > 1) {
				if (currentPathIterator == null) {
					Iterator<BlockIndex> it = path.iterator();
					BlockIndex start = it.next();
					currentPathIterator = new PathIterator(start, it,
							ForgeDirection.values()[worldObj.getBlockMetadata(
									xCoord, yCoord, zCoord)].getOpposite());
				}

				if (currentBuilder != null && currentBuilder.isDone(this)) {
					currentBuilder.postProcessing(worldObj);
				}

				currentBuilder = currentPathIterator.next();

				if (currentBuilder != null) {
					box.reset();
					box.initialize(currentBuilder);
					sendNetworkUpdate();
				}

				if (currentBuilder == null) {
					currentPathIterator = currentPathIterator.iterate();
				}

				if (currentPathIterator == null) {
					done = true;
				} else {
					done = false;
				}

				scheduleRequirementUpdate();
			} else {
				if (currentBuilder != null && currentBuilder.isDone(this)) {
					currentBuilder.postProcessing(worldObj);
					currentBuilder = recursiveBuilder.nextBuilder();

					scheduleRequirementUpdate();
				} else {
					BlueprintBase bpt = instanciateBlueprint();

					if (bpt != null) {
						recursiveBuilder = new RecursiveBlueprintBuilder(bpt, worldObj, xCoord, yCoord, zCoord,
								ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite());

						currentBuilder = recursiveBuilder.nextBuilder();

						scheduleRequirementUpdate();
					}
				}

				if (currentBuilder == null) {
					done = true;
				} else {
					box.initialize(currentBuilder);
					sendNetworkUpdate();
					done = false;
				}
			}
		}

		if (done && getStackInSlot(0) != null) {
			boolean dropBlueprint = true;
			for (int i = 1; i < getSizeInventory(); ++i) {
				if (getStackInSlot(i) == null) {
					setInventorySlotContents(i, getStackInSlot(0));
					dropBlueprint = false;
					break;
				}
			}
			if (dropBlueprint) {
				InvUtils.dropItems(getWorldObj(), getStackInSlot(0), xCoord, yCoord, zCoord);
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
				BuildCraftCore.instance.sendToWorld(new PacketCommand(this, "clearItemRequirements", null), worldObj);
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
		fluidTank.readFromNBT(nbttagcompound);

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
		fluidTank.writeToNBT(nbttagcompound);

		NBTTagCompound bptNBT = new NBTTagCompound();

		if (currentBuilder != null) {
			NBTTagCompound builderCpt = new NBTTagCompound();
			currentBuilder.saveBuildStateToNBT(builderCpt, this);
			bptNBT.setTag("builderState", builderCpt);
		}

		if (currentPathIterator != null) {
			NBTTagCompound iteratorNBT = new NBTTagCompound();
			new BlockIndex((int) currentPathIterator.ix,
					(int) currentPathIterator.iy, (int) currentPathIterator.iz)
					.writeTo(iteratorNBT);
			bptNBT.setTag("iterator", iteratorNBT);
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

		if (shouldUpdateRequirements && networkUpdateTracker.markTimeIfDelay(worldObj)) {
			updateRequirements();
			shouldUpdateRequirements = false;
		}

		if ((currentBuilder == null || currentBuilder.isDone(this))
				&& box.isInitialized()) {
			box.reset();

			sendNetworkUpdate();

			return;
		}

		iterateBpt(false);

		if (mode != Mode.Off) {
			if (getWorldObj().getWorldInfo().getGameType() == GameType.CREATIVE
					|| getBattery().getEnergyStored() > POWER_ACTIVATION) {
				build();
			}
		}

		if (!isBuilding && this.isBuildingBlueprint()) {
			scheduleRequirementUpdate();
		}
		isBuilding = this.isBuildingBlueprint();

		if (done) {
			return;
		} else if (getBattery().getEnergyStored() < 25) {
			return;
		}
	}

	@Override
	public boolean hasWork() {
		return !done;
	}

	public boolean isBuildingBlueprint() {
		return getStackInSlot(0) != null && getStackInSlot(0).getItem() instanceof ItemBlueprint;
	}

	public List<RequirementItemStack> getNeededItems() {
		return worldObj.isRemote ? requiredToBuild : (currentBuilder instanceof BptBuilderBlueprint ? ((BptBuilderBlueprint) currentBuilder).getNeededItems() : null);
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		super.receiveCommand(command, side, sender, stream);
		if (side.isClient()) {
			if ("clearItemRequirements".equals(command)) {
				requiredToBuild = null;
			} else if ("setItemRequirements".equals(command)) {
				int size = stream.readUnsignedMedium();
				requiredToBuild = new ArrayList<RequirementItemStack>();
				for (int i = 0; i < size; i++) {
					int itemId = stream.readUnsignedShort();
					int itemDamage = stream.readShort();
					int stackSize = stream.readUnsignedMedium();
					boolean hasCompound = stackSize >= 0x800000;

					ItemStack stack = new ItemStack(Item.getItemById(itemId), 1, itemDamage);
					if (hasCompound) {
						stack.setTagCompound(NetworkUtils.readNBT(stream));
					}

					if (stack.getItem() != null) {
						requiredToBuild.add(new RequirementItemStack(stack, stackSize & 0x7FFFFF));
					} else {
						BCLog.logger.error("Corrupt ItemStack in TileBuilder.receiveCommand! This should not happen! (ID " + itemId + ", damage " + itemDamage + ")");
					}
				}
			}
		} else if (side.isServer()) {
			EntityPlayer player = (EntityPlayer) sender;
			if ("eraseFluidTank".equals(command)) {
				int id = stream.readInt();
				if (id < 0 || id >= fluidTanks.length) {
					return;
				}
				if (isUseableByPlayer(player) && player.getDistanceSq(xCoord, yCoord, zCoord) <= 64) {
					fluidTanks[id].setFluid(null);
					sendNetworkUpdate();
				}
			}
		}
	}

	private Packet getItemRequirementsPacket(List<RequirementItemStack> itemsIn) {
		if (itemsIn != null) {
			final List<RequirementItemStack> items = new ArrayList<RequirementItemStack>();
			items.addAll(itemsIn);

			return new PacketCommand(this, "setItemRequirements", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeMedium(items.size());
					for (RequirementItemStack rb : items) {
						data.writeShort(Item.getIdFromItem(rb.stack.getItem()));
						data.writeShort(rb.stack.getItemDamage());
						data.writeMedium((rb.stack.hasTagCompound() ? 0x800000 : 0x000000) | Math.min(0x7FFFFF, rb.size));
						if (rb.stack.hasTagCompound()) {
							NetworkUtils.writeNBT(data, rb.stack.getTagCompound());
						}
					}
				}
			});
		} else {
			return new PacketCommand(this, "clearItemRequirements", null);
		}
	}

	@Override
	public boolean isBuildingMaterialSlot(int i) {
		return i != 0;
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
		Box renderBox = new Box(this).extendToEncompass(box);

		for (LaserData l : pathLasers) {
			renderBox = renderBox.extendToEncompass(l.head);
			renderBox = renderBox.extendToEncompass(l.tail);
		}

		return renderBox.expand(50).getBoundingBox();
	}

	public void build() {
		if (currentBuilder != null) {
			if (currentBuilder.buildNextSlot(worldObj, this, xCoord, yCoord, zCoord)) {
				scheduleRequirementUpdate();
			}
		}
	}

	private void updateRequirements() {
		List<RequirementItemStack> reqCopy = null;
		if (currentBuilder instanceof BptBuilderBlueprint) {
			currentBuilder.initialize();
			reqCopy = ((BptBuilderBlueprint) currentBuilder).getNeededItems();
		}

		for (EntityPlayer p : guiWatchers) {
			BuildCraftCore.instance.sendToPlayer(p, getItemRequirementsPacket(reqCopy));
		}
	}

	public void scheduleRequirementUpdate() {
		shouldUpdateRequirements = true;
	}

	public void updateRequirementsOnGuiOpen(EntityPlayer caller) {
		List<RequirementItemStack> reqCopy = null;
		if (currentBuilder instanceof BptBuilderBlueprint) {
			currentBuilder.initialize();
			reqCopy = ((BptBuilderBlueprint) currentBuilder).getNeededItems();
		}

		BuildCraftCore.instance.sendToPlayer(caller, getItemRequirementsPacket(reqCopy));
	}

	public BptBuilderBase getBlueprint() {
		if (currentBuilder != null) {
			return currentBuilder;
		} else {
			return null;
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean drainBuild(FluidStack fluidStack, boolean realDrain) {
		for (Tank tank : fluidTanks) {
			if (tank.getFluidType() == fluidStack.getFluid()) {
				return tank.getFluidAmount() >= fluidStack.amount && tank.drain(fluidStack.amount, realDrain).amount > 0;
			}
		}
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		Fluid fluid = resource.getFluid();
		Tank emptyTank = null;
		for (Tank tank : fluidTanks) {
			Fluid type = tank.getFluidType();
			if (type == fluid) {
				int used = tank.fill(resource, doFill);
				if (used > 0 && doFill) {
					sendNetworkUpdate();
				}
				return used;
			} else if (emptyTank == null && tank.isEmpty()) {
				emptyTank = tank;
			}
		}
		if (emptyTank != null) {
			int used = emptyTank.fill(resource, doFill);
			if (used > 0 && doFill) {
				sendNetworkUpdate();
			}
			return used;
		}
		return 0;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		boolean emptyAvailable = false;
		for (Tank tank : fluidTanks) {
			Fluid type = tank.getFluidType();
			if (type == fluid) {
				return !tank.isFull();
			} else if (!emptyAvailable) {
				emptyAvailable = tank.isEmpty();
			}
		}
		return emptyAvailable;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return fluidTank.getTankInfo(from);
	}

	@Override
	public int getRequestsCount() {
		if (currentBuilder == null) {
			return 0;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return 0;
		} else {
			BptBuilderBlueprint bpt = (BptBuilderBlueprint) currentBuilder;

			return bpt.getNeededItems().size();
		}
	}

	@Override
	public ItemStack getRequest(int slot) {
		if (currentBuilder == null) {
			return null;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return null;
		} else {
			BptBuilderBlueprint bpt = (BptBuilderBlueprint) currentBuilder;
			List<RequirementItemStack> neededItems = bpt.getNeededItems();

			if (neededItems.size() <= slot) {
				return null;
			}

			RequirementItemStack requirement = neededItems.get(slot);

			int qty = quantityMissing(requirement.stack, requirement.size);

			if (qty <= 0) {
				return null;
			}
			ItemStack requestStack = requirement.stack.copy();
			requestStack.stackSize = qty;
			return requestStack;
		}
	}

	@Override
	public ItemStack offerItem(int slot, ItemStack stack) {
		if (currentBuilder == null) {
			return stack;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return stack;
		} else {
			BptBuilderBlueprint bpt = (BptBuilderBlueprint) currentBuilder;
			List<RequirementItemStack> neededItems = bpt.getNeededItems();

			if (neededItems.size() <= slot) {
				return stack;
			}

			RequirementItemStack requirement = neededItems.get(slot);

			int qty = quantityMissing(requirement.stack, requirement.size);

			if (qty <= 0) {
				return stack;
			}

			ItemStack toAdd = stack.copy();

			if (qty < toAdd.stackSize) {
				toAdd.stackSize = qty;
			}

			ITransactor t = Transactor.getTransactorFor(this);
			ItemStack added = t.add(toAdd, ForgeDirection.UNKNOWN, true);

			if (added.stackSize >= stack.stackSize) {
				return null;
			} else {
				stack.stackSize -= added.stackSize;
				return stack;
			}
		}
	}

	private int quantityMissing(ItemStack requirement, int amount) {
		int left = amount <= 0 ? requirement.stackSize : amount;

		for (IInvSlot slot : InventoryIterator.getIterable(this)) {
			if (slot.getStackInSlot() != null) {
				// TODO: This should also be using the Schematic version of the function!
				if (StackHelper.isEqualItem(requirement, slot.getStackInSlot())) {
					if (slot.getStackInSlot().stackSize >= left) {
						return 0;
					} else {
						left -= slot.getStackInSlot().stackSize;
					}
				}
			}
		}

		return left;
	}

	@Override
	public boolean acceptsControlMode(Mode mode) {
		return mode == Mode.Off || mode == Mode.On;
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		box.writeData(stream);
		fluidTank.writeData(stream);
	}

	@Override
	public void readData(ByteBuf stream) {
		super.readData(stream);
		box.readData(stream);
		fluidTank.readData(stream);
	}

	public Tank[] getFluidTanks() {
		return fluidTanks;
	}
}
