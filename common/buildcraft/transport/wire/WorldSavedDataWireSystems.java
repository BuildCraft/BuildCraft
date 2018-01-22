/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.net.MessageManager;
import buildcraft.transport.plug.PluggableGate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorldSavedDataWireSystems extends WorldSavedData {
    public static final String DATA_NAME = "buildcraft_wire_systems";
    public World world;
    public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
    public boolean gatesChanged = true;
    public boolean structureChanged = true;
    public final List<WireSystem> changedSystems = new ArrayList<>();
    public final List<EntityPlayerMP> changedPlayers = new ArrayList<>();
    public final Map<WireSystem.WireElement, IWireEmitter> emittersCache = new HashMap<>();

    public WorldSavedDataWireSystems() {
        super(DATA_NAME);
    }

    public WorldSavedDataWireSystems(String name) {
        super(name);
    }

    public void markStructureChanged() {
        structureChanged = true;
        gatesChanged = true;
        emittersCache.clear();
    }

    public List<WireSystem> getWireSystemsWithElement(WireSystem.WireElement element) {
        return wireSystems.keySet().stream().filter(wireSystem -> wireSystem.hasElement(element)).collect(Collectors.toList());
    }

    public void removeWireSystem(WireSystem wireSystem) {
        wireSystems.remove(wireSystem);
        markStructureChanged();
    }

    public void buildAndAddWireSystem(WireSystem.WireElement element) {
        WireSystem wireSystem = new WireSystem().build(this, element);
        if(!wireSystem.isEmpty()) {
            wireSystems.put(wireSystem, false);
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
        if(element.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            if(!emittersCache.containsKey(element)) {
                TileEntity tile = world.getTileEntity(element.blockPos);
                if(tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                        PluggableGate gate = (PluggableGate) holder.getPluggable(element.emitterSide);
                        emittersCache.put(element, gate.logic);
                    }
                }
                if(!emittersCache.containsKey(element)) {
                    throw new IllegalStateException("Tried to get a wire element when none existed! THIS IS A BUG " + element);
                }
            }
            return emittersCache.get(element);
        }
        return null;
    }

    public boolean isEmitterEmitting(WireSystem.WireElement element, EnumDyeColor color) {
        TileEntity tile = world.getTileEntity(element.blockPos);
        if(tile instanceof IPipeHolder) {
            IPipeHolder holder = (IPipeHolder) tile;
            if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                return getEmitter(element).isEmitting(color);
            }
        }
        return false;
    }

    public void tick() {
        if(gatesChanged) {
            wireSystems.keySet().stream()
                    .filter(wireSystem -> {
                        boolean newPowered = wireSystem.update(this);
                        return wireSystems.put(wireSystem, newPowered) != newPowered;
                    })
                    .forEach(changedSystems::add);
        }
        world.getPlayers(EntityPlayerMP.class, entityPlayerMP -> true).forEach(player -> {
            Map<Integer, WireSystem> changedWires = this.wireSystems.keySet().stream()
                    .filter(wireSystem -> wireSystem.isPlayerWatching(player) && (structureChanged || changedPlayers.contains(player)))
                    .collect(Collectors.toMap(WireSystem::getWiresHashCode, Function.identity()));
            if(!changedWires.isEmpty()) {
                MessageManager.sendTo(new MessageWireSystems(changedWires), player);
            }
            Map<Integer, Boolean> hashesPowered = this.wireSystems.entrySet().stream()
                    .filter(systemPower ->
                            systemPower.getKey().isPlayerWatching(player) &&
                                    (structureChanged || changedSystems.contains(systemPower.getKey()) || changedPlayers.contains(player))
                    )
                    .map(systemPowered -> Pair.of(systemPowered.getKey().getWiresHashCode(), systemPowered.getValue()))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            if(!hashesPowered.isEmpty()) {
                MessageManager.sendTo(new MessageWireSystemsPowered(hashesPowered), player);
            }
        });
        if(structureChanged || !changedSystems.isEmpty()) {
            markDirty();
        }
        structureChanged = false;
        changedSystems.clear();
        changedPlayers.clear();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList entriesList = new NBTTagList();
        wireSystems.forEach((wireSystem, powered) -> {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setTag("wireSystem", wireSystem.writeToNBT());
            entry.setBoolean("powered", powered);
            entriesList.appendTag(entry);
        });
        nbt.setTag("entries", entriesList);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        wireSystems.clear();
        NBTTagList entriesList = nbt.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < entriesList.tagCount(); i++) {
            NBTTagCompound entry = entriesList.getCompoundTagAt(i);
            wireSystems.put(new WireSystem().readFromNBT(entry.getCompoundTag("wireSystem")), entry.getBoolean("powered"));
        }
    }

    public static WorldSavedDataWireSystems get(World world) {
        if(world.isRemote) {
            BCLog.logger.warn("Creating WireSystems on client, this is a bug");
        }
        MapStorage storage = world.getPerWorldStorage();
        WorldSavedDataWireSystems instance = (WorldSavedDataWireSystems) storage.getOrLoadData(WorldSavedDataWireSystems.class, DATA_NAME);
        if (instance == null) {
            instance = new WorldSavedDataWireSystems();
            storage.setData(DATA_NAME, instance);
        }
        instance.world = world;
        return instance;
    }
}
