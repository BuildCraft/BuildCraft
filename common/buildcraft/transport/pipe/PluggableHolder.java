package buildcraft.transport.pipe;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipePluggable;
import buildcraft.api.transport.neptune.PluggableDefinition;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.tile.TilePipeHolder;

public final class PluggableHolder {
    private static final int ID_REMOVE_PLUG = 0;
    private static final int ID_UPDATE_PLUG = 1;
    private static final int ID_CREATE_PLUG = 2;

    public final TilePipeHolder holder;
    public final EnumFacing side;
    public PipePluggable pluggable;
    /** Used to determine if a full "create" message should be sent when {@link #writePayload(PacketBufferBC, Side)} is
     * called. If this is false it means that last time it was null, and a create message should. */
    private boolean lastGeneralExisted = false;

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
        PluggableDefinition def = PipeAPI.pluggableRegistry.getDefinition(identifier);
        if (def == null) {
            BCLog.logger.warn("Unknown pluggable id '" + id + "'");
            throw new Error("Def was null!");
        } else {
            pluggable = def.readFromNbt(holder, side, data);
            holder.eventBus.registerHandler(pluggable);
            lastGeneralExisted = true;
        }
    }

    // Network

    public void writeCreationPayload(PacketBuffer buffer) {
        if (pluggable == null) {
            buffer.writeByte(ID_REMOVE_PLUG);
        } else {
            buffer.writeByte(ID_CREATE_PLUG);
            buffer.writeString(pluggable.definition.identifier.toString());
            pluggable.writeCreationPayload(buffer);
        }
    }

    public void readCreationPayload(PacketBuffer buffer) {
        int id = buffer.readUnsignedByte();
        if (id == ID_CREATE_PLUG) {
            readCreateInternal(buffer);
        } else {
            holder.eventBus.unregisterHandler(pluggable);
            pluggable = null;
        }
    }

    private void readCreateInternal(PacketBuffer buffer) {
        ResourceLocation identifer = new ResourceLocation(buffer.readString(256));
        PluggableDefinition def = PipeAPI.pluggableRegistry.getDefinition(identifer);
        if (def == null) {
            throw new IllegalStateException("Unknown remote pluggable \"" + identifer + "\"");
        }
        pluggable = def.loadFromBuffer(holder, side, buffer);
        holder.eventBus.registerHandler(pluggable);
    }

    public void writePayload(PacketBufferBC buffer, Side netSide) {
        if (pluggable == null) {
            lastGeneralExisted = false;
            buffer.writeByte(ID_REMOVE_PLUG);
        } else if (lastGeneralExisted) {
            buffer.writeByte(ID_UPDATE_PLUG);
            pluggable.writePayload(buffer, netSide);
        } else {
            // The last general one did NOT exist, and so we need to create it
            lastGeneralExisted = true;
            writeCreationPayload(buffer);
        }
    }

    public void readPayload(PacketBufferBC buffer, Side netSide, MessageContext ctx) throws IOException {
        int id = buffer.readUnsignedByte();
        if (id == ID_REMOVE_PLUG) {
            holder.eventBus.unregisterHandler(pluggable);
            pluggable = null;
        } else if (id == ID_UPDATE_PLUG) {
            pluggable.readPayload(buffer, netSide, ctx);
        } else if (id == ID_CREATE_PLUG) {
            readCreateInternal(buffer);
        } else {
            BCLog.logger.warn("[PluggableHolder] Unknown ID " + id);
        }
    }

    // Pluggable overrides

    public void onTick() {
        if (pluggable != null) {
            pluggable.onTick();
        }
    }
}
