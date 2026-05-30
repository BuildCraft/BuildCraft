/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;

import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.net.MessageMultiPipeItem.TravellingItemData;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStone;

public final class PipeFlowItems extends PipeFlow implements IFlowItems {
    private static final double EXTRACT_SPEED = 0.08;
    public static final int NET_CREATE_ITEM = 2;

    private final DelayedList<TravellingItem> items = new DelayedList<>();
    private final List<ItemStack> postDropCache = new ArrayList<>();

    public PipeFlowItems(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowItems(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
        NbtList list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
        long tickNow = pipe.getHolder().getPipeWorld().getTime();
        for (int i = 0; i < list.size(); i++) {
            TravellingItem item = new TravellingItem(list.getCompound(i), tickNow);
            if (!item.stack.isEmpty()) {
                items.add(item.getCurrentDelay(tickNow), item);
            }
        }
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();
        List<List<TravellingItem>> allItems = items.getAllElements();
        NbtList list = new NbtList();

        long tickNow = pipe.getHolder().getPipeWorld().getTime();
        for (List<TravellingItem> l : allItems) {
            for (TravellingItem item : l) {
                list.add(item.writeToNbt(tickNow));
            }
        }
        nbt.put("items", list);
        return nbt;
    }

    // Network

    @Override
    public void readPayload(int id, PacketByteBuf bufIn, EnvType msgSide) throws IOException {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(bufIn);
        if (msgSide == EnvType.CLIENT) {
            if (id == NET_CREATE_ITEM) {
                int stackId = buffer.readInt();
                Supplier<ItemStack> link = BuildCraftObjectCaches.retrieveItemStack(stackId);
                int count = buffer.readUnsignedShort();
                TravellingItem item = new TravellingItem(link, count);
                item.toCenter = buffer.readBoolean();
                item.side = buffer.readEnumValue(Direction.class);
                item.colour = MessageUtil.readEnumOrNull(buffer, DyeColor.class);
                item.timeToDest = buffer.readUnsignedShort();
                item.tickStarted = pipe.getHolder().getPipeWorld().getTime() + 1;
                item.tickFinished = item.tickStarted + item.timeToDest;
                items.add(item.timeToDest + 1, item);
            }
        }
    }

    public void handleClientReceviedItems(List<TravellingItemData> list) {
        for (TravellingItemData data : list) {
            handleClientReceviedItem(data);
        }
    }

    public void handleClientReceviedItem(TravellingItemData data) {
        int stackId = data.stackId;
        Supplier<ItemStack> link = BuildCraftObjectCaches.retrieveItemStack(stackId);
        int count = data.stackCount;
        TravellingItem item = new TravellingItem(link, count);
        item.toCenter = data.toCenter;
        item.side = data.side;
        item.colour = data.colour;
        item.timeToDest = data.timeToDest;
        item.tickStarted = pipe.getHolder().getPipeWorld().getTime() + 1;
        item.tickFinished = item.tickStarted + item.timeToDest;
        items.add(item.timeToDest + 1, item);
    }

    void sendItemDataToClient(TravellingItem item) {
        final int stackId = BuildCraftObjectCaches.storeItemStack(item.stack);
        PipeItemMessageQueue.appendTravellingItem(
            pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos(), stackId, (byte) item.stack.getCount(),
            item.toCenter, item.side, item.colour, item.timeToDest > Byte.MAX_VALUE ? Byte.MAX_VALUE
                : (byte) item.timeToDest
        );
    }

    @Override
    public void addDrops(List<ItemStack> toDrop, int fortune) {
        super.addDrops(toDrop, fortune);
        for (List<TravellingItem> list : items.getAllElements()) {
            for (TravellingItem item : list) {
                if (!item.isPhantom) {
                    toDrop.add(item.stack);
                }
            }
        }
    }

    // IFlowItems

    @Override
    public int tryExtractItems(int count, Direction from, DyeColor colour, IStackFilter filter, boolean simulate) {
        if (pipe.getHolder().getPipeWorld().isClient) {
            throw new IllegalStateException("Cannot extract items on the client side!");
        }
        if (from == null) {
            return 0;
        }

        BlockEntity tile = pipe.getConnectedTile(from);
        IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, from.getOpposite());

        ItemStack possible = trans.extract(filter, 1, count, true);

        if (possible.isEmpty()) {
            return 0;
        }
        if (possible.getCount() > possible.getMaxCount()) {
            possible.setCount(possible.getMaxCount());
            count = possible.getMaxCount();
        }

        IPipeHolder holder = pipe.getHolder();
        PipeEventItem.TryInsert tryInsert = new PipeEventItem.TryInsert(holder, this, colour, from, possible);
        holder.fireEvent(tryInsert);
        if (tryInsert.isCanceled() || tryInsert.accepted <= 0) {
            return 0;
        }

        count = Math.min(count, tryInsert.accepted);

        ItemStack stack = trans.extract(filter, count, count, simulate);

        if (stack.isEmpty()) {
            throw new IllegalStateException(
                "The transactor " + trans + " returned an empty itemstack from a known good request!"
            );
        }

        if (!simulate) {
            insertItemEvents(stack, colour, EXTRACT_SPEED, from);
        }

        return count;
    }

