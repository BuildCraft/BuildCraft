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
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.BCLog;

import buildcraft.lib.net.MessageManager;

public class WorldSavedDataVolumeBoxes extends WorldSavedData {
    public static final String DATA_NAME = "buildcraft_volume_boxes";
    public World world;
    public final List<VolumeBox> boxes = new ArrayList<>();

    public WorldSavedDataVolumeBoxes() {
        super(DATA_NAME);
    }

    public WorldSavedDataVolumeBoxes(String name) {
        super(name);
    }

    public VolumeBox getBoxAt(BlockPos pos) {
        return boxes.stream().filter(box -> box.box.contains(pos)).findFirst().orElse(null);
    }

    public VolumeBox addBox(BlockPos pos) {
        VolumeBox box = new VolumeBox(pos);
        boxes.add(box);
        return box;
    }

    public VolumeBox getBoxFromId(UUID id) {
        return boxes.stream().filter(box -> box.id.equals(id)).findFirst().orElse(null);
    }

    public VolumeBox getCurrentEditing(EntityPlayer player) {
        return boxes.stream().filter(box -> box.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        boxes.stream().filter(VolumeBox::isEditing).forEach(box -> {
            EntityPlayer player = box.getPlayer(world);
            if (player == null) {
                box.pauseEditing();
                dirty.set(true);
            } else {
                AxisAlignedBB oldBox = box.box.getBoundingBox();
                box.box.reset();
                box.box.extendToEncompass(box.getHeld());
                BlockPos lookingAt = new BlockPos(player.getPositionVector().addVector(0, player.getEyeHeight(), 0).add(player.getLookVec().scale(box.getDist())));
                box.box.extendToEncompass(lookingAt);
                if (!box.box.getBoundingBox().equals(oldBox)) {
                    dirty.set(true);
                }
            }
        });
        for (VolumeBox box : boxes) {
            List<Lock> locksToRemove = new ArrayList<>(box.locks).stream()
                    .filter(lock -> !lock.cause.stillWorks(world))
                    .collect(Collectors.toList());
            if (!locksToRemove.isEmpty()) {
                box.locks.removeAll(locksToRemove);
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
        MessageManager.sendToDimension(new MessageVolumeBoxes(boxes), world.provider.getDimension());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList boxesTag = new NBTTagList();
        boxes.stream().map(VolumeBox::writeToNBT).forEach(boxesTag::appendTag);
        nbt.setTag("boxes", boxesTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        boxes.clear();
        NBTTagList boxesTag = nbt.getTagList("boxes", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, boxesTag.tagCount()).mapToObj(boxesTag::getCompoundTagAt).map(VolumeBox::new).forEach(boxes::add);
    }

    public static WorldSavedDataVolumeBoxes get(World world) {
        if (world.isRemote) {
            BCLog.logger.warn("Creating VolumeBoxes on client, this is a bug");
        }
        MapStorage storage = world.getPerWorldStorage();
        WorldSavedDataVolumeBoxes instance = (WorldSavedDataVolumeBoxes) storage.getOrLoadData(WorldSavedDataVolumeBoxes.class, DATA_NAME);
        if(instance == null) {
            instance = new WorldSavedDataVolumeBoxes();
            storage.setData(DATA_NAME, instance);
        }
        instance.world = world;
        return instance;
    }
}
