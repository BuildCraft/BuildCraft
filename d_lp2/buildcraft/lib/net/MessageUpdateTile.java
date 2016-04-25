package buildcraft.lib.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.lib.tile.TileBuildCraft_BC8;

import io.netty.buffer.ByteBuf;

public class MessageUpdateTile implements IMessage {
    
    public MessageUpdateTile() {
        // TODO Auto-generated constructor stub
    }
    
    public MessageUpdateTile(TileBuildCraft_BC8 tile, IPayloadWriter writer) {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

}