    @Override
    public void sendPhantomItem(ItemStack stack, Direction from, Direction to, DyeColor colour) {
        if (from == null && to == null) {
            return;
        }
        Direction face0, face1, face2;
        boolean twoItems = from != null && to != null;
        face0 = from;
        face1 = from == null ? to : null;
        face2 = to;

        long now = pipe.getHolder().getPipeWorld().getTime();

        TravellingItem firstItem = new TravellingItem(stack);
        firstItem.isPhantom = true;
        firstItem.toCenter = face1 == null;
        firstItem.colour = colour;
        firstItem.side = face0 == null ? face1 : face0;
        firstItem.speed = EXTRACT_SPEED;
        firstItem.genTimings(now, getPipeLength(firstItem.side));
        items.add(firstItem.timeToDest, firstItem);
        sendItemDataToClient(firstItem);

        if (twoItems) {
            TravellingItem secondItem = new TravellingItem(stack);
            secondItem.isPhantom = true;
            secondItem.toCenter = false;
            secondItem.colour = colour;
            secondItem.side = face2;
            secondItem.speed = EXTRACT_SPEED;
            secondItem.genTimings(firstItem.tickFinished, getPipeLength(secondItem.side));
            items.add(secondItem.timeToDest, secondItem);
            sendItemDataToClient(secondItem);
        }
    }

    // PipeFlow

