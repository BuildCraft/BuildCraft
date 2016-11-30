package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.IWireManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class MessageElementsPowered implements IMessage {
    private Map<WireSystem.Element, Boolean> elementsPowered = new HashMap<>();

    public MessageElementsPowered() {
    }

    public MessageElementsPowered(Map<WireSystem.Element, Boolean> elementsPowered) {
        this.elementsPowered = elementsPowered;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(elementsPowered.size());
        elementsPowered.forEach((element, powered) -> {
            element.toBytes(buf);
            buf.writeBoolean(powered);
        });
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        elementsPowered.clear();
        int count = buf.readInt();
        for(int i = 0; i < count; i++) {
            elementsPowered.put(new WireSystem.Element(buf), buf.readBoolean());
        }
    }

    public enum Handler implements IMessageHandler<MessageElementsPowered, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageElementsPowered message, MessageContext ctx) {
            message.elementsPowered.forEach((element, powered) -> {
                if(element.type == WireSystem.Element.Type.WIRE_PART) {
                    TileEntity tile = Minecraft.getMinecraft().theWorld.getTileEntity(element.blockPos);
                    if(tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IWireManager iWireManager = holder.getWireManager();
                        if(iWireManager instanceof WireManager) {
                            WireManager wireManager = (WireManager) iWireManager;
                            if(wireManager.getColorOfPart(element.wirePart) != null) {
                                if(powered) {
                                    wireManager.poweredClient.add(element.wirePart);
                                } else {
                                    wireManager.poweredClient.remove(element.wirePart);
                                }
                            }
                        }
                    }
                }
            });
            return null;
        }
    }
}
