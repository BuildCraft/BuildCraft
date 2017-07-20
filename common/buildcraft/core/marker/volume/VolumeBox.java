/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VolumeBox {
    public UUID id;
    public Box box;
    private UUID player = null;
    private UUID oldPlayer = null;
    private BlockPos held = null;
    private double dist = 0;
    private BlockPos oldMin = null, oldMax = null;
    public final Map<EnumAddonSlot, Addon> addons = new EnumMap<>(EnumAddonSlot.class);
    public final List<Lock> locks = new ArrayList<>();

    public VolumeBox(BlockPos at) {
        id = UUID.randomUUID();
        box = new Box(at, at);
    }

    public VolumeBox(NBTTagCompound nbt) {
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
        NBTTagList addonsTag = nbt.getTagList("addons", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, addonsTag.tagCount()).mapToObj(addonsTag::getCompoundTagAt).forEach(addonsEntryTag -> {
            Class<? extends Addon> addonClass = AddonsRegistry.INSTANCE.getClassByName(new ResourceLocation(addonsEntryTag.getString("addonClass")));
            try {
                Addon addon = addonClass.newInstance();
                addon.box = this;
                addon.readFromNBT(addonsEntryTag.getCompoundTag("addonData"));
                EnumAddonSlot slot = NBTUtilBC.readEnum(addonsEntryTag.getTag("slot"), EnumAddonSlot.class);
                addons.put(slot, addon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        NBTTagList locksTag = nbt.getTagList("locks", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, locksTag.tagCount()).mapToObj(locksTag::getCompoundTagAt).map(lockTag -> {
            Lock lock = new Lock();
            lock.readFromNBT(lockTag);
            return lock;
        }).forEach(locks::add);
    }

    public VolumeBox(PacketBuffer buf) {
        fromBytes(buf);
    }

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
        addons.values().forEach(Addon::onBoxSizeChange);
    }

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
        return this.player != null && Objects.equals(this.player, player.getGameProfile().getId());
    }

    public boolean isPausedEditingBy(EntityPlayer player) {
        return this.oldPlayer != null && Objects.equals(this.oldPlayer, player.getGameProfile().getId());
    }

    public EntityPlayer getPlayer(World world) {
        return world.getPlayerEntityByUUID(player);
    }

    public void setHeldDistOldMinOldMax(BlockPos held, double dist, BlockPos oldMin, BlockPos oldMax) {
        this.held = held;
        this.dist = dist;
        this.oldMin = oldMin;
        this.oldMax = oldMax;
    }

    public BlockPos getHeld() {
        return held;
    }

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
        NBTTagList addonsTag = new NBTTagList();
        addons.forEach((slot, addon) -> {
            NBTTagCompound addonsEntryTag = new NBTTagCompound();
            addonsEntryTag.setTag("slot", NBTUtilBC.writeEnum(slot));
            addonsEntryTag.setString("addonClass", AddonsRegistry.INSTANCE.getNameByClass(addon.getClass()).toString());
            addonsEntryTag.setTag("addonData", addon.writeToNBT(new NBTTagCompound()));
            addonsTag.appendTag(addonsEntryTag);
        });
        nbt.setTag("addons", addonsTag);
        NBTTagList locksTag = new NBTTagList();
        locks.stream().map(lock -> lock.writeToNBT(new NBTTagCompound())).forEach(locksTag::appendTag);
        nbt.setTag("locks", locksTag);
        return nbt;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(id);
        box.writeData(buf);
        buf.writeBoolean(player != null);
        if (player != null) {
            buf.writeUniqueId(player);
        }
        buf.writeInt(addons.size());
        addons.forEach((slot, addon) -> {
            new PacketBufferBC(buf).writeEnumValue(slot);
            buf.writeString(AddonsRegistry.INSTANCE.getNameByClass(addon.getClass()).toString());
            addon.toBytes(buf);
        });
        buf.writeInt(locks.size());
        locks.forEach(lock -> lock.toBytes(buf));
    }

    public void fromBytes(PacketBuffer buf) {
        id = buf.readUniqueId();
        box = new Box();
        box.readData(buf);
        player = buf.readBoolean() ? buf.readUniqueId() : null;
        Map<EnumAddonSlot, Addon> newAddons = new EnumMap<>(EnumAddonSlot.class);
        IntStream.range(0, buf.readInt())
                .forEach(i -> {
                    EnumAddonSlot slot = new PacketBufferBC(buf).readEnumValue(EnumAddonSlot.class);
                    Class<? extends Addon> addonClass = AddonsRegistry.INSTANCE.getClassByName(new ResourceLocation(buf.readString(1024)));
                    try {
                        Addon addon = addonClass.newInstance();
                        addon.box = this;
                        addon.fromBytes(buf);
                        newAddons.put(slot, addon);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
        addons.keySet().removeIf(slot -> !newAddons.containsKey(slot));
        newAddons.entrySet().stream()
                .filter(slotAddon -> !addons.containsKey(slotAddon.getKey()))
                .forEach(slotAddon -> addons.put(slotAddon.getKey(), slotAddon.getValue()));
        for (Map.Entry<EnumAddonSlot, Addon> slotAddon : newAddons.entrySet()) {
            PacketBuffer buffer = new PacketBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
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
