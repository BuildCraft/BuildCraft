/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;


import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.MessageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class WorldSavedDataVolumeBoxes extends SavedData {
    private static final String DATA_NAME = "buildcraft_volume_boxes";
    /**
     * Used to assign {@link WorldSavedDataVolumeBoxes#world} to pass it to {@link VolumeBox},
     * as we can't pass it other way ({@link DimensionDataStorage} can call only constructor with one {@link String} argument
     * and then it calls NBT deserialization method,
     * giving us no chance to set the {@link WorldSavedDataVolumeBoxes#world} field).
     */
    private static Level currentWorld;
    public final Level world = currentWorld;
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();

    public WorldSavedDataVolumeBoxes() {
//        super(DATA_NAME);
        super();
    }

    @SuppressWarnings("unused")
    public WorldSavedDataVolumeBoxes(String name) {
        super();
//        super(name);
    }

    public VolumeBox getVolumeBoxAt(BlockPos pos) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.box.contains(pos)).findFirst().orElse(null);
    }

    public void addVolumeBox(BlockPos pos) {
        volumeBoxes.add(new VolumeBox(world, pos));
    }

    public VolumeBox getVolumeBoxFromId(UUID id) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.id.equals(id)).findFirst().orElse(null);
    }

    public VolumeBox getCurrentEditing(Player player) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        volumeBoxes.stream().filter(VolumeBox::isEditing).forEach(volumeBox ->
        {
            Player player = volumeBox.getPlayer(world);
            if (player == null) {
                volumeBox.pauseEditing();
                dirty.set(true);
            } else {
                AABB oldAabb = volumeBox.box.getBoundingBox();
                volumeBox.box.reset();
                volumeBox.box.extendToEncompass(volumeBox.getHeld());
                BlockPos lookingAt = BlockPos.containing(
                        player.position()
                                .add(0, player.getEyeHeight(), 0)
                                .add(player.getLookAngle().scale(volumeBox.getDist()))
                );
                volumeBox.box.extendToEncompass(lookingAt);
                if (!volumeBox.box.getBoundingBox().equals(oldAabb)) {
                    dirty.set(true);
                }
            }
        });
        for (VolumeBox volumeBox : volumeBoxes) {
            List<Lock> locksToRemove = new ArrayList<>(volumeBox.locks).stream()
                    .filter(lock -> !lock.cause.stillWorks(world))
                    .collect(Collectors.toList());
            if (!locksToRemove.isEmpty()) {
                volumeBox.locks.removeAll(locksToRemove);
                dirty.set(true);
            }
        }
        if (dirty.get()) {
            setDirty();
        }
    }

    @Override
//    public void markDirty()
    public void setDirty() {
//        super.markDirty();
        super.setDirty();
//        MessageManager.sendToDimension(new MessageVolumeBoxes(volumeBoxes), world.provider.getDimension());
        MessageManager.sendToDimension(new MessageVolumeBoxes(volumeBoxes), world.dimension());
    }

    @SuppressWarnings("NullableProblems")
    @Override
//    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("volumeBoxes", NBTUtilBC.writeCompoundList(volumeBoxes.stream().map(VolumeBox::writeToNBT)));
        return nbt;
    }

    @SuppressWarnings("NullableProblems")
    // Calen: not override, load by ourselves -> #get: ret.load(nbt)
//    @Override
    public void load(CompoundTag nbt) {
        volumeBoxes.clear();
        NBTUtilBC.readCompoundList(nbt.get("volumeBoxes"))
                .map(volumeBoxTag -> new VolumeBox(world, volumeBoxTag))
                .forEach(volumeBoxes::add);
    }

    public static WorldSavedDataVolumeBoxes get(Level world) {
        if (world.isClientSide || !(world instanceof ServerLevel)) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
        currentWorld = world;
        WorldSavedDataVolumeBoxes instance =
                storage.get((nbt) ->
                {
                    WorldSavedDataVolumeBoxes ret = new WorldSavedDataVolumeBoxes(DATA_NAME);
                    ret.load(nbt);
                    return ret;
                }, DATA_NAME);
        if (instance == null) {
            instance = new WorldSavedDataVolumeBoxes();
//            storage.setData(DATA_NAME, instance);
            storage.set(DATA_NAME, instance);
        }
        currentWorld = null;
        return instance;
    }
}
