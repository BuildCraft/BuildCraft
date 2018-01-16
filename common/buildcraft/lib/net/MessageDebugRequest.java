/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.item.ItemDebugger;

public class MessageDebugRequest implements IMessage {
    private BlockPos pos;
    private EnumFacing side;

    @SuppressWarnings("unused")
    public MessageDebugRequest() {}

    public MessageDebugRequest(BlockPos pos, EnumFacing side) {
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeBlockPos(pos);
        buf.writeEnumValue(side);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        pos = buf.readBlockPos();
        side = buf.readEnumValue(EnumFacing.class);
    }

    public static final IMessageHandler<MessageDebugRequest, MessageDebugResponse> HANDLER = (message, ctx) -> {
        EntityPlayer player = ctx.getServerHandler().player;
        if (!player.capabilities.isCreativeMode &&
            !(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemDebugger) &&
            !(player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemDebugger)) {
            return new MessageDebugResponse();
        }
        TileEntity tile = player.world.getTileEntity(message.pos);
        if (tile instanceof IDebuggable) {
            List<String> left = new ArrayList<>();
            List<String> right = new ArrayList<>();
            ((IDebuggable) tile).getDebugInfo(left, right, message.side);
            return new MessageDebugResponse(left, right);
        }
        return null;
    };
}
