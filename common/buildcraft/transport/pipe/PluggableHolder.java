package buildcraft.transport.pipe;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.tile.TilePipeHolder;

public final class PluggableHolder {
    public final TilePipeHolder holder;
    public final EnumFacing side;
    public PipePluggable pluggable;

    public PluggableHolder(TilePipeHolder holder, EnumFacing side) {
        this.holder = holder;
        this.side = side;
    }

    // TODO: NBT read + write
    // TODO: Merge creation with normal net read+write

    // Network

    public void writeCreationPayload(PacketBuffer buffer) {
        if (pluggable == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeString(pluggable.definition.identifier.toString());
            pluggable.writePayload(buffer, Side.SERVER);
        }
    }

    public void readCreationPayload(PacketBuffer buffer, MessageContext ctx) throws IOException {
        if (buffer.readBoolean()) {
            ResourceLocation identifer = new ResourceLocation(buffer.readStringFromBuffer(256));
            PluggableDefinition def = PipeAPI.pluggableRegistry.getDefinition(identifer);
            if (def == null) {
                throw new IllegalStateException("Unknown remote pluggable \"" + identifer + "\"");
            }
            pluggable = def.pluggableConstructor.createPluggable(holder, side);
            pluggable.readPayload(buffer, Side.CLIENT, ctx);
        } else {
            pluggable = null;
        }
    }

    public void writePayload(PacketBuffer buffer, Side netSide) {
        if (pluggable == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeString(pluggable.definition.identifier.toString());
            pluggable.writePayload(buffer, netSide);
        }
    }

    public void readPayload(PacketBuffer buffer, Side netSide, MessageContext ctx) throws IOException {
        if (buffer.readBoolean()) {
            ResourceLocation identifer = new ResourceLocation(buffer.readStringFromBuffer(256));
            PluggableDefinition def = PipeAPI.pluggableRegistry.getDefinition(identifer);
            if (def == null) {
                throw new IllegalStateException("Unknown remote pluggable \"" + identifer + "\"");
            }
            pluggable = def.pluggableConstructor.createPluggable(holder, side);
            pluggable.readPayload(buffer, netSide, ctx);
        } else {
            pluggable = null;
        }
    }

    // Pluggable overrides

    public void onTick() {
        if (pluggable != null) {
            pluggable.onTick();
        }
    }
}
