/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.item.ItemDebugger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Calen 1.18.2: supported entity
public class MessageDebugRequest implements IMessage {
    private boolean isEntity = false;
    private BlockPos pos;
    private Direction side;
    private UUID uuid;

    @SuppressWarnings("unused")
    public MessageDebugRequest() {
    }

    public MessageDebugRequest(BlockPos pos, Direction side) {
        this.pos = pos;
        this.side = side;

        isEntity = false;
    }

    public MessageDebugRequest(UUID uuid) {
        this.uuid = uuid;

        isEntity = true;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeBoolean(isEntity);

        if (isEntity) {
            buf.writeUUID(uuid);
        } else {
            buf.writeBlockPos(pos);
            buf.writeEnum(side);
        }
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        isEntity = buf.readBoolean();

        if (isEntity) {
            uuid = buf.readUUID();
        } else {
            pos = buf.readBlockPos();
            side = buf.readEnum(Direction.class);
        }
    }

    public static final IMessageHandler<MessageDebugRequest, MessageDebugResponse> HANDLER = (message, ctx) ->
    {
        Player player = ctx.getSender();
        if (!ItemDebugger.isShowDebugInfo(player)) {
            return new MessageDebugResponse();
        }
        Object obj;
        if (message.isEntity) {
            obj = ((ServerLevel) player.level).getEntity(message.uuid);
        } else {
            obj = player.level.getBlockEntity(message.pos);
        }
        if (obj instanceof IDebuggable) {
//            List<String> left = new ArrayList<>();
//            List<String> right = new ArrayList<>();
            List<Component> left = new ArrayList<>();
            List<Component> right = new ArrayList<>();
            ((IDebuggable) obj).getDebugInfo(left, right, message.side);
            return new MessageDebugResponse(left, right);
        }
        return null;
    };
}
