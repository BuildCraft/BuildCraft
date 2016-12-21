package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.PipeEventItem.ItemEntry;
import buildcraft.api.transport.neptune.IFlowItems;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.IPipe.ConnectedType;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.PipeFlow;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.transport.pipe.flow.TravellingItem.EnumTravelState;

public class PipeFlowItems extends PipeFlow implements IFlowItems {
    private static final double EXTRACT_SPEED = 0.08;
    public static final int NET_CREATE_ITEM = 2;

    private final DelayedList<TravellingItem> items = new DelayedList<>();

    public PipeFlowItems(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowItems(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
        long tickNow = pipe.getHolder().getPipeWorld().getTotalWorldTime();
        for (int i = 0; i < list.tagCount(); i++) {
            TravellingItem item = new TravellingItem(list.getCompoundTagAt(i), tickNow);
            if (!item.stack.isEmpty()) {
                items.add(item.getCurrentDelay(tickNow), item);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        List<List<TravellingItem>> allItems = items.getAllElements();
        NBTTagList list = new NBTTagList();

        long tickNow = pipe.getHolder().getPipeWorld().getTotalWorldTime();
        for (List<TravellingItem> l : allItems) {
            for (TravellingItem item : l) {
                list.appendTag(item.writeToNbt(tickNow));
            }
        }
        nbt.setTag("items", list);
        return nbt;
    }

    // Network

    @Override
    public void readPayload(int id, PacketBuffer bufIn, Side side) throws IOException {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(bufIn);
        if (side == Side.CLIENT) {
            if (id == NET_CREATE_ITEM) {
                EnumFacing from = buffer.readEnumValue(EnumFacing.class);
                EnumFacing to = MessageUtil.readEnumOrNull(buffer, EnumFacing.class);
                EnumDyeColor colour = MessageUtil.readEnumOrNull(buffer, EnumDyeColor.class);
                int delay = buffer.readInt();

                int stackId = buffer.readInt();
                int stackSize = buffer.readShort();

                Supplier<ItemStack> link = BuildCraftObjectCaches.retrieveItemStack(stackId);
                TravellingItem item = new TravellingItem(link, stackSize);
                item.from = from;
                item.to = to;
                item.colour = colour;
                long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();
                item.tickStarted = now;
                item.tickFinished = now + delay;
                item.state = EnumTravelState.CLIENT_RUNNING;
                items.add(delay, item);
            }
        }
    }

    // IFlowItems

    @Override
    public int tryExtractItems(int count, EnumFacing from, IStackFilter filter) {
        if (from == null) {
            return 0;
        }

        TileEntity tile = pipe.getConnectedTile(from);
        IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, from.getOpposite());

        ItemStack possible = trans.extract(filter, 1, count, true);

        if (possible.isEmpty()) {
            return 0;
        }

        IPipeHolder holder = pipe.getHolder();
        PipeEventItem.TryInsert tryInsert = new PipeEventItem.TryInsert(holder, this, null, from, possible);
        holder.fireEvent(tryInsert);
        if (tryInsert.isCanceled() || tryInsert.accepted <= 0) {
            return 0;
        }

        ItemStack stack = trans.extract(filter, tryInsert.accepted, tryInsert.accepted, false);

        if (stack.isEmpty()) {
            throw new IllegalStateException("The transactor " + trans + " returned an empty itemstack from a known good request!");
        }

        insertItemEvents(stack, null, EXTRACT_SPEED, from);

        return tryInsert.accepted;
    }

    // PipeFlow

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowItems;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return ItemTransactorHelper.getTransactor(oTile, face.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    @Override
    public void onTick() {
        World world = pipe.getHolder().getPipeWorld();

        List<TravellingItem> toTick = items.advance();
        if (world.isRemote) {
            // Note that we still needed to advance the items even if we are the client
            for (TravellingItem item : toTick) {
                if (item.state == EnumTravelState.CLIENT_RUNNING) {
                    // If we have instructions then we should act on them
                    if (item.motion != null) {
                        runItemInstructions(item);
                    } else {
                        // if not then wait for them
                        item.state = EnumTravelState.CLIENT_WAITING;
                        items.add(2, item);
                    }
                } else if (item.state == EnumTravelState.CLIENT_WAITING) {
                    if (item.motion != null) {
                        runItemInstructions(item);
                    } else {
                        /* forget about it, if the item did anything then we will find out soon and have to spawn a new
                         * item. */
                    }
                } else {
                    // All other client possibilities forget the item
                }
            }
            return;
        }

        for (TravellingItem item : toTick) {
            if (item.state == EnumTravelState.SERVER_TO_CENTER) {
                onItemReachCenter(item);
            } else {
                onItemReachEnd(item);
            }
        }
    }

    private void onItemReachCenter(TravellingItem item) {
        // fire centre event and then check to see if we need to redo the destinations
        ItemStack oldStack = item.stack;
        PipeEventItem.ReachCenter reachCenter = new PipeEventItem.ReachCenter(pipe.getHolder(), this, oldStack.copy(), item.from, item.colour);
        if (pipe.getHolder().fireEvent(reachCenter)) {
            /* If an event handled this then we *may* need to fire destination handling events */

            ItemStack newStack = reachCenter.stack;
            if (newStack.isEmpty()) {
                // we must have voided or used up the item => do nothing
                return;
            } else if (item.colour == reachCenter.colour && ItemStack.areItemStacksEqual(oldStack, newStack)) {
                // Everything is the same, just add it back into the list
                item.state = EnumTravelState.SERVER_TO_EXIT;
                item.colour = reachCenter.colour;
                items.add(item.timeToExit, item);
            } else {
                // Someone changed the itemstack or colour, recheck the destinations
                PipeEventItem.SideCheck sideCheck = new PipeEventItem.SideCheck(pipe.getHolder(), this, reachCenter.colour, item.from, newStack);
                sideCheck.disallow(item.from);
                if (item.tried != null) {
                    sideCheck.disallowAll(item.tried);
                }
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (!pipe.isConnected(face)) {
                        sideCheck.disallow(face);
                    }
                }
                pipe.getHolder().fireEvent(sideCheck);

                // Don't allow splitting now - you *have* to split at the entrance

                ItemEntry entry = new ItemEntry(reachCenter.colour, newStack, item.from);
                PipeEventItem.FindDest findDest = new PipeEventItem.FindDest(pipe.getHolder(), this, sideCheck.getOrder(), ImmutableList.of(entry));
                pipe.getHolder().fireEvent(findDest);
                if (entry.to == null) {
                    entry.to = findDest.generateRandomOrder();
                }

                List<EnumFacing> to = entry.to;
                item.to = (to.size() <= 0) ? null : to.get(0);
                if (to.size() > 1) {
                    item.toTryOrder = to.subList(1, to.size());
                } else {
                    item.toTryOrder = null;
                }
                item.colour = entry.colour;
                item.stack = entry.stack;
                item.state = EnumTravelState.SERVER_TO_EXIT;
                items.add(item.timeToExit, item);
                // TODO: inform the client about the new data
            }
        } else {
            // No-one handled this, its very simple
            item.state = EnumTravelState.SERVER_TO_EXIT;
            items.add(item.timeToExit, item);
        }
    }

    private void onItemReachEnd(TravellingItem item) {
        EnumFacing to = item.to;
        if (to == null) {
            // TODO: fire drop event
            dropItem(item);
        } else {
            ConnectedType type = pipe.getConnectedType(to);

            ItemStack leftOver = item.stack;

            if (type == ConnectedType.PIPE) {
                IPipe oPipe = pipe.getConnectedPipe(to);
                PipeFlow flow = oPipe.getFlow();

                // TODO: Replace with interface for inserting
                if (flow instanceof IInjectable) {
                    IInjectable oItemFlow = (IInjectable) flow;
                    leftOver = oItemFlow.injectItem(item.stack, true, to.getOpposite(), item.colour, item.speed);
                }
            } else if (type == ConnectedType.TILE) {
                TileEntity tile = pipe.getConnectedTile(to);
                IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, to.getOpposite());
                leftOver = trans.insert(item.stack, false, false);
            }

            if (!leftOver.isEmpty()) {
                if (item.toTryOrder == null || item.toTryOrder.isEmpty()) {
                    // Really drop it
                    dropItem(item, leftOver);
                } else {
                    if (item.tried == null) {
                        item.tried = new ArrayList<>(6);
                    }
                    item.tried.add(to);
                    insertItemImpl(leftOver, item.colour, item.speed, item.to, item.toTryOrder, item.tried);
                }
            }
            // TODO: Inform client
        }
    }

    /** Called on the client to use the motion field of the item to either move it to the next pipe or move it around
     * etc */
    private void runItemInstructions(TravellingItem item) {
        // TODO!
    }

    private void dropItem(TravellingItem item) {
        dropItem(item, item.stack);
    }

    private void dropItem(TravellingItem item, ItemStack stack) {
        if (stack == null) {
            return;
        }

        EnumFacing to = item.from.getOpposite();
        IPipeHolder holder = pipe.getHolder();
        World world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        EntityItem ent = new EntityItem(world, x, y, z, stack);

        ent.motionX = to.getFrontOffsetX() * 0.04;
        ent.motionY = to.getFrontOffsetY() * 0.04;
        ent.motionZ = to.getFrontOffsetZ() * 0.04;

        world.spawnEntity(ent);
    }

    @Override
    public boolean canInjectItems(EnumFacing from) {
        return pipe.isConnected(from);
    }

    /* Insertion has the following events:
     * 
     * TryInsert: See if (and how much) of a given stack can be accepted
     * 
     * SideCheck: Remove invalid sides from a set of all connected sides Also can apply ordering to make items prefer
     * some sides over some others
     * 
     * Split: Split up the items into different stacks to be sent to the destinations (only the highest priority list of
     * SideCheck will be included in the output)
     * 
     * FindDest: Finds a destination for each of the split items
     * 
     * ModifySpeed: Changes the speed of the item
     * 
     * (This text was copied from buildcraft.api.transport.PipeEventItem) */
    @Override
    public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor colour, double speed) {
        if (!canInjectItems(from)) {
            return stack;
        }

        if (speed < 0.01) {
            speed = 0.01;
        }

        // Try insert

        PipeEventItem.TryInsert tryInsert = new PipeEventItem.TryInsert(pipe.getHolder(), this, colour, from, stack);
        pipe.getHolder().fireEvent(tryInsert);
        if (tryInsert.isCanceled() || tryInsert.accepted <= 0) {
            return stack;
        }
        ItemStack toSplit = stack.copy();
        ItemStack toInsert = toSplit.splitStack(tryInsert.accepted);

        if (doAdd) {
            insertItemEvents(toInsert, colour, speed, from);
        }

        if (toSplit.isEmpty()) {
            toSplit = StackUtil.EMPTY;
        }

        return toSplit;
    }

