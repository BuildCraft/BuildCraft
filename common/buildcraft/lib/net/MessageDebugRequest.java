/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.item.ItemDebugger;

public class MessageDebugRequest implements IMessage {
    private BlockPos pos;
    private Direction side;

    @SuppressWarnings("unused")
    public MessageDebugRequest() {}

    public MessageDebugRequest(BlockPos pos, Direction side) {
        this.pos = pos;
        this.side = side;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeBlockPos(pos);
        buf.writeEnumValue(side);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        pos = buf.readBlockPos();
        side = buf.readEnumValue(Direction.class);
    }

    public static final IMessageHandler<MessageDebugRequest, MessageDebugResponse> HANDLER = (message, ctx) -> {
        PlayerEntity player = ctx.getServerHandler().player;
        if (!ItemDebugger.isShowDebugInfo(player)) {
            return new MessageDebugResponse();
        }
        BlockEntity tile = player.getWorld().getBlockEntity(message.pos);
        if (tile instanceof IDebuggable) {
            List<String> left = new ArrayList<>();
            List<String> right = new ArrayList<>();
            ((IDebuggable) tile).getDebugInfo(left, right, message.side);
            return new MessageDebugResponse(left, right);
        }
        return null;
    };
}
