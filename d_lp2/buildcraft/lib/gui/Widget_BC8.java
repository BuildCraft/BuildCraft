package buildcraft.lib.gui;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.command.IPayloadWriter;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public class Widget_BC8<C extends ContainerBC8> {
    public final C container;
    public boolean hidden;

    public Widget_BC8(C container) {
        this.container = container;
    }

    // Net updating

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public void handleWidgetDataServer(PacketBuffer buffer) throws IOException {}

    @SideOnly(Side.CLIENT)
    public void handleWidgetDataClient(PacketBuffer buffer) throws IOException {}
}