    /** Used internally to split up manual insertions from controlled extractions. */
    private void insertItemEvents(@Nonnull ItemStack toInsert, EnumDyeColor colour, double speed, EnumFacing from) {
        IPipeHolder holder = pipe.getHolder();

        // Side Check

        PipeEventItem.SideCheck sideCheck = new PipeEventItem.SideCheck(holder, this, colour, from, toInsert);
        sideCheck.disallow(from);
        for (EnumFacing face : EnumFacing.VALUES) {
            if (face == from) {
                continue;
            }
            if (!pipe.isConnected(face)) {
                sideCheck.disallow(face);
            }
        }
        holder.fireEvent(sideCheck);

        List<EnumSet<EnumFacing>> order = sideCheck.getOrder();

        if (order.isEmpty()) {
            // TryBounce

            PipeEventItem.TryBounce bounce = new PipeEventItem.TryBounce(holder, this, colour, from, toInsert);
            holder.fireEvent(bounce);
            if (bounce.canBounce) {
                order = ImmutableList.of(EnumSet.of(from));
            } else {
                /* We failed and will be dropping the item right in the centre of the pipe.
                 * 
                 * No need for any other events */
                insertItemImpl(toInsert, colour, speed, from, null, null);
                return;
            }
        }

        // Split:

        PipeEventItem.ItemEntry toSplit = new PipeEventItem.ItemEntry(colour, toInsert, from);
        PipeEventItem.Split split = new PipeEventItem.Split(holder, this, order, toSplit);
        holder.fireEvent(split);
        ImmutableList<PipeEventItem.ItemEntry> splitList = ImmutableList.copyOf(split.items);

        // FindDest:

        PipeEventItem.FindDest findDest = new PipeEventItem.FindDest(holder, this, order, splitList);
        holder.fireEvent(findDest);

        // ModifySpeed:

        for (PipeEventItem.ItemEntry item : findDest.items) {
            PipeEventItem.ModifySpeed modifySpeed = new PipeEventItem.ModifySpeed(holder, this, item, speed);
            modifySpeed.modifyTo(0.04, 0.01);
            holder.fireEvent(modifySpeed);

            double target = modifySpeed.targetSpeed;
            double maxDelta = modifySpeed.maxSpeedChange;
            double nSpeed = speed;
            if (nSpeed < target) {
                nSpeed += maxDelta;
                if (nSpeed > target) {
                    nSpeed = target;
                }
            } else if (nSpeed > target) {
                nSpeed -= maxDelta;
                if (nSpeed < target) {
                    nSpeed = target;
                }
            }

            if (item.to == null) {
                item.to = findDest.generateRandomOrder();
            }
            insertItemImpl(item.stack, item.colour, nSpeed, from, item.to, null);
        }
    }

