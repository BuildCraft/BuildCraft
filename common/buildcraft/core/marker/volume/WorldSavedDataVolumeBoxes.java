/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.MessageManager;

public class WorldSavedDataVolumeBoxes extends WorldSavedData {
    private static final String DATA_NAME = "buildcraft_volume_boxes";
    private static World currentWorld;
    public World world = currentWorld;
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();

    public WorldSavedDataVolumeBoxes() {
        super(DATA_NAME);
    }

    @SuppressWarnings("unused")
    public WorldSavedDataVolumeBoxes(String name) {
        super(name);
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

    public VolumeBox getCurrentEditing(EntityPlayer player) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        volumeBoxes.stream().filter(VolumeBox::isEditing).forEach(volumeBox -> {
            EntityPlayer player = volumeBox.getPlayer(world);
            if (player == null) {
                volumeBox.pauseEditing();
                dirty.set(true);
            } else {
                AxisAlignedBB oldAabb = volumeBox.box.getBoundingBox();
                volumeBox.box.reset();
                volumeBox.box.extendToEncompass(volumeBox.getHeld());
                BlockPos lookingAt = new BlockPos(
                    player.getPositionVector()
                        .addVector(0, player.getEyeHeight(), 0)
                        .add(player.getLookVec().scale(volumeBox.getDist()))
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
            markDirty();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        MessageManager.sendToDimension(new MessageVolumeBoxes(volumeBoxes), world.provider.getDimension());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("volumeBoxes", NBTUtilBC.writeCompoundList(volumeBoxes.stream().map(VolumeBox::writeToNBT)));
        return nbt;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        volumeBoxes.clear();
        NBTUtilBC.readCompoundList(nbt.getTag("volumeBoxes"))
            .map(volumeBoxTag -> new VolumeBox(world, volumeBoxTag))
            .forEach(volumeBoxes::add);
    }

    public static WorldSavedDataVolumeBoxes get(World world) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        MapStorage storage = world.getPerWorldStorage();
        currentWorld = world;
        WorldSavedDataVolumeBoxes instance = (WorldSavedDataVolumeBoxes)
            storage.getOrLoadData(WorldSavedDataVolumeBoxes.class, DATA_NAME);
        currentWorld = null;
        if (instance == null) {
            instance = new WorldSavedDataVolumeBoxes();
            storage.setData(DATA_NAME, instance);
        }
        instance.world = world;
        return instance;
    }
}
