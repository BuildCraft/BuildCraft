package buildcraft.lib.gui;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.command.IPayloadReceiver;
import buildcraft.lib.net.command.IPayloadWriter;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public abstract class Widget_Neptune<C extends ContainerBC_Neptune> implements IPayloadReceiver {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.player.worldObj.isRemote;
    }

    // Net updating

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public IMessage handleWidgetDataServer(MessageContext ctx, PacketBuffer buffer) throws IOException {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IMessage handleWidgetDataClient(MessageContext ctx, PacketBuffer buffer) throws IOException {
        return null;
    }

    @Override
    public IMessage receivePayload(MessageContext ctx, PacketBuffer buffer) throws IOException {
        if (ctx.side == Side.CLIENT) {
            return handleWidgetDataClient(ctx, buffer);
        } else {
            return handleWidgetDataServer(ctx, buffer);
        }
    }
}