    private void insertItemImpl(@Nonnull ItemStack stack, EnumDyeColor colour, double speed, EnumFacing from, List<EnumFacing> to, List<EnumFacing> tried) {
        TravellingItem item = new TravellingItem(stack);

        World world = pipe.getHolder().getPipeWorld();
        long now = world.getTotalWorldTime();

        item.from = from;
        item.speed = speed;
        item.colour = colour;
        item.to = (to == null || to.size() <= 0) ? null : to.get(0);
        if (to != null && to.size() > 1) {
            item.toTryOrder = to.subList(1, to.size());
        }
        item.tried = tried;

        double dist = getPipeLength(item.from) + getPipeLength(item.to);
        item.genTimings(now, dist);

        int delay = (int) (item.tickFinished - now);
        item.state = EnumTravelState.SERVER_TO_CENTER;
        items.add(item.timeToCenter, item);

        final int stackId = BuildCraftObjectCaches.storeItemStack(stack);
        sendCustomPayload(NET_CREATE_ITEM, (buffer) -> {
            PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
            buf.writeEnumValue(from);
            MessageUtil.writeEnumOrNull(buf, item.to);
            MessageUtil.writeEnumOrNull(buf, colour);
            buf.writeInt(delay);
            buf.writeInt(stackId);
            buf.writeShort(stack.getCount());
        });
    }

    @Nullable
    private static EnumSet<EnumFacing> getFirstNonEmptySet(List<EnumSet<EnumFacing>> possible) {
        for (EnumSet<EnumFacing> set : possible) {
            if (set.size() > 0) {
                return set;
            }
        }
        return null;
    }

    private double getPipeLength(EnumFacing to) {
        if (to == null) {
            return 0;
        }
        if (pipe.isConnected(to)) {
            // TODO: Check the length between this pipes centre and the next block along
            return 0.5;
        } else {
            return 0.25;
        }
    }

    @SideOnly(Side.CLIENT)
    public List<TravellingItem> getAllItemsForRender() {
        List<TravellingItem> all = new ArrayList<>();
        for (List<TravellingItem> innerList : items.getAllElements()) {
            all.addAll(innerList);
        }
        return all;
    }
}
