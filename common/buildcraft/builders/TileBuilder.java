/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.common.util.Constants;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import net.minecraft.util.BlockPos;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.Position;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.StackRequest;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.RecursiveBlueprintBuilder;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.fluids.Tank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.robots.ResourceIdRequest;
import buildcraft.core.robots.RobotRegistry;
import buildcraft.core.utils.Utils;

public class TileBuilder extends TileAbstractBuilder implements IHasWork, IFluidHandler, IRequestProvider {

	private static int POWER_ACTIVATION = 500;

	public Box box = new Box();
	public PathIterator currentPathIterator;
	public Tank[] fluidTanks = new Tank[] {
			new Tank("fluid1", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
			new Tank("fluid2", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
			new Tank("fluid3", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
			new Tank("fluid4", FluidContainerRegistry.BUCKET_VOLUME * 8, this)
	};
	public TankManager<Tank> fluidTank = new TankManager<Tank>(fluidTanks);

	private SimpleInventory inv = new SimpleInventory(28, "Builder", 64);
	private BptBuilderBase currentBuilder;
	private RecursiveBlueprintBuilder recursiveBuilder;
	private LinkedList<BlockPos> path;
	private ArrayList<ItemStack> requiredToBuild;
	private NBTTagCompound initNBT = null;
	private boolean done = true;
	private boolean isBuilding = false;
	
	private class PathIterator {

		public Iterator<BlockPos> currentIterator;
		public double cx, cy, cz;
		public float ix, iy, iz;
		public BlockPos to;
		public double lastDistance;
		AxisAlignedBB oldBoundingBox = null;
		EnumFacing o = null;

		public PathIterator(BlockPos from, Iterator<BlockPos> it, EnumFacing initialDir) {
			this.to = it.next();

			currentIterator = it;

			double dx = to.getX() - from.getX();
			double dy = to.getY() - from.getY();
			double dz = to.getZ() - from.getZ();

			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);

			cx = dx / size / 10;
			cy = dy / size / 10;
			cz = dz / size / 10;

			ix = from.getX();
			iy = from.getY();
			iz = from.getZ();

			lastDistance = (ix - to.getX()) * (ix - to.getX()) + (iy - to.getY())
					* (iy - to.getY()) + (iz - to.getZ()) * (iz - to.getZ());

			if (dx == 0 && dz == 0) {
				o = initialDir;
			} else if (Math.abs(dx) > Math.abs(dz)) {
				if (dx > 0) {
					o = EnumFacing.EAST;
				} else {
					o = EnumFacing.WEST;
				}
			} else {
				if (dz > 0) {
					o = EnumFacing.SOUTH;
				} else {
					o = EnumFacing.NORTH;
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

					if (bpt != null) {
						return bpt;
					}
				}

				ix += cx;
				iy += cy;
				iz += cz;

				double distance = (ix - to.getX()) * (ix - to.getX()) + (iy - to.getY())
						* (iy - to.getY()) + (iz - to.getZ()) * (iz - to.getZ());

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
				BlockPos expectedTo = Utils.readBlockPos(initNBT.getCompoundTag("iterator"));

				while (!done && currentBuilder != null && currentPathIterator != null) {
					BlockPos bi = new BlockPos((int) currentPathIterator.ix,
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

		for (int x = pos.getX() - 1; x <= pos.getX() + 1; ++x) {
			for (int y = pos.getY() - 1; y <= pos.getY() + 1; ++y) {
				for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; ++z) {
					TileEntity tile = worldObj.getTileEntity(new BlockPos(x, y, z));

					if (tile instanceof TilePathMarker) {
						path = ((TilePathMarker) tile).getPath();

						for (BlockPos b : path) {
							worldObj.setBlockToAir(b);

							BuildCraftBuilders.pathMarkerBlock.dropBlockAsItem(
									worldObj, b,
									worldObj.getBlockState(b), 0);
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
		BlockPos previous = null;

		for (BlockPos b : path) {
			if (previous != null) {
				LaserData laser = new LaserData(new Position(previous.getX() + 0.5,
						previous.getY() + 0.5, previous.getZ() + 0.5), new Position(
						b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5));

				pathLasers.add(laser);
			}

			previous = b;
		}
	}

	public BlueprintBase instanciateBlueprint() {
		BlueprintBase bpt = null;

		try {
			bpt = ItemBlueprint.loadBlueprint(getStackInSlot(0));
		} catch (Throwable t) {
			setInventorySlotContents(0, null);
			t.printStackTrace();
			return null;
		}

		return bpt;
	}

	@Deprecated
	public BptBuilderBase instanciateBluePrintBuilder(int x, int y, int z, EnumFacing o) {
		BlockPos pos = new BlockPos(x, y, z);
		BlueprintBase bpt = instanciateBlueprint();
        if (bpt == null) {
            return null;
        }

		bpt = bpt.adjustToWorld(worldObj, pos, o);

		if (bpt != null) {
			if (getStackInSlot(0).getItem() instanceof ItemBlueprintStandard) {
				return new BptBuilderBlueprint((Blueprint) bpt, worldObj, pos);
			} else if (getStackInSlot(0).getItem() instanceof ItemBlueprintTemplate) {
				return new BptBuilderTemplate(bpt, worldObj, pos);
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

				updateRequirements();

				sendNetworkUpdate();

				return;
			}
		}

		if (currentBuilder == null || (currentBuilder.isDone(this) || forceIterate)) {
			if (path != null && path.size() > 1) {
				if (currentPathIterator == null) {
					Iterator<BlockPos> it = path.iterator();
					BlockPos start = it.next();
					currentPathIterator = new PathIterator(start, it, ((EnumFacing)worldObj.getBlockState(pos).getValue(BlockBuildCraft.FACING_PROP)).getOpposite());
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

				updateRequirements();
			} else {
				if (currentBuilder != null && currentBuilder.isDone(this)) {
					currentBuilder.postProcessing(worldObj);
					currentBuilder = recursiveBuilder.nextBuilder();

					updateRequirements();
				} else {
					BlueprintBase bpt = instanciateBlueprint();

					if (bpt != null) {
						recursiveBuilder = new RecursiveBlueprintBuilder(bpt, worldObj, pos, ((EnumFacing)worldObj.getBlockState(pos).getValue(BlockBuildCraft.FACING_PROP)).getOpposite());

						currentBuilder = recursiveBuilder.nextBuilder();

						updateRequirements();
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
				InvUtils.dropItems(worldObj, getStackInSlot(0), pos);
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
	public String getName() {
		return "Builder";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(getPos()) == this;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		inv.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		if (nbttagcompound.hasKey("path")) {
			path = new LinkedList<BlockPos>();
			NBTTagList list = nbttagcompound.getTagList("path",
					Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); ++i) {
				path.add(Utils.readBlockPos(list.getCompoundTagAt(i)));
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

			for (BlockPos i : path) {
				NBTTagCompound c = new NBTTagCompound();
				Utils.writeBlockPos(c, i);
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
			Utils.writeBlockPos(iteratorNBT, new BlockPos((int) currentPathIterator.ix,
					(int) currentPathIterator.iy, (int) currentPathIterator.iz));
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
	public void update() {
		super.update();

		if (worldObj.isRemote) {
			return;
		}

		if (currentBuilder != null) {
			currentBuilder.removeDoneBuilders(this);
		}

		if ((currentBuilder == null || currentBuilder.isDone(this))
				&& box.isInitialized()) {
			box.reset();

			sendNetworkUpdate();

			return;
		}

		iterateBpt(false);

		if (worldObj.getWorldInfo().getGameType() == GameType.CREATIVE) {
			build();
		} else if (getBattery().getEnergyStored() > POWER_ACTIVATION) {
			build();
		}

		if (!isBuilding && this.isBuildingBlueprint()) {
			updateRequirements();
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

	public Collection<ItemStack> getNeededItems() {
		return requiredToBuild;
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		super.receiveCommand(command, side, sender, stream);
		if (side.isClient()) {
			if ("clearItemRequirements".equals(command)) {
				requiredToBuild = null;
			} else if ("setItemRequirements".equals(command)) {
				int size = stream.readUnsignedShort();
				requiredToBuild = new ArrayList<ItemStack>();
				for (int i = 0; i < size; i++) {
					ItemStack stack = Utils.readStack(stream);
					stack.stackSize = Math.min(999, stream.readUnsignedShort());
					requiredToBuild.add(stack);
				}
			}
		} else if (side.isServer()) {
			EntityPlayer player = (EntityPlayer) sender;
			if ("eraseFluidTank".equals(command)) {
				int id = stream.readInt();
				if (id < 0 || id >= fluidTanks.length) {
					return;
				}
				if (isUseableByPlayer(player) && player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= 64) {
					fluidTanks[id].setFluid(null);
					sendNetworkUpdate();
				}
			}
		}
	}

	private BuildCraftPacket getItemRequirementsPacket(final ArrayList<ItemStack> items) {
		if (items != null) {
			return new PacketCommand(this, "setItemRequirements", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeShort(items.size());
					if (items != null) {
						for (ItemStack rb : items) {
							Utils.writeStack(data, rb);
							data.writeShort(rb.stackSize);
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
	public boolean hasCustomName() {
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
		if (currentBuilder != null) {
			if (currentBuilder.buildNextSlot(worldObj, this, pos.getX(), pos.getY(), pos.getZ())) {
				updateRequirements();
			}
		}
	}

	public void updateRequirements() {
		ArrayList<ItemStack> reqCopy = null;
		if (currentBuilder instanceof BptBuilderBlueprint) {
			currentBuilder.initialize();
			reqCopy = ((BptBuilderBlueprint) currentBuilder).neededItems;
		}

		for (EntityPlayer p : guiWatchers) {
			BuildCraftCore.instance.sendToPlayer(p, getItemRequirementsPacket(reqCopy));
		}
	}
	
	public void updateRequirements(EntityPlayer caller) {
		ArrayList<ItemStack> reqCopy = null;
		if (currentBuilder instanceof BptBuilderBlueprint) {
			currentBuilder.initialize();
			reqCopy = ((BptBuilderBlueprint) currentBuilder).neededItems;
		}

		BuildCraftCore.instance.sendToPlayer(caller, getItemRequirementsPacket(reqCopy));
	}

	public BptBuilderBase getBlueprint () {
		if (currentBuilder != null) {
			return currentBuilder;
		} else {
			return null;
		}
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		return null;
	}

	public boolean drainBuild(FluidStack fluidStack, boolean realDrain) {
		for (Tank tank : fluidTanks) {
			if (tank.getFluidType() == fluidStack.getFluid()) {
				return tank.getFluidAmount() >= fluidStack.amount && tank.drain(fluidStack.amount, realDrain).amount > 0;
			}
		}
		return false;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
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
	public boolean canFill(EnumFacing from, Fluid fluid) {
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
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		return fluidTank.getTankInfo(from);
	}

	@Override
	public int getNumberOfRequests() {
		if (currentBuilder == null) {
			return 0;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return 0;
		} else {
			BptBuilderBlueprint bpt = (BptBuilderBlueprint) currentBuilder;

			return bpt.neededItems.size();
		}
	}

	@Override
	public StackRequest getAvailableRequest(int i) {
		if (currentBuilder == null) {
			return null;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return null;
		} else {
			BptBuilderBlueprint bpt = (BptBuilderBlueprint) currentBuilder;

			if (bpt.neededItems.size() <= i) {
				return null;
			}

			ItemStack requirement = bpt.neededItems.get(i);

			int qty = quantityMissing(requirement);

			if (qty <= 0) {
				return null;
			}

			StackRequest request = new StackRequest();

			request.index = i;
			request.requester = this;
			request.stack = requirement;

			return request;
		}
	}

	@Override
	public boolean takeRequest(int i, EntityRobotBase robot) {
		if (currentBuilder == null) {
			return false;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return false;
		} else {
			return RobotRegistry.getRegistry(worldObj).take(new ResourceIdRequest(this, i), robot);
		}
	}

	@Override
	public ItemStack provideItemsForRequest(int i, ItemStack stack) {
		if (currentBuilder == null) {
			return stack;
		} else if (!(currentBuilder instanceof BptBuilderBlueprint)) {
			return stack;
		} else {
			BptBuilderBlueprint bpt = (BptBuilderBlueprint) currentBuilder;

			if (bpt.neededItems.size() <= i) {
				return stack;
			}

			ItemStack requirement = bpt.neededItems.get(i);

			int qty = quantityMissing(requirement);

			if (qty <= 0) {
				return stack;
			}

			ItemStack toAdd = stack.copy();

			if (qty < toAdd.stackSize) {
				toAdd.stackSize = qty;
			}

			ITransactor t = Transactor.getTransactorFor(this);
			ItemStack added = t.add(toAdd, null, true);

			if (added.stackSize >= stack.stackSize) {
				return null;
			} else {
				stack.stackSize -= added.stackSize;
				return stack;
			}
		}
	}

	private int quantityMissing(ItemStack requirement) {
		int left = requirement.stackSize;

		for (IInvSlot slot : InventoryIterator.getIterable(this)) {
			if (slot.getStackInSlot() != null) {
				if (StackHelper.isMatchingItem(requirement, slot.getStackInSlot())) {
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

	@Override
	public void openInventory(EntityPlayer playerIn) {
	}

	@Override
	public void closeInventory(EntityPlayer playerIn) {		
	}
}
