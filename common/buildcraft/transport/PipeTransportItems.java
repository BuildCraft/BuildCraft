/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.*;

public class PipeTransportItems extends PipeTransport implements IDebuggable {
    private enum ReceiveType {
        ALLOWED,
        CLOGGED,
        NONE;
    }

    public static final int MAX_PIPE_STACKS = 32;
    public boolean allowBouncing = false;
    public final TravelerSet items = new TravelerSet(this);

    private static final int DESTINATION_REFRESH_SPEED = 20;
    private final int REFRESH_OFFSET = BuildCraftCore.random.nextInt(DESTINATION_REFRESH_SPEED);

    @Override
    public IPipeTile.PipeType getPipeType() {
        return IPipeTile.PipeType.ITEM;
    }

    public void readjustSpeed(TravelingItem item) {
        PipeEventItem.AdjustSpeed event = new PipeEventItem.AdjustSpeed(container.pipe, item);
        container.pipe.eventBus.handleEvent(event);
        if (!event.handled) {
            defaultReadjustSpeed(item, event.slowdownAmount);
        }
    }

    protected void defaultReadjustSpeed(TravelingItem item, float slowdownAmount) {
        float speed = item.getSpeed();

        if (speed > TransportConstants.PIPE_MAX_SPEED) {
            speed = TransportConstants.PIPE_MAX_SPEED;
        }

        if (speed > TransportConstants.PIPE_MIN_SPEED) {
            speed -= slowdownAmount;
        }

        if (speed < TransportConstants.PIPE_MIN_SPEED) {
            speed = TransportConstants.PIPE_MIN_SPEED;
        }

        item.setSpeed(speed);
    }

    private void readjustPosition(TravelingItem item) {
        Vec3 middle = Utils.convertMiddle(container.getPos());
        Vec3 littleBitBelow0Point5 = new Vec3(0.49, 0.49, 0.49);
        Vec3 newPos = Utils.clamp(item.pos, middle.subtract(littleBitBelow0Point5), middle.add(littleBitBelow0Point5));

        if (item.input.getAxis() != Axis.Y) {
            newPos = new Vec3(newPos.xCoord, container.getPos().getY() + TransportUtils.getPipeFloorOf(item.getItemStack()), newPos.zCoord);
        }

        item.pos = newPos;
    }

    public boolean injectItem(TravelingItem item, EnumFacing inputOrientation, boolean doAdd) {
        return injectItem(item, inputOrientation, doAdd, false);
    }

    protected boolean injectItem(TravelingItem item, EnumFacing inputOrientation, boolean doAdd, boolean force) {
        if (item.isCorrupted()) {
            // Safe guard - if for any reason the item is corrupted at this
            // stage, avoid adding it to the pipe to avoid further exceptions.
            return false;
        }

        if (!force) {
            for (TravelingItem item1 : items) {
                if (item1.input != null && item1.input.getAxis() == inputOrientation.getAxis() && !item1.isMoving()) {
                    return false;
                }
            }
        }

        if (doAdd) {
            item.reset();
            item.input = inputOrientation;

            readjustSpeed(item);
            readjustPosition(item);

            PipeEventItem.Entered event = new PipeEventItem.Entered(container.pipe, item);
            container.pipe.eventBus.handleEvent(event);
            if (event.cancelled) {
                return false;
            }
        }

        int itemStackCount = getNumberOfStacks();

        if (itemStackCount >= MAX_PIPE_STACKS) {
            groupEntities();
            itemStackCount = getNumberOfStacks();
        }

        if (itemStackCount >= MAX_PIPE_STACKS) {
            return false;
        }

        if (doAdd) {
            if (!container.getWorld().isRemote) {
                item.output = resolveDestination(item);
            }

            items.add(item);

            if (!container.getWorld().isRemote) {
                sendTravelerPacket(item, false);
            }
        }

        return true;
    }

    private void destroyPipe() {
        BlockUtils.explodeBlock(container.getWorld(), container.getPos());
        container.getWorld().setBlockToAir(container.getPos());
    }

    /** Bounces the item back into the pipe without changing the items map.
     *
     * @param item */
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

        PipeEventItem.Entered event = new PipeEventItem.Entered(container.pipe, item);
        container.pipe.eventBus.handleEvent(event);
        if (event.cancelled) {
            return;
        }