    @Override
    public <T> T getCapability(@Nonnull Object capability, Direction facing) {
        // STUB(R.Chen): full implementation in Phase 4F.
        // The original routed PipeApi.CAP_INJECTABLE → this and CapUtil.CAP_ITEM_TRANSACTOR →
        // ItemTransactorHelper.wrapInjectable(this, facing). Both depend on the Forge Capability
        // system (CapUtil + Capability#cast), which has no Fabric equivalent and is replaced by
        // BlockApiLookup wiring in Phase 4F — the same deferral as PipeFlow#getCapability and Pipe.
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof IFlowItems;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        return ItemTransactorHelper.getTransactor(oTile, face.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    @Override
    public void onTick() {
        World world = pipe.getHolder().getPipeWorld();

        List<TravellingItem> toTick = items.advance();
        long currentTime = world.getTime();

        for (TravellingItem item : toTick) {
            if (item.tickFinished > currentTime) {
                // Can happen if something ticks this tile multiple times in a single real tick
                items.add((int) (item.tickFinished - currentTime), item);
                continue;
            }
            if (item.isPhantom) {
                postDropCache.add(item.stack);
                continue;
            }
            if (world.isClient) {
                // TODO: Client item advancing/intelligent stuffs
                continue;
            }
            if (item.toCenter) {
                onItemReachCenter(item);
            } else {
                onItemReachEnd(item);
            }
        }
    }

    @Override
    public void postPluggableTick() {
        postDropCache.clear();
    }

    private void onItemReachCenter(TravellingItem item) {
        IPipeHolder holder = pipe.getHolder();
        PipeEventItem.ReachCenter reachCenter = new PipeEventItem.ReachCenter(
            holder, this, item.colour, item.stack, item.side
        );
        holder.fireEvent(reachCenter);
        if (reachCenter.getStack().isEmpty()) {
            return;
        }

        PipeEventItem.SideCheck sideCheck = new PipeEventItem.SideCheck(
            holder, this, reachCenter.colour, reachCenter.from, reachCenter.getStack()
        );
        sideCheck.disallow(reachCenter.from);
        for (Direction face : Direction.values()) {
            if (item.tried.contains(face) || !pipe.isConnected(face)) {
                sideCheck.disallow(face);
            }
        }
        holder.fireEvent(sideCheck);

        List<EnumSet<Direction>> order = sideCheck.getOrder();
        if (order.isEmpty()) {
            PipeEventItem.TryBounce tryBounce = new PipeEventItem.TryBounce(
                holder, this, reachCenter.colour, reachCenter.from, reachCenter.getStack()
            );
            holder.fireEvent(tryBounce);
            if (tryBounce.canBounce) {
                order = ImmutableList.of(EnumSet.of(reachCenter.from));
            } else {
                dropItem(item.stack, null, item.side.getOpposite(), item.speed);
                return;
            }
        }

        PipeEventItem.ItemEntry entry = new PipeEventItem.ItemEntry(
            reachCenter.colour, reachCenter.getStack(), reachCenter.from
        );
        PipeEventItem.Split split = new PipeEventItem.Split(holder, this, order, entry);
        holder.fireEvent(split);
        ImmutableList<PipeEventItem.ItemEntry> entries = ImmutableList.copyOf(split.items);

        PipeEventItem.FindDest findDest = new PipeEventItem.FindDest(holder, this, order, entries);
        holder.fireEvent(findDest);

        World world = holder.getPipeWorld();
        long now = world.getTime();
        for (PipeEventItem.ItemEntry itemEntry : findDest.items) {
            if (itemEntry.stack.isEmpty()) {
                continue;
            }
            PipeEventItem.ModifySpeed modifySpeed = new PipeEventItem.ModifySpeed(holder, this, itemEntry, item.speed);

            final double newSpeed;

            if (holder.fireEvent(modifySpeed)) {
                double target = modifySpeed.targetSpeed;
                double maxDelta = modifySpeed.maxSpeedChange;
                if (item.speed < target) {
                    newSpeed = Math.min(target, item.speed + maxDelta);
                } else if (item.speed > target) {
                    newSpeed = Math.max(target, item.speed - maxDelta);
                } else {
                    newSpeed = item.speed;
                }
            } else {
                // Nothing affected the speed
                // so just fallback to a sensible default
                if (item.speed > 0.03) {
                    newSpeed = Math.max(0.03, item.speed - PipeBehaviourStone.SPEED_DELTA);
                } else {
                    newSpeed = item.speed;
                }
            }

            List<Direction> destinations = itemEntry.to;
            if (destinations == null || destinations.size() == 0) {
                destinations = findDest.generateRandomOrder();
            }
            if (destinations.size() == 0) {
                dropItem(itemEntry.stack, null, item.side.getOpposite(), newSpeed);
            } else {
                TravellingItem newItem = new TravellingItem(itemEntry.stack);
                newItem.tried.addAll(item.tried);
                newItem.toCenter = false;
                newItem.colour = itemEntry.colour;
                newItem.side = destinations.get(0);
                newItem.speed = newSpeed;
                newItem.genTimings(now, getPipeLength(newItem.side));
                items.add(newItem.timeToDest, newItem);
                sendItemDataToClient(newItem);
            }
        }
    }

    private void onItemReachEnd(TravellingItem item) {
        IPipeHolder holder = pipe.getHolder();
        PipeEventItem.ReachEnd reachEnd = new PipeEventItem.ReachEnd(holder, this, item.colour, item.stack, item.side);
        holder.fireEvent(reachEnd);
        item.colour = reachEnd.colour;
        item.stack = reachEnd.getStack();
        ItemStack excess = item.stack;
        if (excess.isEmpty()) {
            return;
        }
        if (pipe.isConnected(item.side)) {
            ConnectedType type = pipe.getConnectedType(item.side);
            Direction oppositeSide = item.side.getOpposite();
            switch (type) {
                case PIPE: {
                    IPipe oPipe = pipe.getConnectedPipe(item.side);
                    if (oPipe == null) {
                        break;
                    }
                    PipeFlow flow = oPipe.getFlow();
                    if (flow instanceof IFlowItems) {
                        IFlowItems oFlow = (IFlowItems) flow;
                        ItemStack before = excess;
                        excess = oFlow.injectItem(excess.copy(), true, oppositeSide, item.colour, item.speed);

                        if (!excess.isEmpty()) {
                            before.decrement(excess.getCount());
                        }

                        excess = fireEventEjectIntoPipe(oFlow, item.side, before, excess);
                    }
                    break;
                }
                case TILE: {
                    BlockEntity tile = pipe.getConnectedTile(item.side);
                    IInjectable injectable = ItemTransactorHelper.getInjectable(tile, oppositeSide);
                    ItemStack before = excess;
                    excess = injectable.injectItem(excess.copy(), true, oppositeSide, item.colour, item.speed);

                    if (!excess.isEmpty()) {
                        IItemTransactor transactor = ItemTransactorHelper.getTransactor(tile, oppositeSide);
                        excess = transactor.insert(excess, false, false);
                    }
                    excess = fireEventEjectIntoTile(tile, item.side, before, excess);
                    break;
                }
            }
        }
        if (excess.isEmpty()) {
            postDropCache.add(item.stack);
            return;
        }
        item.tried.add(item.side);
        item.toCenter = true;
        item.stack = excess;
        item.genTimings(holder.getPipeWorld().getTime(), getPipeLength(item.side));
        items.add(item.timeToDest, item);
        sendItemDataToClient(item);
    }

    private ItemStack fireEventEjectIntoPipe(IFlowItems oFlow, Direction to, ItemStack before, ItemStack excess) {
        IPipeHolder holder = this.pipe.getHolder();
        return fireEventEjected(holder, new PipeEventItem.Ejected.IntoPipe(holder, this, before, excess, to, oFlow));
    }

    private ItemStack fireEventEjectIntoTile(BlockEntity tile, Direction to, ItemStack before, ItemStack excess) {
        IPipeHolder holder = this.pipe.getHolder();
        return fireEventEjected(holder, new PipeEventItem.Ejected.IntoTile(holder, this, before, excess, to, tile));
    }

    private static ItemStack fireEventEjected(IPipeHolder holder, PipeEventItem.Ejected event) {
        holder.fireEvent(event);
        return event.getExcess();
    }

    private void dropItem(ItemStack stack, Direction side, Direction motion, double speed) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        IPipeHolder holder = pipe.getHolder();
        World world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();

        double x = pos.getX() + 0.5 + motion.getOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + motion.getOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + motion.getOffsetZ() * 0.5;
        speed += 0.01;
        speed *= 2;
        ItemEntity ent = new ItemEntity(world, x, y, z, stack);
        ent.setVelocity(motion.getOffsetX() * speed, motion.getOffsetY() * speed, motion.getOffsetZ() * speed);

        PipeEventItem.Drop drop = new PipeEventItem.Drop(holder, this, ent);
        holder.fireEvent(drop);
        if (ent.getStack().isEmpty() || ent.isRemoved()) {
            return;
        }

        world.spawnEntity(ent);
    }

    @Override
    public boolean canInjectItems(Direction from) {
        return pipe.isConnected(from);
    }

    @Nonnull
    @Override
    public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, Direction from, DyeColor colour,
        double speed) {
        if (pipe.getHolder().getPipeWorld().isClient) {
            throw new IllegalStateException("Cannot inject items on the client side!");
        }
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
        ItemStack toInsert = toSplit.split(tryInsert.accepted);

        if (doAdd) {
            insertItemEvents(toInsert, colour, speed, from);
        }

        if (toSplit.isEmpty()) {
            toSplit = StackUtil.EMPTY;
        }

        return toSplit;
    }

