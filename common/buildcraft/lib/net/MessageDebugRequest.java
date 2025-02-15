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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class MessageDebugRequest implements IMessage {
    private BlockPos pos;
    private Direction side;

    @SuppressWarnings("unused")
    public MessageDebugRequest() {
    }

    public MessageDebugRequest(BlockPos pos, Direction side) {
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeBlockPos(pos);
        buf.writeEnum(side);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        pos = buf.readBlockPos();
        side = buf.readEnum(Direction.class);
    }

    public static final IMessageHandler<MessageDebugRequest, MessageDebugResponse> HANDLER = (message, ctx) ->
    {
        Player player = ctx.getSender();
        if (!ItemDebugger.isShowDebugInfo(player)) {
            return new MessageDebugResponse();
        }
        BlockEntity tile = player.level().getBlockEntity(message.pos);
        if (tile instanceof IDebuggable) {
//            List<String> left = new ArrayList<>();
//            List<String> right = new ArrayList<>();
            List<Component> left = new ArrayList<>();
            List<Component> right = new ArrayList<>();
            ((IDebuggable) tile).getDebugInfo(left, right, message.side);
            return new MessageDebugResponse(left, right);
        }
        return null;
    };
}
