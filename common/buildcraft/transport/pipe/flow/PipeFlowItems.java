package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.PipeEventItem;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.transport.api_move.IFlowItems;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipe.ConnectedType;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipeFlow;

public class PipeFlowItems extends PipeFlow implements IFlowItems {
    private static final double EXTRACT_SPEED = 0.08;

    public static final int NET_CREATE_ITEM = 2;

    private final DelayedList<TravellingItem> items = new DelayedList<>();

    public PipeFlowItems(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowItems(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    // Network

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        if (side == Side.CLIENT) {
            if (id == NET_CREATE_ITEM) {
                EnumFacing from = EnumFacing.getFront(buffer.readUnsignedByte());
                short readTo = buffer.readUnsignedByte();
                EnumFacing to = readTo == 7 ? null : EnumFacing.getFront(readTo);
                short readColour = buffer.readUnsignedByte();
                EnumDyeColor colour = readColour == 16 ? null : EnumDyeColor.byMetadata(readColour);
                int delay = buffer.readInt();
                ItemStack stack = buffer.readItemStackFromBuffer();
                TravellingItem item = new TravellingItem(this, stack);
                item.from = from;
                item.to = to;
                item.colour = colour;
                long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();
                item.tickStarted = now;
                item.tickFinished = now + delay;
                items.add(delay, item);
            }
        }
    }

    // IFlow Items

    @Override
    public int tryExtractStack(int count, EnumFacing from, IStackFilter filter) {
        TileEntity tile = pipe.getConnectedTile(from);
        IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, from.getOpposite());
        ItemStack stack = trans.extract(filter, 0, count, false);
        if (stack != null && stack.stackSize > 0) {
            insertItem(stack, null, EXTRACT_SPEED, from);
            return stack.stackSize;
        }

        return 0;
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
            return;
        }

        for (TravellingItem item : toTick) {
            EnumFacing to = item.to;
            if (to == null) {
                // TODO: fire drop event
                dropItem(item);
            } else {
                ConnectedType type = pipe.getConnectedType(to);

                if (type == ConnectedType.PIPE) {
                    IPipe oPipe = pipe.getConnectedPipe(to);
                    PipeFlow flow = oPipe.getFlow();

                    // TODO: Replace with interface for inserting
                    if (flow instanceof PipeFlowItems) {
                        PipeFlowItems oItemFlow = (PipeFlowItems) flow;
                        oItemFlow.insertItem(item.stack, item.colour, item.speed, to.getOpposite());
                    } else {
                        dropItem(item);
                    }

                } else if (type == ConnectedType.TILE) {
                    TileEntity tile = pipe.getConnectedTile(to);
                    IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, to.getOpposite());
                    ItemStack leftOver = trans.insert(item.stack, false, false);
                    dropItem(item, leftOver);
                } else {
                    // Something went wrong???
                    dropItem(item);
                }

                // TODO: Inform client
            }
        }
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

        world.spawnEntityInWorld(ent);
    }

    /** @param stack
     * @param colour
     * @param speed
     * @param from
     * @return The leftover stack. May be null. */
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
    public ItemStack insertItem(ItemStack stack, EnumDyeColor colour, double speed, EnumFacing from) {
        IPipeHolder holder = pipe.getHolder();

        BCLog.logger.info("Attempting to insert " + stack + " from " + from);

        // Try insert

        PipeEventItem.TryInsert tryInsert = new PipeEventItem.TryInsert(holder, this, colour, from, stack);
        holder.fireEvent(tryInsert);
        if (tryInsert.isCanceled() || tryInsert.accepted <= 0) {
            BCLog.logger.info("Insertion failed! (" + tryInsert.isCanceled() + ", " + tryInsert.accepted + ")");
            return stack;
        }
        ItemStack toInsert = stack.splitStack(tryInsert.accepted);

        // Side Check

        PipeEventItem.SideCheck sideCheck = new PipeEventItem.SideCheck(holder, this, colour, from, toInsert);
        EnumSet<EnumFacing> possible = EnumSet.noneOf(EnumFacing.class);
        for (EnumFacing face : EnumFacing.VALUES) {
            if (face == from) {
                continue;
            }
            if (pipe.isConnected(face)) {
                possible.add(face);
            }
        }
        sideCheck.possible.add(possible);
        BCLog.logger.info("Possible sides (before): " + sideCheck.possible);
        holder.fireEvent(sideCheck);
        BCLog.logger.info("Possible sides (after): " + sideCheck.possible);
        EnumSet<EnumFacing> used = getFirstNonEmptySet(sideCheck.possible);

        if (used == null) {
            /* We failed and will be dropping the item right in the centre of the pipe.
             * 
             * No need for any other events */
            BCLog.logger.info("Failed to find a side!");
            insertItemImpl(toInsert, colour, speed, from, null);

            return stack;
        }
        List<EnumFacing> randOrder = new ArrayList<>(used);
        Collections.shuffle(randOrder);
        ImmutableList<EnumFacing> destinations = ImmutableList.copyOf(randOrder);

        // Split:

        PipeEventItem.ItemEntry toSplit = new PipeEventItem.ItemEntry(colour, toInsert, from);
        PipeEventItem.Split split = new PipeEventItem.Split(holder, this, destinations, toSplit);
        holder.fireEvent(split);
        ImmutableList<PipeEventItem.ItemEntry> splitList = ImmutableList.copyOf(split.items);

        // FindDest:

        PipeEventItem.FindDest findDest = new PipeEventItem.FindDest(holder, this, destinations, splitList);
        holder.fireEvent(findDest);

        // ModifySpeed:

        for (PipeEventItem.ItemEntry item : findDest.items) {
            PipeEventItem.ModifySpeed modifySpeed = new PipeEventItem.ModifySpeed(holder, this, item, speed);
            holder.fireEvent(modifySpeed);
            if (item.to == null) {
                item.to = destinations.get(0);
            }

            insertItemImpl(item.stack, item.colour, modifySpeed.speed, from, item.to);
        }

        return stack;
    }

    private void insertItemImpl(ItemStack stack, EnumDyeColor colour, double speed, EnumFacing from, EnumFacing to) {
        TravellingItem item = new TravellingItem(this, stack);

        World world = pipe.getHolder().getPipeWorld();
        long now = world.getTotalWorldTime();

        item.from = from;
        item.speed = speed;
        item.colour = colour;
        item.to = to;

        double dist = getPipeLength(item.from) + getPipeLength(item.to);
        item.genTimings(now, dist);

        int delay = (int) (item.tickFinished - now);
        items.add(delay, item);

        // TODO: Optimise!
        sendCustomPayload(NET_CREATE_ITEM, (buffer) -> {
            buffer.writeByte(from.ordinal());
            buffer.writeByte(item.to == null ? 7 : item.to.ordinal());
            buffer.writeByte(colour == null ? 16 : colour.getMetadata());
            buffer.writeInt(delay);
            buffer.writeItemStackToBuffer(stack);
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
