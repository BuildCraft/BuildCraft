/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.enums.EnumBlueprintType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.builders.blueprints.RecursiveBlueprintBuilder;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.*;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankManager;
import buildcraft.core.lib.inventory.*;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TileBuilder extends TileAbstractBuilder implements IHasWork, IFluidHandler, IRequestProvider, IControllable, IInventoryListener {

    private static int POWER_ACTIVATION = 500;

    public Box box = new Box();
    public PathIterator currentPathIterator;
    public Tank[] fluidTanks = new Tank[] {
        //@formatter:off
            new Tank("fluid1", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
            new Tank("fluid2", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
            new Tank("fluid3", FluidContainerRegistry.BUCKET_VOLUME * 8, this),
            new Tank("fluid4", FluidContainerRegistry.BUCKET_VOLUME * 8, this)
        //@formatter:on
    };
    public TankManager<Tank> fluidTank = new TankManager<>(fluidTanks);

    private SafeTimeTracker networkUpdateTracker = new SafeTimeTracker(BuildCraftCore.updateFactor / 2);
    private boolean shouldUpdateRequirements;

    private SimpleInventory inv = new SimpleInventory(28, "Builder", 64);
    private BptBuilderBase currentBuilder;
    private RecursiveBlueprintBuilder recursiveBuilder;
    private List<BlockPos> path;
    private List<RequirementItemStack> requiredToBuild;
    private NBTTagCompound initNBT = null;
    private boolean done = true;
    private boolean isBuilding = false;
    /** A cached value used at the client for the block state */
    private EnumBlueprintType type = EnumBlueprintType.NONE;

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

            lastDistance = (ix - to.getX()) * (ix - to.getX()) + (iy - to.getY()) * (iy - to.getY()) + (iz - to.getZ()) * (iz - to.getZ());

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

        /** Return false when reached the end of the iteration */
        public BptBuilderBase next() {
            while (true) {
                BptBuilderBase bpt;

                int newX = Math.round(ix);
                int newY = Math.round(iy);
                int newZ = Math.round(iz);

                bpt = instanciateBluePrintBuilder(new BlockPos(newX, newY, newZ), o);

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

                double distance = (ix - to.getX()) * (ix - to.getX()) + (iy - to.getY()) * (iy - to.getY()) + (iz - to.getZ()) * (iz - to.getZ());

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
        inv.addInvListener(this);
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
                BlockPos expectedTo = NBTUtils.readBlockPos(initNBT.getTag("iterator"));

                while (!done && currentBuilder != null && currentPathIterator != null) {
                    BlockPos bi = new BlockPos((int) currentPathIterator.ix, (int) currentPathIterator.iy, (int) currentPathIterator.iz);

                    if (bi.equals(expectedTo)) {
                        break;
                    }

                    iterateBpt(true);
                }
            }

            if (currentBuilder != null) {
                currentBuilder.loadBuildStateToNBT(initNBT.getCompoundTag("builderState"), this);
            }

            initNBT = null;
        }

        box.kind = Kind.STRIPES;

        for (EnumFacing face : EnumFacing.values()) {
            TileEntity tile = worldObj.getTileEntity(pos.offset(face));

            if (tile instanceof IPathProvider) {
                path = ((IPathProvider) tile).getPath();
                ((IPathProvider) tile).removeFromWorld();

                break;
            }
        }

        if (path != null && pathLasers.size() == 0) {
            createLasersForPath();
            sendNetworkUpdate();
        }

        iterateBpt(false);
    }

    public void createLasersForPath() {
        pathLasers = new LinkedList<>();
        BlockPos previous = null;

        for (BlockPos b : path) {
            if (previous != null) {
                Vec3d point5 = new Vec3d(0.5, 0.5, 0.5);
                LaserData laser = new LaserData(Utils.convert(previous).add(point5), Utils.convert(b).add(point5));

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
    public BptBuilderBase instanciateBluePrintBuilder(BlockPos pos, EnumFacing o) {
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

                scheduleRequirementUpdate();

                sendNetworkUpdate();

                return;
            }
        }

        if (currentBuilder == null || (currentBuilder.isDone(this) || forceIterate)) {
            if (path != null && path.size() > 1) {
                if (currentPathIterator == null) {
                    Iterator<BlockPos> it = path.iterator();
                    BlockPos start = it.next();

                    EnumFacing face = worldObj.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING);
                    currentPathIterator = new PathIterator(start, it, face);
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
                        EnumFacing face = worldObj.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING);
                        recursiveBuilder = new RecursiveBlueprintBuilder(bpt, worldObj, pos, face.getOpposite());

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
                InvUtils.dropItems(getWorld(), getStackInSlot(0), pos);
            }

            setInventorySlotContents(0, null);
            box.reset();
            sendNetworkUpdate();
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
    public ItemStack removeStackFromSlot(int slot) {
        return inv.removeStackFromSlot(slot);
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
        return worldObj.getTileEntity(pos) == this;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        inv.readFromNBT(nbttagcompound);

        if (nbttagcompound.hasKey("box")) {
            box.initialize(nbttagcompound.getCompoundTag("box"));
        }

        type = EnumBlueprintType.getType(getStackInSlot(0));

        if (nbttagcompound.hasKey("path")) {
            path = new LinkedList<>();
            NBTTagList list = nbttagcompound.getTagList("path", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < list.tagCount(); ++i) {
                path.add(NBTUtils.readBlockPos(list.get(i)));
            }
        }

        done = nbttagcompound.getBoolean("done");
        fluidTank.readFromNBT(nbttagcompound);

        // The rest of load has to be done upon initialize.
        initNBT = (NBTTagCompound) nbttagcompound.getCompoundTag("bptBuilder").copy();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
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
                list.appendTag(NBTUtils.writeBlockPos(i));
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
            BlockPos nPos = new BlockPos((int) currentPathIterator.ix, (int) currentPathIterator.iy, (int) currentPathIterator.iz);
            bptNBT.setTag("iterator", NBTUtils.writeBlockPos(nPos));
        }

        nbttagcompound.setTag("bptBuilder", bptNBT);
		return nbttagcompound;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        destroy();
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            return;
        }

        if (shouldUpdateRequirements && networkUpdateTracker.markTimeIfDelay(worldObj)) {
            updateRequirements();
            shouldUpdateRequirements = false;
        }

        if ((currentBuilder == null || currentBuilder.isDone(this)) && box.isInitialized()) {
            box.reset();

            sendNetworkUpdate();

            return;
        }

        iterateBpt(false);

        if (mode != Mode.Off) {
            if (getWorld().getWorldInfo().getGameType() == GameType.CREATIVE || getBattery().getEnergyStored() > POWER_ACTIVATION) {
                build();
            }
        }

        if (!isBuilding && this.isBuildingBlueprint()) {
            scheduleRequirementUpdate();
        }
        isBuilding = this.isBuildingBlueprint();

        if (done) {// TODO (PASS 3): This is useless right? Is/was this needed for anything?
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
        return worldObj.isRemote ? requiredToBuild : (currentBuilder instanceof BptBuilderBlueprint ? ((BptBuilderBlueprint) currentBuilder)
                .getNeededItems() : null);
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        super.receiveCommand(command, side, sender, stream);
        if (side.isClient()) {
            if ("clearItemRequirements".equals(command)) {
                requiredToBuild = null;
            } else if ("setItemRequirements".equals(command)) {
                int size = stream.readUnsignedMedium();
                requiredToBuild = new ArrayList<>();
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
                        BCLog.logger.error("Corrupt ItemStack in TileBuilder.receiveCommand! This should not happen! (ID " + itemId + ", damage "
                            + itemDamage + ")");
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
                if (isUseableByPlayer(player) && player.getDistanceSq(pos) <= 64) {
                    fluidTanks[id].setFluid(null);
                    sendNetworkUpdate();
                }
            }
        }
    }

    private Packet getItemRequirementsPacket(List<RequirementItemStack> itemsIn) {
        if (itemsIn != null) {
            final List<RequirementItemStack> items = new ArrayList<>();
            items.addAll(itemsIn);

            return new PacketCommand(this, "setItemRequirements", new CommandWriter() {
                @Override
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
        Box renderBox = new Box(this).extendToEncompass(box);

        for (LaserData l : pathLasers) {
            renderBox = renderBox.extendToEncompass(l.head);
            renderBox = renderBox.extendToEncompass(l.tail);
        }

        return renderBox.expand(50).getBoundingBox();
    }

    public void build() {
        if (currentBuilder != null) {
            if (currentBuilder.buildNextSlot(worldObj, this)) {
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

            ITransactor t = Transactor.getTransactorFor(this, null);
            ItemStack added = t.add(toAdd, true);

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
        stream.writeByte(getType().ordinal());
    }

    @Override
    public void readData(ByteBuf stream) {
        super.readData(stream);
        box.readData(stream);
        fluidTank.readData(stream);
        byte type = stream.readByte();
        EnumBlueprintType old = this.type;
        this.type = EnumBlueprintType.valueOf(type);
        if (old != this.type) {
            // Needed because BLUEPRINT_TYPE is not part of the integer block meta, so we must tell minecraft that this
            // changed ourselves
            worldObj.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    public EnumBlueprintType getType() {
        return type;
    }

    @Override
    public void onChange(int slot, ItemStack before, ItemStack after) {
        if (slot == 0 && worldObj != null && !worldObj.isRemote) {
            EnumBlueprintType beforeType = EnumBlueprintType.getType(before);
            EnumBlueprintType afterType = EnumBlueprintType.getType(after);
            if (beforeType != afterType) {
                type = afterType;
                sendNetworkUpdate();
            }
        }
    }

    @Override
    public Tank[] getFluidTanks() {
        return fluidTanks;
    }
}
