/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Yarn 1.20.1 renames (mirrors WorldSavedDataVolumeBoxes):
//   WorldSavedData         → PersistentState        NbtCompound → NbtCompound   NbtList → NbtList
//   ServerPlayerEntity         → ServerPlayerEntity      BlockEntity     → BlockEntity   DyeColor → DyeColor
//   world.getTileEntity    → world.getBlockEntity    world.isClient → world.isClient
//   MapStorage.getOrLoadData → ServerWorld.getPersistentStateManager()
//                              .getOrCreate(Function<NbtCompound,T>, Supplier<T>, String)
//   NbtElement.COMPOUND_TYPE → NbtElement.COMPOUND_TYPE
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

public class WorldSavedDataWireSystems extends PersistentState {
    public static final String DATA_NAME = "buildcraft_wire_systems";
    public World world;
    public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
    public boolean gatesChanged = true;
    public boolean structureChanged = true;
    public final List<WireSystem> changedSystems = new ArrayList<>();
    public final List<ServerPlayerEntity> changedPlayers = new ArrayList<>();
    public final Map<WireSystem.WireElement, IWireEmitter> emittersCache = new HashMap<>();

    private final Map<WireSystem.WireElement, List<WireSystem>> elementsToWireSystemsIndex = new HashMap<>();

    public WorldSavedDataWireSystems(World world) {
        this.world = world;
    }

    public void markStructureChanged() {
        structureChanged = true;
        gatesChanged = true;
        emittersCache.clear();
    }

    public List<WireSystem> getWireSystemsWithElement(WireSystem.WireElement element) {
        List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.get(element);
        return wireSystemsWithElement != null ? new ArrayList<>(wireSystemsWithElement) : Collections.emptyList();
    }

    public List<WireSystem> getWireSystemsWithElementAsReadOnlyList(WireSystem.WireElement element) {
        return this.elementsToWireSystemsIndex.getOrDefault(element, Collections.emptyList());
    }

    public void removeWireSystem(WireSystem wireSystem) {
        wireSystems.remove(wireSystem);
        wireSystem.elements.forEach(elementIn -> {
            elementsToWireSystemsIndex.computeIfPresent(elementIn, (element, wireSystems) -> {
                wireSystems.remove(wireSystem);
                return wireSystems.isEmpty() ? null : wireSystems;
            });
        });
        markStructureChanged();
    }

    public void addWireSystem(WireSystem wireSystem, boolean powered) {
        if (this.wireSystems.put(wireSystem, powered) == null) {
            wireSystem.elements.forEach(systemElement -> {
                List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.computeIfAbsent(systemElement, unused -> new ArrayList<>());
                if (wireSystemsWithElement.contains(wireSystem)) {
                    throw new IllegalStateException();
                }
                wireSystemsWithElement.add(wireSystem);
            });
        }
    }

    public void buildAndAddWireSystem(WireSystem.WireElement element) {
        WireSystem wireSystem = new WireSystem(this, element);
        if(!wireSystem.isEmpty()) {
            this.addWireSystem(wireSystem, false);
            wireSystems.put(wireSystem, wireSystem.update(this));
        }
        markStructureChanged();
    }

    public void rebuildWireSystemsAround(IPipeHolder holder) {
        Arrays.stream(EnumWirePart.values())
                .flatMap(part -> WireSystem.getConnectedElementsOfElement(world, new WireSystem.WireElement(holder.getPipePos(), part)).stream())
                .distinct()
                .forEach(this::buildAndAddWireSystem);
    }

    public IWireEmitter getEmitter(WireSystem.WireElement element) {
        if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            if (!emittersCache.containsKey(element)) {
                BlockEntity tile = world.getBlockEntity(element.blockPos);
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    PipePluggable plug = holder.getPluggable(element.emitterSide);
                    if(plug instanceof IWireEmitter) {
                        emittersCache.put(element, (IWireEmitter) plug);
                    }
                }
                if (!emittersCache.containsKey(element)) {
                    throw new IllegalStateException("Tried to get a wire element when none existed! THIS IS A BUG " + element);
                }
            }
            return emittersCache.get(element);
        }
        return null;
    }

    public boolean isEmitterEmitting(WireSystem.WireElement element, DyeColor color) {
        BlockEntity tile = world.getBlockEntity(element.blockPos);
        if(tile instanceof IPipeHolder) {
            IPipeHolder holder = (IPipeHolder) tile;
            if (holder.getPluggable(element.emitterSide) instanceof IWireEmitter) {
                return getEmitter(element).isEmitting(color);
            }
        }
        return false;
    }

    public void tick() {
        if(gatesChanged) {
            wireSystems.replaceAll((wireSystem, oldPowered) -> {
                boolean newPowered = wireSystem.update(this);
                if (oldPowered != newPowered) {
                    changedSystems.add(wireSystem);
                }
                return newPowered;
            });
        }
        // STUB(R.Chen): per-player wire sync deferred to Phase 5 with the BC networking layer. The original
        // loop walked world.getPlayers(ServerPlayerEntity.class, ...) and pushed MessageWireSystems /
        // MessageWireSystemsPowered through MessageManager.sendTo(); neither message is migrated yet.
        if(structureChanged || !changedSystems.isEmpty()) {
            markDirty();
        }
        structureChanged = false;
        changedSystems.clear();
        changedPlayers.clear();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList entriesList = new NbtList();
        wireSystems.forEach((wireSystem, powered) -> {
            NbtCompound entry = new NbtCompound();
            entry.put("wireSystem", wireSystem.writeToNBT());
            entry.putBoolean("powered", powered);
            entriesList.add(entry);
        });
        nbt.put("entries", entriesList);
        return nbt;
    }

    public static WorldSavedDataWireSystems fromNbt(NbtCompound nbt, World world) {
        WorldSavedDataWireSystems instance = new WorldSavedDataWireSystems(world);
        NbtList entriesList = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < entriesList.size(); i++) {
            NbtCompound entry = entriesList.getCompound(i);
            instance.addWireSystem(new WireSystem(entry.getCompound("wireSystem")), entry.getBoolean("powered"));
        }
        return instance;
    }

    public static WorldSavedDataWireSystems get(World world) {
        if(world.isClient) {
            throw new UnsupportedOperationException("Attempted to get WorldSavedDataWireSystems on the client!");
        }
        ServerWorld serverWorld = (ServerWorld) world;
        return serverWorld.getPersistentStateManager().getOrCreate(
            nbt -> WorldSavedDataWireSystems.fromNbt(nbt, world),
            () -> new WorldSavedDataWireSystems(world),
            DATA_NAME
        );
    }
}
