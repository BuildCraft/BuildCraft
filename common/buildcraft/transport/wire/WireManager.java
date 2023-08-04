/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;

import buildcraft.lib.net.PacketBufferBC;

public class WireManager implements IWireManager {
    private final IPipeHolder holder;
    public final Map<EnumWirePart, EnumDyeColor> parts = new EnumMap<>(EnumWirePart.class);
    public final Set<EnumWirePart> poweredClient = EnumSet.noneOf(EnumWirePart.class);
    public final Map<EnumWireBetween, EnumDyeColor> betweens = new EnumMap<>(EnumWireBetween.class);
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
        if (!holder.getPipeWorld().isRemote) {
            removePartsFromSystem(parts.keySet());
        }
    }

    public void validate() {
        if (!holder.getPipeWorld().isRemote) {
            initialised = false;
        }
    }

    public void tick() {
        if (!initialised) {
            initialised = true;
            if (!holder.getPipeWorld().isRemote) {
                for (EnumWirePart part : parts.keySet()) {
                    getWireSystems().buildAndAddWireSystem(new WireSystem.WireElement(holder.getPipePos(), part));
                }
            }
            updateBetweens(false);
        }
    }

    @Override
    public boolean addPart(EnumWirePart part, EnumDyeColor colour) {
        if (getColorOfPart(part) == null) {
            parts.put(part, colour);
            if (!holder.getPipeWorld().isRemote) {
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
    public EnumDyeColor removePart(EnumWirePart part) {
        EnumDyeColor color = getColorOfPart(part);
        if (color == null) {
            return null;
        } else {
            parts.remove(part);
            if (!holder.getPipeWorld().isRemote) {
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
        if (!holder.getPipeWorld().isRemote) {
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
            for (EnumFacing side : EnumFacing.VALUES) {
                TileEntity tile = holder.getPipeWorld().getTileEntity(holder.getPipePos().offset(side));
                if (tile instanceof IPipeHolder) {
                    ((IPipeHolder) tile).getWireManager().updateBetweens(true);
                }
            }
        }
    }

    @Override
    public EnumDyeColor getColorOfPart(EnumWirePart part) {
        return parts.get(part);
    }

    @Override
    public boolean hasPartOfColor(EnumDyeColor color) {
        return parts.values().contains(color);
    }

    @Override
    public boolean isPowered(EnumWirePart part) {
        if (holder.getPipeWorld().isRemote) {
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
    public boolean isAnyPowered(EnumDyeColor color) {
        if (!this.parts.isEmpty()) {
            for (Map.Entry<EnumWirePart, EnumDyeColor> partColor : this.parts.entrySet()) {
                if (partColor.getValue() == color && this.isPowered(partColor.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        int[] wiresArray = new int[parts.size() * 2];
        int[] i = { 0 };
        parts.forEach((part, color) -> {
            wiresArray[i[0]] = part.ordinal();
            wiresArray[i[0] + 1] = color.getMetadata();
            i[0] += 2;
        });
        nbt.setIntArray("parts", wiresArray);
        return nbt;
    }

    public void readFromNbt(NBTTagCompound nbt) {
        parts.clear();
        int[] wiresArray = nbt.getIntArray("parts");
        for (int i = 0; i < wiresArray.length; i += 2) {
            parts.put(EnumWirePart.VALUES[wiresArray[i]], EnumDyeColor.byMetadata(wiresArray[i + 1]));
        }
    }

    public void writePayload(PacketBufferBC buffer, Side side) {
        if (side == Side.SERVER) {
            buffer.writeInt(parts.size());
            for (Entry<EnumWirePart, EnumDyeColor> entry : parts.entrySet()) {
                buffer.writeEnumValue(entry.getKey());
                buffer.writeEnumValue(entry.getValue());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void readPayload(PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.CLIENT) {
            parts.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                EnumWirePart part = buffer.readEnumValue(EnumWirePart.class);
                EnumDyeColor colour = buffer.readEnumValue(EnumDyeColor.class);
                parts.put(part, colour);
            }
            updateBetweens(false);
        }
    }
}
