/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;

import buildcraft.transport.network.PacketFluidUpdate;
import buildcraft.transport.network.PacketPipeTransportItemStack;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.network.PacketPowerUpdate;

public class BuildCraftChannelHandler extends FMLIndexedMessageToMessageCodec<BuildCraftPacket> {

    public BuildCraftChannelHandler() {
		addDiscriminator(0, PacketTileUpdate.class);
		addDiscriminator(1, PacketTileState.class);
		addDiscriminator(2, PacketFluidUpdate.class);
		addDiscriminator(3, PacketNBT.class);
		addDiscriminator(4, PacketPowerUpdate.class);
		addDiscriminator(5, PacketSlotChange.class);
		addDiscriminator(6, PacketGuiReturn.class);
		addDiscriminator(7, PacketGuiWidget.class);
		addDiscriminator(8, PacketPipeTransportItemStack.class);
		addDiscriminator(9, PacketPipeTransportItemStackRequest.class);
		addDiscriminator(10, PacketPipeTransportTraveler.class);
		addDiscriminator(11, PacketUpdate.class);
		addDiscriminator(12, PacketCommand.class);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, BuildCraftPacket packet, ByteBuf data) throws Exception {
        packet.writeData(data);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BuildCraftPacket packet) {
		packet.readData(data);
    }
}
