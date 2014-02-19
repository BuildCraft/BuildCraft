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
import buildcraft.builders.network.PacketLibraryAction;
import buildcraft.transport.network.PacketFluidUpdate;
import buildcraft.transport.network.PacketGateExpansionMap;
import buildcraft.transport.network.PacketPipeTransportItemStack;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.network.PacketPowerUpdate;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;

public class BuildCraftChannelHandler extends FMLIndexedMessageToMessageCodec<BuildCraftPacket> {

    public BuildCraftChannelHandler() {
    	addDiscriminator(0, PacketTileUpdate.class);
    	addDiscriminator(1, PacketTileState.class);
    	addDiscriminator(2, PacketCoordinates.class);
    	addDiscriminator(3, PacketFluidUpdate.class);
    	addDiscriminator(4, PacketLibraryAction.class);
    	addDiscriminator(5, PacketNBT.class);
    	addDiscriminator(6, PacketPowerUpdate.class);
    	addDiscriminator(7, PacketSlotChange.class);
    	addDiscriminator(8, PacketGateExpansionMap.class);
    	addDiscriminator(9, PacketGuiReturn.class);
    	addDiscriminator(10, PacketGuiWidget.class);
    	addDiscriminator(11, PacketPipeTransportItemStack.class);
    	addDiscriminator(12, PacketPipeTransportItemStackRequest.class);
    	addDiscriminator(13, PacketPipeTransportTraveler.class);
    	addDiscriminator(14, PacketUpdate.class);
    	addDiscriminator(15, PacketRPCTile.class);
    	addDiscriminator(16, PacketRPCPipe.class);
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
