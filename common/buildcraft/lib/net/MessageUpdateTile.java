/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Fabric 1.20.1 {@link FabricPacket} carrying a {@link PacketBufferBC} payload keyed by a tile's
 * {@link BlockPos}. Replaces the original Forge {@code IMessage}; the bit-level layered payload format
 * (PacketBufferBC) is preserved verbatim on the wire.
 */
public class MessageUpdateTile implements FabricPacket {

    public static final PacketType<MessageUpdateTile> TYPE = PacketType.create(
        new Identifier("buildcraft", "update_tile_bc"),
        MessageUpdateTile::read
    );

    private BlockPos pos;
    private PacketBufferBC payload;

    public MessageUpdateTile(BlockPos pos, PacketBufferBC payload) {
        this.pos = pos;
        this.payload = payload;
        if (getPayloadSize() > 1 << 24) {
            throw new IllegalStateException("Can't write out " + getPayloadSize() + "bytes!");
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public PacketBufferBC getPayload() {
        return payload;
    }

    public int getPayloadSize() {
        return payload == null ? 0 : payload.readableBytes();
    }

    public static MessageUpdateTile read(PacketByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readUnsignedMedium();
        return new MessageUpdateTile(pos, new PacketBufferBC(buf.readBytes(size)));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        int length = payload.readableBytes();
        buf.writeMedium(length);
        buf.writeBytes(payload, payload.readerIndex(), length);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    /** Convenience for callers that build a payload eagerly. */
    public static MessageUpdateTile of(BlockPos pos, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        writer.write(buffer);
        return new MessageUpdateTile(pos, buffer);
    }
}
