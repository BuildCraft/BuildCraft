/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.BCLibProxy;

public class MessageDebuggableRequest implements IMessage {
    private BlockPos pos;
    private EnumFacing side;

    @SuppressWarnings("unused")
    public MessageDebuggableRequest() {
    }

    public MessageDebuggableRequest(BlockPos pos, EnumFacing side) {
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeBlockPos(pos);
        new PacketBufferBC(buf).writeEnumValue(side);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new PacketBuffer(buf).readBlockPos();
        side = new PacketBufferBC(buf).readEnumValue(EnumFacing.class);
    }

    public static final IMessageHandler<MessageDebuggableRequest, MessageDebuggableResponse> HANDLER = (message, ctx) -> {
        TileEntity tile = BCLibProxy.getProxy().getPlayerForContext(ctx).world.getTileEntity(message.pos);
        if (tile instanceof IDebuggable) {
            List<String> left = new ArrayList<>();
            List<String> right = new ArrayList<>();
            ((IDebuggable) tile).getDebugInfo(left, right, message.side);
            return new MessageDebuggableResponse(left, right);
        }
        return null;
    };
}
