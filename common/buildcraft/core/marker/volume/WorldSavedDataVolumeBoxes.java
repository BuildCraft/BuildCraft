/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

// Yarn 1.20.1 renames:
//   WorldSavedData          → PersistentState
//   NbtCompound          → NbtCompound
//   PlayerEntity            → PlayerEntity
//   Box           → net.minecraft.util.math.Box
//   World#isRemote          → World#isClient
//   new BlockPos(Vec3d)     → BlockPos.ofFloored(Vec3d)
//   player.getEyePos()      replaces getPositionVector().add(0, getEyeHeight(), 0)
//   player.getRotationVec(1.0f)     → player.getRotationVector()
//   Vec3d.multiply()           → Vec3d.multiply()
//   MapStorage.getOrLoadData → ServerWorld.getPersistentStateManager()
//                              .getOrCreate(Function<NbtCompound,T>, Supplier<T>, String)
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.BCNetworkManager;

public class WorldSavedDataVolumeBoxes extends PersistentState {
    private static final String DATA_NAME = "buildcraft_volume_boxes";

    public final World world;
    public World getWorld() { return world; }
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();

    public WorldSavedDataVolumeBoxes(World world) {
        this.world = world;
    }

    public VolumeBox getVolumeBoxAt(BlockPos pos) {
        return volumeBoxes.stream().filter(vb -> vb.box.contains(pos)).findFirst().orElse(null);
    }

    public void addVolumeBox(BlockPos pos) {
        volumeBoxes.add(new VolumeBox(world, pos));
    }

    public VolumeBox getVolumeBoxFromId(UUID id) {
        return volumeBoxes.stream().filter(vb -> vb.id.equals(id)).findFirst().orElse(null);
    }

    public VolumeBox getCurrentEditing(PlayerEntity player) {
        return volumeBoxes.stream().filter(vb -> vb.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        volumeBoxes.stream().filter(VolumeBox::isEditing).forEach(vb -> {
            PlayerEntity player = vb.getPlayer(world);
            if (player == null) {
                vb.pauseEditing();
                dirty.set(true);
            } else {
                net.minecraft.util.math.Box oldAabb = vb.box.getBoundingBox();
                vb.box.reset();
                vb.box.extendToEncompass(vb.getHeld());
                // player.getEyePos() replaces getPositionVector().add(0, getEyeHeight(), 0)
                BlockPos lookingAt = BlockPos.ofFloored(
                    player.getEyePos()
                        .add(player.getRotationVector().multiply(vb.getDist()))
                );
                vb.box.extendToEncompass(lookingAt);
                if (!vb.box.getBoundingBox().equals(oldAabb)) {
                    dirty.set(true);
                }
            }
        });
        for (VolumeBox vb : volumeBoxes) {
            List<Lock> locksToRemove = new ArrayList<>(vb.locks).stream()
                .filter(lock -> !lock.cause.stillWorks(world))
                .collect(Collectors.toList());
            if (!locksToRemove.isEmpty()) {
                vb.locks.removeAll(locksToRemove);
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
        // TODO(R.Chen): use PlayerLookup.getWorld()(serverWorld) for dimension-scoped broadcast once
        // BCNetworkManager exposes a sendToWorld helper; sendToAll is a safe over-send for now.
        if (world instanceof ServerWorld serverWorld) {
            BCNetworkManager.sendToAll(MessageVolumeBoxes.of(volumeBoxes), serverWorld.getServer());
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("volumeBoxes", NBTUtilBC.writeCompoundList(volumeBoxes.stream().map(VolumeBox::writeToNBT)));
        return nbt;
    }

    public static WorldSavedDataVolumeBoxes fromNbt(NbtCompound nbt, World world) {
        WorldSavedDataVolumeBoxes instance = new WorldSavedDataVolumeBoxes(world);
        NBTUtilBC.readCompoundList(nbt.get("volumeBoxes"))
            .map(tag -> new VolumeBox(world, tag))
            .forEach(instance.volumeBoxes::add);
        return instance;
    }

    public static WorldSavedDataVolumeBoxes get(World world) {
        if (world.isClient) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        ServerWorld serverWorld = (ServerWorld) world;
        return serverWorld.getPersistentStateManager().getOrCreate(
            nbt -> WorldSavedDataVolumeBoxes.fromNbt(nbt, world),
            () -> new WorldSavedDataVolumeBoxes(world),
            DATA_NAME
        );
    }
}