    @Override
    public void insertItemsForce(@Nonnull ItemStack stack, Direction from, DyeColor colour, double speed) {
        World world = pipe.getHolder().getPipeWorld();
        if (world.isClient) {
            throw new IllegalStateException("Cannot inject items on the client side!");
        }
        if (stack.isEmpty()) {
            return;
        }
        if (speed < 0.01) {
            speed = 0.01;
        }
        long now = world.getTime();
        TravellingItem item = new TravellingItem(stack);
        if (from == null) {
            // Find a reasonable alternative (as it's not allowed to be null)
            for (Direction f : Direction.values()) {
                if (!pipe.isConnected(f)) {
                    item.side = f;
                    break;
                }
            }
            if (item.side == null) {
                item.side = Direction.UP;
            }
        } else {
            item.side = from;
        }
        item.toCenter = true;
        item.speed = speed;
        item.colour = colour;
        item.genTimings(now, 0);
        if (from != null) {
            item.tried.add(from);
        }
        // Explicitly don't send this item to the client:
        // There's little point in trying to render it
        // seeing as it needs to travel 0 distance.
        items.add(item.timeToDest, item);
    }

    /** Used internally to split up manual insertions from controlled extractions. */
    private void insertItemEvents(@Nonnull ItemStack toInsert, DyeColor colour, double speed, Direction from) {
        IPipeHolder holder = pipe.getHolder();

        PipeEventItem.OnInsert onInsert = new PipeEventItem.OnInsert(holder, this, colour, toInsert, from);
        holder.fireEvent(onInsert);

        if (onInsert.getStack().isEmpty()) {
            return;
        }

        World world = pipe.getHolder().getPipeWorld();
        long now = world.getTime();

        TravellingItem item = new TravellingItem(toInsert);
        item.side = from;
        item.toCenter = true;
        item.speed = speed;
        item.colour = onInsert.colour;
        item.stack = onInsert.getStack();
        item.genTimings(now, getPipeLength(from));
        item.tried.add(from);
        addItemTryMerge(item);
    }

