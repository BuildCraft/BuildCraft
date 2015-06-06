/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;

import buildcraft.core.lib.network.command.PacketCommand;

public class ChannelHandler extends FMLIndexedMessageToMessageCodec<Packet> {
    private int maxDiscriminator;

    public ChannelHandler() {
        // Packets common to buildcraft.core.network
        addDiscriminator(0, PacketTileUpdate.class);
        addDiscriminator(1, PacketTileState.class);
        addDiscriminator(2, PacketNBT.class);
        addDiscriminator(3, PacketSlotChange.class);
        addDiscriminator(4, PacketGuiReturn.class);
        addDiscriminator(5, PacketGuiWidget.class);
        addDiscriminator(6, PacketUpdate.class);
        addDiscriminator(7, PacketCommand.class);
        addDiscriminator(8, PacketEntityUpdate.class);
        maxDiscriminator = 9;
    }

    public void registerPacketType(Class<? extends Packet> packetType) {
        addDiscriminator(maxDiscriminator++, packetType);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet packet, ByteBuf data) throws Exception {
        packet.writeData(data);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, Packet packet) {
        packet.readData(data);
    }
}
