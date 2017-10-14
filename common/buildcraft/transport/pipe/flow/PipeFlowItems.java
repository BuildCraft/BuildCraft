/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;

public final class PipeFlowItems extends PipeFlow implements IFlowItems {
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
    public void readPayload(int id, PacketBuffer bufIn, Side msgSide) throws IOException {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(bufIn);
        if (msgSide == Side.CLIENT) {
            if (id == NET_CREATE_ITEM) {
                int stackId = buffer.readInt();
                Supplier<ItemStack> link = BuildCraftObjectCaches.retrieveItemStack(stackId);
                int count = buffer.readUnsignedShort();
                TravellingItem item = new TravellingItem(link, count);
                item.toCenter = buffer.readBoolean();
                item.side = buffer.readEnumValue(EnumFacing.class);
                item.colour = MessageUtil.readEnumOrNull(buffer, EnumDyeColor.class);
                item.timeToDest = buffer.readUnsignedShort();
                item.tickStarted = pipe.getHolder().getPipeWorld().getTotalWorldTime() + 1;
                item.tickFinished = item.tickStarted + item.timeToDest;
                items.add(item.timeToDest + 1, item);
            }
        }
    }

    void sendItemDataToClient(TravellingItem item) {
        final int stackId = BuildCraftObjectCaches.storeItemStack(item.stack);
        sendCustomPayload(NET_CREATE_ITEM, (buffer) -> {
            PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
            buf.writeInt(stackId);
            buf.writeShort(item.stack.getCount());
            buf.writeBoolean(item.toCenter);
            buf.writeEnumValue(item.side);
            MessageUtil.writeEnumOrNull(buf, item.colour);
            buf.writeShort(item.timeToDest > Short.MAX_VALUE ? Short.MAX_VALUE : item.timeToDest);
        });
    }

    @Override
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
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
    public int tryExtractItems(int count, EnumFacing from, EnumDyeColor colour, IStackFilter filter, boolean simulate) {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            throw new IllegalStateException("Cannot extract items on the client side!");
        }
        if (from == null) {
            return 0;
        }

