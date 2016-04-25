package buildcraft.lib.gui;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.command.IPayloadReceiver;
import buildcraft.lib.net.command.IPayloadWriter;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public abstract class Widget_BC8<C extends ContainerBC8> implements IPayloadReceiver {
    public final C container;
    public boolean hidden;

    public Widget_BC8(C container) {
        this.container = container;
    }

    // Net updating

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public IMessage handleWidgetDataServer(PacketBuffer buffer) throws IOException {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IMessage handleWidgetDataClient(PacketBuffer buffer) throws IOException {
        return null;
    }

    @Override
    public IMessage receivePayload(Side side, PacketBuffer buffer) throws IOException {
        if (side == Side.CLIENT) return handleWidgetDataClient(buffer);
        return handleWidgetDataServer(buffer);
    }
}