        if (!container.getWorld().isRemote) {
            item.output = resolveDestination(item);
        }

        items.unscheduleRemoval(item);

        if (!container.getWorld().isRemote) {
            sendTravelerPacket(item, true);
        }
    }

    // TODO: Rewrite to allow putting directions into priority categories on the pipe's side. ;w;
    public EnumFacing resolveDestination(TravelingItem item) {
        item.blacklist.add(item.input.getOpposite());

        EnumSet<EnumFacing> baseSet = EnumSet.noneOf(EnumFacing.class);
        EnumSet<EnumFacing> cloggedSides = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing o : EnumSet.complementOf(item.blacklist)) {
            if (container.pipe.outputOpen(o)) {
                ReceiveType type = canReceivePipeObjects(o, item);
                if (type != ReceiveType.NONE) {
                    baseSet.add(o);
                    if (type == ReceiveType.CLOGGED) {
                        cloggedSides.add(o);
                    }
                }
            }
        }

        List<EnumSet<EnumFacing>> destinations = new ArrayList<>(4);
        destinations.add(baseSet);

        PipeEventItem.FindDest event = new PipeEventItem.FindDest(container.pipe, item, destinations);
        container.pipe.eventBus.handleEvent(event);

        // Remove empty EnumSets
        Iterator<EnumSet<EnumFacing>> sets = destinations.iterator();
        while (sets.hasNext()) {
            if (sets.next().isEmpty()) {
                sets.remove();
            }
        }

        // First, find the first set of faces which contains an unclogged one.
        // If it lets you go straight, do so.
        List<EnumFacing> uncloggedFaces = new ArrayList<>();
        for (EnumSet<EnumFacing> faces : destinations) {
            for (EnumFacing face : faces) {
                if (!cloggedSides.contains(face)) {
                    uncloggedFaces.add(face);
                }
            }

            int s = uncloggedFaces.size();
            if (s > 0) {
                if (uncloggedFaces.contains(item.input)) {
                    return item.input;
                }
                return uncloggedFaces.get(BuildCraftCore.random.nextInt(s));
            }
        }

        // If all sides are clogged, check if bouncing can be done.
        if (allowBouncing) {
            EnumFacing o = item.input.getOpposite();
            ReceiveType type = canReceivePipeObjects(o, item);
            if (type == ReceiveType.ALLOWED) {
                return o;
            }
        }

        // If nothing else works, just return null.
        return null;
    }

    private ReceiveType canReceivePipeObjects(EnumFacing o, TravelingItem item) {
        TileEntity entity = container.getTile(o);

        if (!container.isPipeConnected(o)) {
            return ReceiveType.NONE;
        }

        if (entity instanceof IPipeTile) {
            Pipe<?> pipe = (Pipe<?>) ((IPipeTile) entity).getPipe();

            if (pipe == null || pipe.transport == null || !pipe.inputOpen(o.getOpposite()) || !(pipe.transport instanceof PipeTransportItems)) {
                return ReceiveType.NONE;
            }

            // TODO: OPTIMIZE ME
            for (TravelingItem item1 : ((PipeTransportItems) pipe.transport).items) {
                if (item1.input != null && item1.input.getAxis() == o.getAxis() && !item1.isMoving()) {
                    return ReceiveType.CLOGGED;
                }
            }

            return ReceiveType.ALLOWED;
        } else if (item.getInsertionHandler().canInsertItem(item, entity)) {
            ITransactor transactor = Transactor.getTransactorFor(entity, o.getOpposite());
            if (transactor != null) {
                return transactor.add(item.getItemStack(), false).stackSize > 0 ? ReceiveType.ALLOWED : ReceiveType.CLOGGED;
            }
        }

        return ReceiveType.NONE;
    }

    @Override
    public void updateEntity() {
        moveSolids();
    }

    private void refreshDestination(TravelingItem item, boolean force) {
        if (!getWorld().isRemote) {
            if (force || ((getWorld().getTotalWorldTime() + REFRESH_OFFSET) % DESTINATION_REFRESH_SPEED) == 0) {
                if (item.output == null || canReceivePipeObjects(item.output, item) == ReceiveType.NONE) {
                    EnumFacing output = resolveDestination(item);
                    if (output != item.output) {
                        item.output = output;
                        sendTravelerPacket(item, false);
                    }
                }
            }
        }
    }

    private void moveSolids() {
        items.flush();

        items.iterating = true;
        for (TravelingItem item : items) {
            if (item.getContainer() != this.container) {
                items.scheduleRemoval(item);
                continue;
            }

            EnumFacing face = item.toCenter ? item.input : item.output;

            if (!item.isMoving()) {
                refreshDestination(item, false);
            } else {
                item.movePosition(Utils.convert(face, item.getSpeed()));
            }

            if ((item.toCenter && middleReached(item)) || outOfBounds(item)) {
                if (item.isCorrupted()) {
                    items.remove(item);
                    continue;
                }

                item.toCenter = false;

                // Readjusting to the middle
                item.pos = Utils.convert(container.getPos()).add(new Vec3(0.5, TransportUtils.getPipeFloorOf(item.getItemStack()), 0.5));
                refreshDestination(item, true);

                PipeEventItem.ReachedCenter event = new PipeEventItem.ReachedCenter(container.pipe, item);
                container.pipe.eventBus.handleEvent(event);
            } else if (!item.toCenter && endReached(item)) {
                if (item.isCorrupted()) {
                    items.remove(item);
                    continue;
                }

                if (item.output == null) {
                    // TODO: Figure out why this is actually happening.
                    items.scheduleRemoval(item);
                    BCLog.logger.warn("Glitched item [Output direction UNKNOWN] removed from world @ " + container.getPos().getX() + ", " + container
                            .getPos().getY() + ", " + container.getPos().getZ() + "!");
                    continue;
                }

                TileEntity tile = container.getTile(item.output, true);

                PipeEventItem.ReachedEnd event = new PipeEventItem.ReachedEnd(container.pipe, item, tile);
                container.pipe.eventBus.handleEvent(event);
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
        if (tile instanceof IPipeTile) {
            Pipe<?> pipe = (Pipe<?>) ((IPipeTile) tile).getPipe();
            if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeTransportItems) {
                return ((PipeTransportItems) pipe.transport).injectItem(item, item.output, true, true);
            }
        }
        return false;
    }

    private void handleTileReached(TravelingItem item, TileEntity tile) {
        if (passToNextPipe(item, tile)) {
            // NOOP
        } else {
            boolean handled = false;

            if (!container.getWorld().isRemote) {
                if (item.getInsertionHandler().canInsertItem(item, tile)) {
                    ITransactor transactor = Transactor.getTransactorFor(tile, item.output.getOpposite());
                    if (transactor != null) {
                        handled = true;
                        ItemStack added = transactor.add(item.getItemStack(), true);
                        item.getItemStack().stackSize -= added.stackSize;
                    }
                }

                if (!handled) {
                    dropItem(item);
                } else if (item.getItemStack().stackSize > 0) {
                    reverseItem(item);
                }
            }
        }
    }

    private void dropItem(TravelingItem item) {
        if (container.getWorld().isRemote) {
            return;
        }

        PipeEventItem.DropItem event = new PipeEventItem.DropItem(container.pipe, item, item.toEntityItem());
        container.pipe.eventBus.handleEvent(event);

        if (event.entity == null) {
            return;
        }

        final EntityItem entity = event.entity;
        EnumFacing direction = item.input;
        entity.setPosition(entity.posX + direction.getFrontOffsetX() * 0.5d, entity.posY + direction.getFrontOffsetY() * 0.5d, entity.posZ + direction
                .getFrontOffsetZ() * 0.5d);

        entity.motionX = direction.getFrontOffsetX() * item.speed * 5 + getWorld().rand.nextGaussian() * 0.1d;
        entity.motionY = direction.getFrontOffsetY() * item.speed * 5 + getWorld().rand.nextGaussian() * 0.1d;
        entity.motionZ = direction.getFrontOffsetZ() * item.speed * 5 + getWorld().rand.nextGaussian() * 0.1d;

        container.getWorld().spawnEntityInWorld(entity);
    }

    protected boolean middleReached(TravelingItem item) {
        float middleLimit = item.getSpeed() * 1.01F;
        return Math.abs(container.getPos().getX() + 0.5 - item.pos.xCoord) < middleLimit
                && Math.abs(container.getPos().getY() + TransportUtils.getPipeFloorOf(item.getItemStack()) - item.pos.yCoord) < middleLimit
                && Math.abs(container.getPos().getZ() + 0.5 - item.pos.zCoord) < middleLimit;
        }

    protected boolean endReached(TravelingItem item) {
        return item.pos.distanceTo(Utils.convertMiddle(container.getPos())) > 0.65;
        // return item.pos.xCoord > container.getPos().getX() + 1 || item.pos.xCoord < container.x() || item.pos.yCoord
        // > container.y() + 1
        // || item.pos.yCoord < container.y() || item.pos.zCoord > container.z() + 1 || item.pos.zCoord < container.z();
    }

    protected boolean outOfBounds(TravelingItem item) {
        return item.pos.distanceTo(Utils.convertMiddle(container.getPos())) > 1;
        // return item.pos.xCoord > container.x() + 2 || item.pos.xCoord < container.x() - 1 || item.pos.yCoord >
        // container.y() + 2
        // || item.pos.yCoord < container.y() - 1 || item.pos.zCoord > container.z() + 2 || item.pos.zCoord <
        // container.z() - 1;
    }

    public Vec3 getPosition() {
        return Utils.convert(container.getPos());
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

    protected void doWork() {}

    /** Handles a packet describing a stack of items inside a pipe.
     *
     * @param packet */
    public void handleTravelerPacket(PacketPipeTransportTraveler packet) {
        TravelingItem item = TravelingItem.clientCache.get(packet.getTravelingEntityId());

        if (item == null) {
            item = TravelingItem.make(packet.getTravelingEntityId());
        }

        if (item.getContainer() != container) {
            items.add(item);
        }

        if (packet.forceStackRefresh() || item.getItemStack() == null) {
            BuildCraftTransport.instance.sendToServer(new PacketPipeTransportItemStackRequest(container, packet));
        }

        item.pos = packet.getItemPos();

        item.setSpeed(packet.getSpeed());

        item.toCenter = true;
        item.input = packet.getInputOrientation();
        item.output = packet.getOutputOrientation();
        item.color = packet.getColor();
    }

    private void sendTravelerPacket(TravelingItem data, boolean forceStackRefresh) {
        PacketPipeTransportTraveler packet = new PacketPipeTransportTraveler(container, data, forceStackRefresh);
        BuildCraftTransport.instance.sendToPlayers(packet, container.getWorld(), container.getPos(), DefaultProps.PIPE_CONTENTS_RENDER_DIST);
    }

    public int getNumberOfStacks() {
        int num = 0;
        for (TravelingItem item : items) {
            if (!item.ignoreWeight()) {
                num++;
            }
        }
        return num;
    }

    public int getNumberOfItems() {
        int num = 0;
        for (TravelingItem item : items) {
            if (!item.ignoreWeight() && item.getItemStack() != null) {
                num += item.getItemStack().stackSize;
            }
        }
        return num;
    }

    protected void neighborChange() {}

    @Override
    public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
        if (tile == null) {
            return false;
        }

        if (tile instanceof IPipeTile) {
            Pipe<?> pipe2 = (Pipe<?>) ((IPipeTile) tile).getPipe();
            if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportItems)) {
                return false;
            }
        }

        if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
            return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()).getSlots() > 0;
        }

        // TODO: Remove IInv/ISidedInv in 1.9

        if (tile instanceof ISidedInventory) {
            int[] slots = ((ISidedInventory) tile).getSlotsForFace(side.getOpposite());
            return slots != null && slots.length > 0;
        }

        return tile instanceof IPipeTile || (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0);
    }

    /** Group all items that are similar, that is to say same dmg, same id, same nbt and no contribution controlling
     * them */
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

    @Override
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

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("PipeTransportItems");
        left.add("- Items: " + getNumberOfStacks() + "/" + MAX_PIPE_STACKS);
        for (TravelingItem item : items) {
            left.add("");
            left.add("  - " + item.itemStack);
            left.add("    - pos = " + item.pos);
            left.add("    - middle = " + middleReached(item));
            left.add("    - end = " + endReached(item));
            left.add("    - out of boounds = " + outOfBounds(item));
        }
    }
}