        TileEntity tile = pipe.getConnectedTile(from);
        IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, from.getOpposite());

        ItemStack possible = trans.extract(filter, 1, count, true);

        if (possible.isEmpty()) {
            return 0;
        }
        if (possible.getCount() > possible.getMaxStackSize()) {
            possible.setCount(possible.getMaxStackSize());
            count = possible.getMaxStackSize();
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
                "The transactor " + trans + " returned an empty itemstack from a known good request!");
        }

        if (!simulate) {
            insertItemEvents(stack, colour, EXTRACT_SPEED, from);
        }

        return count;
    }

    @Override
    public void sendPhantomItem(ItemStack stack, EnumFacing from, EnumFacing to, EnumDyeColor colour) {
        if (from == null && to == null) {
            return;
        }
        EnumFacing face0, face1, face2;
        boolean twoItems = from != null && to != null;
        face0 = from;
        face1 = from == null ? to : null;
        face2 = to;

        long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();

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
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == PipeApi.CAP_INJECTABLE) {
            return PipeApi.CAP_INJECTABLE.cast(this);
        } else if (capability == CapUtil.CAP_ITEM_TRANSACTOR) {
            return CapUtil.CAP_ITEM_TRANSACTOR.cast(ItemTransactorHelper.wrapInjectable(this, facing));
        } else {
            return super.getCapability(capability, facing);
        }
    }

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
            // TODO: Client item advancing/intelligent stuffs
            return;
        }

        for (TravellingItem item : toTick) {
            if (item.isPhantom) {
                continue;
            }
            if (item.toCenter) {
                onItemReachCenter(item);
            } else {
                onItemReachEnd(item);
            }
        }
    }

    private void onItemReachCenter(TravellingItem item) {
        IPipeHolder holder = pipe.getHolder();
        PipeEventItem.ReachCenter reachCenter =
            new PipeEventItem.ReachCenter(holder, this, item.colour, item.stack, item.side);
        holder.fireEvent(reachCenter);
        if (reachCenter.getStack().isEmpty()) {
            return;
        }

        PipeEventItem.SideCheck sideCheck =
            new PipeEventItem.SideCheck(holder, this, reachCenter.colour, reachCenter.from, reachCenter.getStack());
        sideCheck.disallow(reachCenter.from);
        for (EnumFacing face : EnumFacing.VALUES) {
            if (item.tried.contains(face) || !pipe.isConnected(face)) {
                sideCheck.disallow(face);
            }
        }
        holder.fireEvent(sideCheck);

        List<EnumSet<EnumFacing>> order = sideCheck.getOrder();
        if (order.isEmpty()) {
            PipeEventItem.TryBounce tryBounce =
                new PipeEventItem.TryBounce(holder, this, reachCenter.colour, reachCenter.from, reachCenter.getStack());
            holder.fireEvent(tryBounce);
            if (tryBounce.canBounce) {
                order = ImmutableList.of(EnumSet.of(reachCenter.from));
            } else {
                dropItem(item.stack, null, item.side.getOpposite(), item.speed);
                return;
            }
        }

        PipeEventItem.ItemEntry entry =
            new PipeEventItem.ItemEntry(reachCenter.colour, reachCenter.getStack(), reachCenter.from);
        PipeEventItem.Split split = new PipeEventItem.Split(holder, this, order, entry);
        holder.fireEvent(split);
        ImmutableList<PipeEventItem.ItemEntry> entries = ImmutableList.copyOf(split.items);

        PipeEventItem.FindDest findDest = new PipeEventItem.FindDest(holder, this, order, entries);
        holder.fireEvent(findDest);

        World world = holder.getPipeWorld();
        long now = world.getTotalWorldTime();
        for (PipeEventItem.ItemEntry itemEntry : findDest.items) {
            if (itemEntry.stack.isEmpty()) {
                continue;
            }
            PipeEventItem.ModifySpeed modifySpeed = new PipeEventItem.ModifySpeed(holder, this, itemEntry, item.speed);
            modifySpeed.modifyTo(0.04, 0.01);
            holder.fireEvent(modifySpeed);

            double target = modifySpeed.targetSpeed;
            double maxDelta = modifySpeed.maxSpeedChange;
            double nSpeed = item.speed;
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

            List<EnumFacing> destinations = itemEntry.to;
            if (destinations == null || destinations.size() == 0) {
                destinations = findDest.generateRandomOrder();
            }
            if (destinations.size() == 0) {
                dropItem(itemEntry.stack, null, item.side.getOpposite(), nSpeed);
            } else {
                TravellingItem newItem = new TravellingItem(itemEntry.stack);
                newItem.tried.addAll(item.tried);
                newItem.toCenter = false;
                newItem.colour = itemEntry.colour;
                newItem.side = destinations.get(0);
                newItem.speed = nSpeed;
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
            switch (type) {
                case PIPE: {
                    IPipe oPipe = pipe.getConnectedPipe(item.side);
                    if (oPipe == null) {
                        break;
                    }
                    PipeFlow flow = oPipe.getFlow();
                    if (flow instanceof IFlowItems) {
                        IFlowItems oFlow = (IFlowItems) flow;
                        excess = oFlow.injectItem(excess, true, item.side.getOpposite(), item.colour, item.speed);
                        if (excess.isEmpty()) {
                            return;
                        }
                    }
                    break;
                }
                case TILE: {
                    TileEntity tile = pipe.getConnectedTile(item.side);
                    IInjectable injectable = ItemTransactorHelper.getInjectable(tile, item.side.getOpposite());
                    excess = injectable.injectItem(excess, true, item.side.getOpposite(), item.colour, item.speed);
                    if (excess.isEmpty()) {
                        return;
                    }

                    IItemTransactor transactor = ItemTransactorHelper.getTransactor(tile, item.side.getOpposite());
                    excess = transactor.insert(excess, false, false);

                    if (excess.isEmpty()) {
                        return;
                    }

                    break;
                }
            }
        }
        if (excess.isEmpty()) {
            return;
        }
        item.tried.add(item.side);
        item.toCenter = true;
        item.stack = excess;
        item.genTimings(holder.getPipeWorld().getTotalWorldTime(), getPipeLength(item.side));
        items.add(item.timeToDest, item);
        sendItemDataToClient(item);
    }

    private void dropItem(ItemStack stack, EnumFacing side, EnumFacing motion, double speed) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        IPipeHolder holder = pipe.getHolder();
        World world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();

        double x = pos.getX() + 0.5 + motion.getFrontOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + motion.getFrontOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + motion.getFrontOffsetZ() * 0.5;
        speed += 0.01;
        speed *= 2;
        EntityItem ent = new EntityItem(world, x, y, z, stack);
        ent.motionX = motion.getFrontOffsetX() * speed;
        ent.motionY = motion.getFrontOffsetY() * speed;
        ent.motionZ = motion.getFrontOffsetZ() * speed;

        PipeEventItem.Drop drop = new PipeEventItem.Drop(holder, this, ent);
        holder.fireEvent(drop);
        if (ent.getItem().isEmpty() || ent.isDead) {
            return;
        }

        world.spawnEntity(ent);
    }

    @Override
    public boolean canInjectItems(EnumFacing from) {
        return pipe.isConnected(from);
    }

    @Nonnull
    @Override
    public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor colour,
        double speed) {
        if (pipe.getHolder().getPipeWorld().isRemote) {
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
        ItemStack toInsert = toSplit.splitStack(tryInsert.accepted);

        if (doAdd) {
            insertItemEvents(toInsert, colour, speed, from);
        }

        if (toSplit.isEmpty()) {
            toSplit = StackUtil.EMPTY;
        }

        return toSplit;
    }

    @Override
    public void insertItemsForce(@Nonnull ItemStack stack, EnumFacing from, EnumDyeColor colour, double speed) {
        World world = pipe.getHolder().getPipeWorld();
        if (world.isRemote) {
            throw new IllegalStateException("Cannot inject items on the client side!");
        }
        if (stack.isEmpty()) {
            return;
        }
        if (speed < 0.01) {
            speed = 0.01;
        }
        long now = world.getTotalWorldTime();
        TravellingItem item = new TravellingItem(stack);
        item.side = from;
        item.toCenter = true;
        item.speed = speed;
        item.colour = colour;
        item.genTimings(now, 0);
        item.tried.add(from);
        addItemTryMerge(item);
    }

    /** Used internally to split up manual insertions from controlled extractions. */
    private void insertItemEvents(@Nonnull ItemStack toInsert, EnumDyeColor colour, double speed, EnumFacing from) {
        IPipeHolder holder = pipe.getHolder();

        PipeEventItem.OnInsert onInsert = new PipeEventItem.OnInsert(holder, this, colour, toInsert, from);
        holder.fireEvent(onInsert);

        if (onInsert.getStack().isEmpty()) {
            return;
        }

        World world = pipe.getHolder().getPipeWorld();
        long now = world.getTotalWorldTime();

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

    @Nullable
    private static EnumSet<EnumFacing> getFirstNonEmptySet(List<EnumSet<EnumFacing>> possible) {
        for (EnumSet<EnumFacing> set : possible) {
            if (set.size() > 0) {
                return set;
            }
        }
        return null;
    }

    private double getPipeLength(EnumFacing side) {
        if (side == null) {
            return 0;
        }
        if (pipe.isConnected(side)) {
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
