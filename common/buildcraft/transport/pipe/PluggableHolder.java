/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.tile.TilePipeHolder;

public final class PluggableHolder {
    // TODO: Networking is kinda sub-par at the moment for pluggables
    // perhaps add some sort of interface for allowing pluggables to correctly write data?
    private static final IdAllocator ID_ALLOC = new IdAllocator("PlugHolder");
    public static final int ID_REMOVE_PLUG = ID_ALLOC.allocId("REMOVE_PLUG");
    public static final int ID_UPDATE_PLUG = ID_ALLOC.allocId("UPDATE_PLUG");
    public static final int ID_CREATE_PLUG = ID_ALLOC.allocId("CREATE_PLUG");

    public final TilePipeHolder holder;
    public final EnumFacing side;
    public PipePluggable pluggable;

    public PluggableHolder(TilePipeHolder holder, EnumFacing side) {
        this.holder = holder;
        this.side = side;
    }

    // Saving + Loading

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (pluggable != null) {
            nbt.setString("id", pluggable.definition.identifier.toString());
            nbt.setTag("data", pluggable.writeToNbt());
        }
        return nbt;
    }

    public void readFromNbt(NBTTagCompound nbt) {
        if (nbt.hasNoTags()) {
            pluggable = null;
            return;
        }
        String id = nbt.getString("id");
        NBTTagCompound data = nbt.getCompoundTag("data");
        ResourceLocation identifier = new ResourceLocation(id);
        PluggableDefinition def = PipeApi.pluggableRegistry.getDefinition(identifier);
        if (def == null) {
            BCLog.logger.warn("Unknown pluggable id '" + id + "'");
            throw new Error("Def was null!");
        } else {
            pluggable = def.readFromNbt(holder, side, data);
            holder.eventBus.registerHandler(pluggable);
        }
    }

    // Network

    /** Called by {@link TilePipeHolder#replacePluggable(EnumFacing, PipePluggable)} to inform clients about the new
     * pluggable. */
    public void sendNewPluggableData() {
        holder.sendMessage(PipeMessageReceiver.PLUGGABLES[side.ordinal()], this::writeCreationPayload);
    }

    public void writeCreationPayload(PacketBuffer buffer) {
        if (pluggable == null) {
            buffer.writeByte(ID_REMOVE_PLUG);
        } else {
            buffer.writeByte(ID_CREATE_PLUG);
            buffer.writeString(pluggable.definition.identifier.toString());
            pluggable.writeCreationPayload(buffer);
        }
    }

    public void readCreationPayload(PacketBuffer buffer) throws InvalidInputDataException {
        int id = buffer.readUnsignedByte();
        if (id == ID_CREATE_PLUG) {
            readCreateInternal(buffer);
        } else if (id == ID_REMOVE_PLUG) {
            holder.eventBus.unregisterHandler(pluggable);
            pluggable = null;
        } else {
            throw new InvalidInputDataException("Invalid ID for creation! " + ID_ALLOC.getNameFor(id));
        }
    }

    private void readCreateInternal(PacketBuffer buffer) throws InvalidInputDataException {
        ResourceLocation identifier = new ResourceLocation(buffer.readString(256));
        PluggableDefinition def = PipeApi.pluggableRegistry.getDefinition(identifier);
        if (def == null) {
            throw new InvalidInputDataException("Unknown remote pluggable \"" + identifier + "\"");
        }
        if (pluggable != null) {
            holder.eventBus.unregisterHandler(pluggable);
        }
        pluggable = def.loadFromBuffer(holder, side, buffer);
        holder.eventBus.registerHandler(pluggable);
    }

    public void writePayload(PacketBufferBC buffer, Side netSide) {
        if (netSide == Side.CLIENT) {
            buffer.writeByte(ID_UPDATE_PLUG);
            if (pluggable != null) {
                pluggable.writePayload(buffer, netSide);
            }
        } else {
            if (pluggable == null) {
                buffer.writeByte(ID_REMOVE_PLUG);
            } else {
                buffer.writeByte(ID_UPDATE_PLUG);
                pluggable.writePayload(buffer, netSide);
            }
        }
    }

    public void readPayload(PacketBufferBC buffer, Side netSide, MessageContext ctx) throws IOException {
        int id = buffer.readUnsignedByte();
        if (netSide == Side.SERVER) {
            if (id == ID_UPDATE_PLUG) {
                if (pluggable != null) {
                    pluggable.readPayload(buffer, netSide, ctx);
                }
            } else {
                throw new InvalidInputDataException("Unknown ID " + ID_ALLOC.getNameFor(id));
            }
        } else {
            if (id == ID_REMOVE_PLUG) {
                holder.eventBus.unregisterHandler(pluggable);
                pluggable = null;
            } else if (id == ID_UPDATE_PLUG) {
                pluggable.readPayload(buffer, netSide, ctx);
            } else if (id == ID_CREATE_PLUG) {
                readCreateInternal(buffer);
            } else {
                throw new InvalidInputDataException("Unknown ID " + ID_ALLOC.getNameFor(id));
            }
        }
    }

    // Pluggable overrides

    public void onTick() {
        if (pluggable != null) {
            pluggable.onTick();
        }
    }
}
