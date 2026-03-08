/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class MessageUpdateTile implements IMessage {
    private BlockPos pos;
    // private PacketBufferBC payload;
    public PacketBufferBC payload;

    @SuppressWarnings("unused")
    public MessageUpdateTile() {
    }

    public MessageUpdateTile(BlockPos pos, PacketBufferBC payload) {
        this.pos = pos;
        this.payload = payload;
        if (getPayloadSize() > 1 << 24) {
            throw new IllegalStateException("Can't write out " + getPayloadSize() + "bytes!");
        }
    }

    public int getPayloadSize() {
        return payload == null ? 0 : payload.readableBytes();
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = MessageUtil.readBlockPos(new PacketBuffer(buf));
        int size = buf.readUnsignedMedium();
        payload = new PacketBufferBC(buf.readBytes(size));
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        MessageUtil.writeBlockPos(new PacketBuffer(buf), pos);
        int length = payload.readableBytes();
        buf.writeMedium(length);
        buf.writeBytes(payload, 0, length);
    }

    public static final IMessageHandler<MessageUpdateTile, IMessage> HANDLER = (message, ctx) ->
    {
        try {
            PlayerEntity player = BCLibProxy.getProxy().getPlayerForContext(ctx);
            if (player == null || player.level == null) {
                return null;
            }
            TileEntity tile = player.level.getBlockEntity(message.pos);
            if (tile instanceof IPayloadReceiver) {
                return ((IPayloadReceiver) tile).receivePayload(ctx, message.payload);
            } else {
                BCLog.logger.warn("Dropped message for player " + player.getName().getString() + " for tile at " + message.pos
                        + " (found " + tile + ")");
            }
            return null;
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            message.payload.release();
        }
    };
}
