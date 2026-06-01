/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.wire;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

// Yarn 1.20.1 renames:
//   DyeColor → DyeColor   Direction → Direction   NbtCompound → NbtCompound
//   BlockEntity   → BlockEntity   world.isClient → world.isClient   getTileEntity → getBlockEntity
//   @Environment(EnvType.CLIENT) → @Environment(EnvType.CLIENT)   Side → EnvType   MessageContext → Object (stub)
//   DyeColor.byId(i) → DyeColor.byId(i)   color.getId() → color.getId()
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

public class WireManager implements IWireManager {
    private final IPipeHolder holder;
    public final Map<EnumWirePart, DyeColor> parts = new EnumMap<>(EnumWirePart.class);
    public final Set<EnumWirePart> poweredClient = EnumSet.noneOf(EnumWirePart.class);
    public final Map<EnumWireBetween, DyeColor> betweens = new EnumMap<>(EnumWireBetween.class);
    public boolean initialised = false;
    // TODO: Wire connections to adjacent blocks

    public WireManager(IPipeHolder holder) {
        this.holder = holder;
    }

    public WorldSavedDataWireSystems getWireSystems() {
        return WorldSavedDataWireSystems.get(holder.getPipeWorld());
    }

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    public void invalidate() {
        if (!holder.getPipeWorld().isClient) {
            removePartsFromSystem(parts.keySet());
        }
    }

    public void validate() {
        if (!holder.getPipeWorld().isClient) {
            initialised = false;
        }
    }

    public void tick() {
        if (!initialised) {
            initialised = true;
            if (!holder.getPipeWorld().isClient) {
                for (EnumWirePart part : parts.keySet()) {
                    getWireSystems().buildAndAddWireSystem(new WireSystem.WireElement(holder.getPipePos(), part));
                }
            }
            updateBetweens(false);
        }
    }

    @Override
    public boolean addPart(EnumWirePart part, DyeColor colour) {
        if (getColorOfPart(part) == null) {
            parts.put(part, colour);
            if (!holder.getPipeWorld().isClient) {
                getWireSystems().buildAndAddWireSystem(new WireSystem.WireElement(holder.getPipePos(), part));
                holder.getPipeTile().markDirty();
            }
            updateBetweens(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DyeColor removePart(EnumWirePart part) {
        DyeColor color = getColorOfPart(part);
        if (color == null) {
            return null;
        } else {
            parts.remove(part);
            if (!holder.getPipeWorld().isClient) {
                WireSystem.WireElement element = new WireSystem.WireElement(holder.getPipePos(), part);
                WireSystem.getConnectedElementsOfElement(holder, element)
                    .forEach(getWireSystems()::buildAndAddWireSystem);
                getWireSystems().getWireSystemsWithElement(element).forEach(getWireSystems()::removeWireSystem);
                holder.getPipeTile().markDirty();
            }
            updateBetweens(false);
            return color;
        }
    }

    public void removeParts(Collection<EnumWirePart> toRemove) {
        toRemove.forEach(this.parts::remove);
        if (!holder.getPipeWorld().isClient) {
            removePartsFromSystem(toRemove);
        }
        updateBetweens(false);
    }

    private void removePartsFromSystem(Collection<EnumWirePart> toRemove) {
        toRemove.stream().map(part -> new WireSystem.WireElement(holder.getPipePos(), part))
            .flatMap(element -> WireSystem.getConnectedElementsOfElement(holder, element).stream()).distinct()
            .forEach(getWireSystems()::buildAndAddWireSystem);
        toRemove.stream().map(part -> new WireSystem.WireElement(holder.getPipePos(), part))
            .flatMap(element -> getWireSystems().getWireSystemsWithElement(element).stream())
            .forEach(getWireSystems()::removeWireSystem);
        holder.getPipeTile().markDirty();
    }

    @Override
    public void updateBetweens(boolean recursive) {
        betweens.clear();
        parts.forEach((part, color) -> {
            for (EnumWireBetween between : EnumWireBetween.VALUES) {
                EnumWirePart[] betweenParts = between.parts;
                if (between.to == null) {
                    if ((betweenParts[0] == part && getColorOfPart(betweenParts[1]) == color)
                        || (betweenParts[1] == part && getColorOfPart(betweenParts[0]) == color)) {
                        betweens.put(between, color);
                    }
                } else if (WireSystem.canWireConnect(holder, between.to)) {
                    IPipe pipe = holder.getNeighbourPipe(between.to);
                    if (pipe != null) {
                        IWireManager wireManager = pipe.getHolder().getWireManager();
                        if (betweenParts[0] == part && wireManager.getColorOfPart(betweenParts[1]) == color) {
                            betweens.put(between, color);
                        }
                    }
                }
            }
        });

        if (!recursive) {
            for (Direction side : Direction.values()) {
                BlockEntity tile = holder.getPipeWorld().getBlockEntity(holder.getPipePos().offset(side));
                if (tile instanceof IPipeHolder) {
                    ((IPipeHolder) tile).getWireManager().updateBetweens(true);
                }
            }
        }
    }

    @Override
    public DyeColor getColorOfPart(EnumWirePart part) {
        return parts.get(part);
    }

    @Override
    public boolean hasPartOfColor(DyeColor color) {
        return parts.values().contains(color);
    }

    @Override
    public boolean isPowered(EnumWirePart part) {
        if (holder.getPipeWorld().isClient) {
            return poweredClient.contains(part);
        } else {
            WorldSavedDataWireSystems wireSystems = this.getWireSystems();
            List<WireSystem> wireSystemsWithElement = wireSystems.getWireSystemsWithElementAsReadOnlyList(new WireSystem.WireElement(holder.getPipePos(), part));
            if (!wireSystemsWithElement.isEmpty()) {
                for (WireSystem wireSystem : wireSystemsWithElement) {
                    Boolean powered = wireSystems.wireSystems.get(wireSystem);
                    if (powered != null && powered) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public boolean isAnyPowered(DyeColor color) {
        if (!this.parts.isEmpty()) {
            for (Map.Entry<EnumWirePart, DyeColor> partColor : this.parts.entrySet()) {
                if (partColor.getValue() == color && this.isPowered(partColor.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        int[] wiresArray = new int[parts.size() * 2];
        int[] i = { 0 };
        parts.forEach((part, color) -> {
            wiresArray[i[0]] = part.ordinal();
            wiresArray[i[0] + 1] = color.getId();
            i[0] += 2;
        });
        nbt.putIntArray("parts", wiresArray);
        return nbt;
    }

    public void readFromNbt(NbtCompound nbt) {
        parts.clear();
        int[] wiresArray = nbt.getIntArray("parts");
        for (int i = 0; i < wiresArray.length; i += 2) {
            parts.put(EnumWirePart.VALUES[wiresArray[i]], DyeColor.byId(wiresArray[i + 1]));
        }
    }

    public void writePayload(PacketBufferBC buffer, EnvType side) {
        if (side == EnvType.SERVER) {
            buffer.writeInt(parts.size());
            for (Entry<EnumWirePart, DyeColor> entry : parts.entrySet()) {
                buffer.writeEnumValue(entry.getKey());
                buffer.writeEnumValue(entry.getValue());
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public void readPayload(PacketBufferBC buffer, EnvType side, /* STUB(R.Chen): MessageContext */ Object ctx) throws IOException {
        if (side == EnvType.CLIENT) {
            parts.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                EnumWirePart part = buffer.readEnumValue(EnumWirePart.class);
                DyeColor colour = buffer.readEnumValue(DyeColor.class);
                parts.put(part, colour);
            }
            updateBetweens(false);
        }
    }
}
