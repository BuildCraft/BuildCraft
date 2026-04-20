/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.net.MessageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;

public class WorldSavedDataVolumeBoxes extends SavedData {
    private static final String DATA_NAME = "buildcraft_volume_boxes";
    /**
     * Used to assign {@link LevelSavedDataVolumeBoxes#world} to pass it to {@link VolumeBox},
     * as we can't pass it other way ({@link MapStorage} can call only constructor with one {@link String} argument
     * and then it calls NBT deserialization method,
     * giving us no chance to set the {@link LevelSavedDataVolumeBoxes#world} field).
     */
    private static Level currentLevel;
    public final Level world = currentLevel;
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();
    
    public final String mapName;

    public WorldSavedDataVolumeBoxes() {
        mapName = DATA_NAME;
    }
    
    public WorldSavedDataVolumeBoxes(String name, CompoundTag nbt) {
		this.mapName = name == null ? DATA_NAME : name;
		volumeBoxes.clear();
		NBTUtilBC.readCompoundList(nbt.get("volumeBoxes")).map(volumeBoxTag -> new VolumeBox(world, volumeBoxTag))
				.forEach(volumeBoxes::add);
    }

    public WorldSavedDataVolumeBoxes(String name) {
        mapName = (name);
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
        volumeBoxes.stream().filter(VolumeBox::isEditing).forEach(volumeBox -> {
        	Player player = volumeBox.getPlayer(world);
            if (player == null) {
                volumeBox.pauseEditing();
                dirty.set(true);
            } else {
                AABB oldAabb = volumeBox.box.getBoundingBox();
                volumeBox.box.reset();
                volumeBox.box.extendToEncompass(volumeBox.getHeld());
                BlockPos lookingAt = new BlockPos(
                    player.getEyePosition().add(player.getLookAngle().scale(volumeBox.getDist()))
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
    public void setDirty() {
        super.setDirty();
        MessageManager.sendToDimension(new MessageVolumeBoxes(volumeBoxes), world.dimension());
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("volumeBoxes", NBTUtilBC.writeObjectList(volumeBoxes.stream().map(VolumeBox::writeToNBT)));
        return nbt;
    }


    public static WorldSavedDataVolumeBoxes get(Level world) {
        if (world.isClientSide) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        ServerLevel level = (ServerLevel)world;
        DimensionDataStorage storage = level.getDataStorage();
        currentLevel = world;
        WorldSavedDataVolumeBoxes instance = (WorldSavedDataVolumeBoxes)
            storage.get(nbt -> new WorldSavedDataVolumeBoxes(DATA_NAME, nbt), DATA_NAME);
        if (instance == null) {
            instance = new WorldSavedDataVolumeBoxes();
            storage.set(DATA_NAME, instance);;
        }
        currentLevel = null;
        return instance;
    }
}
