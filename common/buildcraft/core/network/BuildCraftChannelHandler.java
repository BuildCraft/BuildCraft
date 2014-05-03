/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
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
import buildcraft.transport.network.PacketGateExpansionMap;
import buildcraft.transport.network.PacketPipeTransportItemStack;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.network.PacketPowerUpdate;

public class BuildCraftChannelHandler extends FMLIndexedMessageToMessageCodec<BuildCraftPacket> {

    public BuildCraftChannelHandler() {
    	addDiscriminator(0, PacketTileUpdate.class);
    	addDiscriminator(1, PacketTileState.class);
    	addDiscriminator(2, PacketCoordinates.class);
    	addDiscriminator(3, PacketFluidUpdate.class);
    	addDiscriminator(4, PacketNBT.class);
    	addDiscriminator(5, PacketPowerUpdate.class);
    	addDiscriminator(6, PacketSlotChange.class);
    	addDiscriminator(7, PacketGateExpansionMap.class);
    	addDiscriminator(8, PacketGuiReturn.class);
    	addDiscriminator(9, PacketGuiWidget.class);
    	addDiscriminator(10, PacketPipeTransportItemStack.class);
    	addDiscriminator(11, PacketPipeTransportItemStackRequest.class);
    	addDiscriminator(12, PacketPipeTransportTraveler.class);
    	addDiscriminator(13, PacketUpdate.class);
    	addDiscriminator(14, PacketRPCTile.class);
    	addDiscriminator(15, PacketRPCPipe.class);
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
