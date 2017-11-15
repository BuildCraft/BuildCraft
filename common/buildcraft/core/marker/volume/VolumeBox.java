/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;

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

    public VolumeBox(World world, NBTTagCompound nbt) {
        if (world == null) throw new NullPointerException("world");
        this.world = world;
        id = nbt.getUniqueId("id");
        box = new Box();
        box.initialize(nbt.getCompoundTag("box"));
        player = nbt.hasKey("player") ? NBTUtil.getUUIDFromTag(nbt.getCompoundTag("player")) : null;
        oldPlayer = nbt.hasKey("oldPlayer") ? NBTUtil.getUUIDFromTag(nbt.getCompoundTag("oldPlayer")) : null;
        if (nbt.hasKey("held")) {
            held = NBTUtil.getPosFromTag(nbt.getCompoundTag("held"));
        }
        dist = nbt.getDouble("dist");
        if (nbt.hasKey("oldMin")) {
            oldMin = NBTUtil.getPosFromTag(nbt.getCompoundTag("oldMin"));
        }
        if (nbt.hasKey("oldMax")) {
            oldMax = NBTUtil.getPosFromTag(nbt.getCompoundTag("oldMax"));
        }
        NBTUtilBC.readCompoundList(nbt.getTag("addons")).forEach(addonsEntryTag -> {
            Class<? extends Addon> addonClass =
                AddonsRegistry.INSTANCE.getClassByName(new ResourceLocation(addonsEntryTag.getString("addonClass")));
            try {
                Addon addon = addonClass.newInstance();
                addon.volumeBox = this;
                addon.readFromNBT(addonsEntryTag.getCompoundTag("addonData"));
                EnumAddonSlot slot = NBTUtilBC.readEnum(addonsEntryTag.getTag("slot"), EnumAddonSlot.class);
                addons.put(slot, addon);
                addon.postReadFromNbt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        NBTUtilBC.readCompoundList(nbt.getTag("locks")).map(lockTag -> {
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

    public void setPlayer(EntityPlayer player) {
        this.player = player.getGameProfile().getId();
    }

    public boolean isEditingBy(EntityPlayer player) {
        return player != null && Objects.equals(this.player, player.getGameProfile().getId());
    }

    public boolean isPausedEditingBy(EntityPlayer player) {
        return oldPlayer != null && Objects.equals(oldPlayer, player.getGameProfile().getId());
    }

    @SuppressWarnings("WeakerAccess")
    public EntityPlayer getPlayer(World world) {
        return world.getPlayerEntityByUUID(player);
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

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("id", id);
        nbt.setTag("box", this.box.writeToNBT());
        if (player != null) {
            nbt.setTag("player", NBTUtil.createUUIDTag(player));
        }
        if (oldPlayer != null) {
            nbt.setTag("oldPlayer", NBTUtil.createUUIDTag(oldPlayer));
        }
        if (held != null) {
            nbt.setTag("held", NBTUtil.createPosTag(held));
        }
        nbt.setDouble("dist", dist);
        if (oldMin != null) {
            nbt.setTag("oldMin", NBTUtil.createPosTag(oldMin));
        }
        if (oldMax != null) {
            nbt.setTag("oldMax", NBTUtil.createPosTag(oldMax));
        }
        nbt.setTag(
            "addons",
            NBTUtilBC.writeCompoundList(
                addons.entrySet().stream().map(entry -> {
                    NBTTagCompound addonsEntryTag = new NBTTagCompound();
                    addonsEntryTag.setTag("slot", NBTUtilBC.writeEnum(entry.getKey()));
                    addonsEntryTag.setString(
                        "addonClass",
                        AddonsRegistry.INSTANCE.getNameByClass(entry.getValue().getClass()).toString()
                    );
                    addonsEntryTag.setTag("addonData", entry.getValue().writeToNBT(new NBTTagCompound()));
                    return addonsEntryTag;
                })
            ));
        nbt.setTag("locks", NBTUtilBC.writeCompoundList(locks.stream().map(Lock::writeToNBT)));
        return nbt;
    }

    public void toBytes(PacketBufferBC buf) {
        buf.writeUniqueId(id);
        box.writeData(buf);
        buf.writeBoolean(player != null);
        if (player != null) {
            buf.writeUniqueId(player);
        }
        buf.writeInt(addons.size());
        addons.forEach((slot, addon) -> {
            buf.writeEnumValue(slot);
            buf.writeString(AddonsRegistry.INSTANCE.getNameByClass(addon.getClass()).toString());
            addon.toBytes(buf);
        });
        buf.writeInt(locks.size());
        locks.forEach(lock -> lock.toBytes(buf));
    }

    public void fromBytes(PacketBufferBC buf) throws IOException {
        id = buf.readUniqueId();
        box = new Box();
        box.readData(buf);
        player = buf.readBoolean() ? buf.readUniqueId() : null;
        Map<EnumAddonSlot, Addon> newAddons = new EnumMap<>(EnumAddonSlot.class);
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            EnumAddonSlot slot = buf.readEnumValue(EnumAddonSlot.class);
            ResourceLocation rl = new ResourceLocation(buf.readString(1024));
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
        IntStream.range(0, buf.readInt()).mapToObj(i -> {
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
