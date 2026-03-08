/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VolumeBox {
    public final World world;
    public UUID id;
    public Box box;
    private UUID player = null;
    private UUID oldPlayer = null;
    private BlockPos held = null;
    private double dist = 0;
    private BlockPos oldMin = null, oldMax = null;
    public final Map<EnumAddonSlot, Addon> addons = new EnumMap<>(EnumAddonSlot.class);
    public final List<Lock> locks = new ArrayList<>();

    public VolumeBox(World world, BlockPos at) {
        if (world == null) throw new NullPointerException("world");
        this.world = world;
        id = UUID.randomUUID();
        box = new Box(at, at);
    }

    public VolumeBox(World world, CompoundNBT nbt) {
        if (world == null) throw new NullPointerException("world");
        this.world = world;
        id = nbt.getUUID("id");
        box = new Box();
        box.initialize(nbt.getCompound("box"));
        player = nbt.contains("player") ? NBTUtil.loadUUID(nbt.getCompound("player")) : null;
        oldPlayer = nbt.contains("oldPlayer") ? NBTUtil.loadUUID(nbt.getCompound("oldPlayer")) : null;
        if (nbt.contains("held")) {
            held = NBTUtil.readBlockPos(nbt.getCompound("held"));
        }
        dist = nbt.getDouble("dist");
        if (nbt.contains("oldMin")) {
            oldMin = NBTUtil.readBlockPos(nbt.getCompound("oldMin"));
        }
        if (nbt.contains("oldMax")) {
            oldMax = NBTUtil.readBlockPos(nbt.getCompound("oldMax"));
        }
        NBTUtilBC.readCompoundList(nbt.get("addons")).forEach(addonsEntryTag ->
        {
            Class<? extends Addon> addonClass =
                    AddonsRegistry.INSTANCE.getClassByName(new ResourceLocation(addonsEntryTag.getString("addonClass")));
            try {
                Addon addon = addonClass.newInstance();
                addon.volumeBox = this;
                addon.readFromNBT(addonsEntryTag.getCompound("addonData"));
                EnumAddonSlot slot = NBTUtilBC.readEnum(addonsEntryTag.get("slot"), EnumAddonSlot.class);
                addons.put(slot, addon);
                addon.postReadFromNbt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        NBTUtilBC.readCompoundList(nbt.get("locks")).map(lockTag ->
        {
            Lock lock = new Lock();
            lock.readFromNBT(lockTag);
            return lock;
        }).forEach(locks::add);
    }

    public VolumeBox(World world, PacketBufferBC buf) throws IOException {
        if (world == null) throw new NullPointerException("world");
        this.world = world;
        fromBytes(buf);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isEditing() {
        return player != null;
    }

    private void resetEditing() {
        oldMin = oldMax = null;
        held = null;
        dist = 0;
    }

    public void cancelEditing() {
        player = null;
        box.reset();
        box.extendToEncompass(oldMin);
        box.extendToEncompass(oldMax);
        resetEditing();
    }

    public void confirmEditing() {
        player = null;
        resetEditing();
        addons.values().forEach(Addon::onVolumeBoxSizeChange);
    }

    @SuppressWarnings("WeakerAccess")
    public void pauseEditing() {
        oldPlayer = player;
        player = null;
    }

    public void resumeEditing() {
        player = oldPlayer;
        oldPlayer = null;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player.getGameProfile().getId();
    }

    public boolean isEditingBy(PlayerEntity player) {
        return player != null && Objects.equals(this.player, player.getGameProfile().getId());
    }

    public boolean isPausedEditingBy(PlayerEntity player) {
        return oldPlayer != null && Objects.equals(oldPlayer, player.getGameProfile().getId());
    }

    @SuppressWarnings("WeakerAccess")
    public PlayerEntity getPlayer(World world) {
//        return world.getPlayerEntityByUUID(player);
        return world.getPlayerByUUID(player);
    }

    public void setHeldDistOldMinOldMax(BlockPos held, double dist, BlockPos oldMin, BlockPos oldMax) {
        this.held = held;
        this.dist = dist;
        this.oldMin = oldMin;
        this.oldMax = oldMax;
    }

    @SuppressWarnings("WeakerAccess")
    public BlockPos getHeld() {
        return held;
    }

    @SuppressWarnings("WeakerAccess")
    public double getDist() {
        return dist;
    }

    public Stream<Lock.Target> getLockTargetsStream() {
        return locks.stream().flatMap(lock -> lock.targets.stream());
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("id", id);
        nbt.put("box", this.box.writeToNBT());
        if (player != null) {
            nbt.put("player", NBTUtil.createUUID(player));
        }
        if (oldPlayer != null) {
            nbt.put("oldPlayer", NBTUtil.createUUID(oldPlayer));
        }
        if (held != null) {
            nbt.put("held", NBTUtil.writeBlockPos(held));
        }
        nbt.putDouble("dist", dist);
        if (oldMin != null) {
            nbt.put("oldMin", NBTUtil.writeBlockPos(oldMin));
        }
        if (oldMax != null) {
            nbt.put("oldMax", NBTUtil.writeBlockPos(oldMax));
        }
        nbt.put(
                "addons",
                NBTUtilBC.writeCompoundList(
                        addons.entrySet().stream().map(entry ->
                        {
                            CompoundNBT addonsEntryTag = new CompoundNBT();
                            addonsEntryTag.put("slot", NBTUtilBC.writeEnum(entry.getKey()));
                            addonsEntryTag.putString(
                                    "addonClass",
                                    AddonsRegistry.INSTANCE.getNameByClass(entry.getValue().getClass()).toString()
                            );
                            addonsEntryTag.put("addonData", entry.getValue().writeToNBT(new CompoundNBT()));
                            return addonsEntryTag;
                        })
                ));
        nbt.put("locks", NBTUtilBC.writeCompoundList(locks.stream().map(Lock::writeToNBT)));
        return nbt;
    }

    public void toBytes(PacketBufferBC buf) {
        buf.writeUUID(id);
        box.writeData(buf);
        buf.writeBoolean(player != null);
        if (player != null) {
            buf.writeUUID(player);
        }
        buf.writeInt(addons.size());
        addons.forEach((slot, addon) ->
        {
            buf.writeEnum(slot);
            buf.writeUtf(AddonsRegistry.INSTANCE.getNameByClass(addon.getClass()).toString());
            addon.toBytes(buf);
        });
        buf.writeInt(locks.size());
        locks.forEach(lock -> lock.toBytes(buf));
    }

    public void fromBytes(PacketBufferBC buf) throws IOException {
        id = buf.readUUID();
        box = new Box();
        box.readData(buf);
        player = buf.readBoolean() ? buf.readUUID() : null;
        Map<EnumAddonSlot, Addon> newAddons = new EnumMap<>(EnumAddonSlot.class);
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            EnumAddonSlot slot = buf.readEnum(EnumAddonSlot.class);
            ResourceLocation rl = new ResourceLocation(buf.readUtf(1024));
            Class<? extends Addon> addonClass = AddonsRegistry.INSTANCE.getClassByName(rl);
            try {
                if (addonClass == null) {
                    throw new IOException("Unknown addon class " + rl);
                }
                Addon addon = addonClass.newInstance();
                addon.volumeBox = this;
                addon.onAdded();
                addon.fromBytes(buf);
                newAddons.put(slot, addon);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IOException("Failed to deserialize addon!", e);
            }
        }
        addons.keySet().removeIf(slot -> !newAddons.containsKey(slot));
        newAddons.entrySet().stream().filter(slotAddon -> !addons.containsKey(slotAddon.getKey()))
                .forEach(slotAddon -> addons.put(slotAddon.getKey(), slotAddon.getValue()));
        for (Map.Entry<EnumAddonSlot, Addon> slotAddon : newAddons.entrySet()) {
            PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
            slotAddon.getValue().toBytes(buffer);
            addons.get(slotAddon.getKey()).fromBytes(buffer);
        }
        locks.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i ->
        {
            Lock lock = new Lock();
            lock.fromBytes(buf);
            return lock;
        }).forEach(locks::add);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && id.equals(((VolumeBox) o).id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
