package buildcraft.core.network;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class BuildCraftChannelHandler extends FMLIndexedMessageToMessageCodec<BuildCraftPacket> {
	
    public BuildCraftChannelHandler() {
    	addDiscriminator(0, PacketTileUpdate.class);
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