    private void addItemTryMerge(TravellingItem item) {
        for (List<TravellingItem> list : items.getAllElements()) {
            for (TravellingItem item2 : list) {
                if (item2.mergeWith(item)) {
                    return;
                }
            }
        }
        items.add(item.timeToDest, item);
        sendItemDataToClient(item);
    }

    @PipeEventHandler
    public static void addTriggers(PipeEventStatement.AddTriggerInternal event) {
        event.triggers.add(BCTransportStatements.TRIGGER_ITEMS_TRAVERSING);
    }

    public boolean doesContainItems() {
        // Note that this counts all items
        // (including phantom items, which is fine)
        // This only works because this list is only expanded to add elements
        // and elements are only removed in advance()
        return items.getMaxDelay() > 0 || !postDropCache.isEmpty();
    }

    public boolean containsItemMatching(ItemStack filter) {
        if (filter.isEmpty()) {
            return doesContainItems();
        }
        for (List<TravellingItem> list : items.getAllElements()) {
            for (TravellingItem item : list) {
                if (StackUtil.matchesStackOrList(filter, item.stack)) {
                    return true;
                }
            }
        }
        for (ItemStack stack : postDropCache) {
            if (StackUtil.matchesStackOrList(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static EnumSet<Direction> getFirstNonEmptySet(List<EnumSet<Direction>> possible) {
        for (EnumSet<Direction> set : possible) {
            if (set.size() > 0) {
                return set;
            }
        }
        return null;
    }

    double getPipeLength(Direction side) {
        if (side == null) {
            return 0;
        }
        if (pipe.isConnected(side)) {
            if (pipe.getConnectedType(side) == ConnectedType.TILE) {
                // TODO: Check the length between this pipes centre and the next block along
                return 0.5 + 0.25;// Tiny distance for fully pushing items in.
            }
            return 0.5;
        } else {
            return 0.25;
        }
    }

    @Environment(EnvType.CLIENT)
    public List<TravellingItem> getAllItemsForRender() {
        List<TravellingItem> all = new ArrayList<>();
        for (List<TravellingItem> innerList : items.getAllElements()) {
            all.addAll(innerList);
        }
        return all;
    }
}
