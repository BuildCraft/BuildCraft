/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

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
        if (!holder.getPipeWorld().isClientSide) {
            removePartsFromSystem(parts.keySet());
        }
    }

    public void validate() {
        if (!holder.getPipeWorld().isClientSide) {
            initialised = false;
        }
    }

    public void tick() {
        if (!initialised) {
            initialised = true;
            if (!holder.getPipeWorld().isClientSide) {
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
            if (!holder.getPipeWorld().isClientSide) {
                getWireSystems().buildAndAddWireSystem(new WireSystem.WireElement(holder.getPipePos(), part));
                holder.getPipeTile().setChanged();
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
            if (!holder.getPipeWorld().isClientSide) {
                WireSystem.WireElement element = new WireSystem.WireElement(holder.getPipePos(), part);
                WireSystem.getConnectedElementsOfElement(holder, element)
                        .forEach(getWireSystems()::buildAndAddWireSystem);
                getWireSystems().getWireSystemsWithElement(element).forEach(getWireSystems()::removeWireSystem);
                holder.getPipeTile().setChanged();
            }
            updateBetweens(false);
            return color;
        }
    }

    public void removeParts(Collection<EnumWirePart> toRemove) {
        toRemove.forEach(this.parts::remove);
        if (!holder.getPipeWorld().isClientSide) {
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
        holder.getPipeTile().setChanged();
    }

    @Override
    public void updateBetweens(boolean recursive) {
        betweens.clear();
        parts.forEach((part, color) ->
        {
            for (EnumWireBetween between : EnumWireBetween.VALUES) {
                EnumWirePart[] betweenParts = between.parts;
                if (between.to == null) {
                    if ((betweenParts[0] == part && getColorOfPart(betweenParts[1]) == color)
                            || (betweenParts[1] == part && getColorOfPart(betweenParts[0]) == color))
                    {
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
                BlockEntity tile = holder.getPipeWorld().getBlockEntity(holder.getPipePos().relative(side));
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
        if (holder.getPipeWorld().isClientSide) {
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
            for (Entry<EnumWirePart, DyeColor> partColor : this.parts.entrySet()) {
                if (partColor.getValue() == color && this.isPowered(partColor.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        int[] wiresArray = new int[parts.size() * 2];
        int[] i = { 0 };
        parts.forEach((part, color) ->
        {
            wiresArray[i[0]] = part.ordinal();
            wiresArray[i[0] + 1] = color.getId();
            i[0] += 2;
        });
        nbt.putIntArray("parts", wiresArray);
        return nbt;
    }

    public void readFromNbt(CompoundTag nbt) {
        parts.clear();
        int[] wiresArray = nbt.getIntArray("parts");
        for (int i = 0; i < wiresArray.length; i += 2) {
            parts.put(EnumWirePart.VALUES[wiresArray[i]], DyeColor.byId(wiresArray[i + 1]));
        }
    }

    public void writePayload(PacketBufferBC buffer, Dist side) {
        if (side == Dist.DEDICATED_SERVER) {
            buffer.writeInt(parts.size());
            for (Entry<EnumWirePart, DyeColor> entry : parts.entrySet()) {
                buffer.writeEnum(entry.getKey());
                buffer.writeEnum(entry.getValue());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
//    public void readPayload(PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            parts.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                EnumWirePart part = buffer.readEnum(EnumWirePart.class);
                DyeColor colour = buffer.readEnum(DyeColor.class);
                parts.put(part, colour);
            }
//            updateBetweens(false);
            // Calen: when world loading, TilePipeHolder#getNeighbourTile -> [lib.tile] Ghost-loading tile at ...
            getHolder().runWhenWorldNotNull(() -> updateBetweens(false), true);
        }
    }
}
