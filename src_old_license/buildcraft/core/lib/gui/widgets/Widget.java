/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui.widgets;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.gui.tooltips.IToolTipProvider;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class Widget implements IToolTipProvider {

    public final int x;
    public final int y;
    public final int u;
    public final int v;
    public final int w;
    public final int h;
    public boolean hidden;
    protected BuildCraftContainer container;

    public Widget(int x, int y, int u, int v, int w, int h) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.w = w;
        this.h = h;
    }

    public void addToContainer(BuildCraftContainer container) {
        this.container = container;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x - 1 && mouseX < x + w + 1 && mouseY >= y - 1 && mouseY < y + h + 1;
    }

    @SideOnly(Side.CLIENT)
    public boolean handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void handleMouseRelease(int mouseX, int mouseY, int eventType) {}

    @SideOnly(Side.CLIENT)
    public void handleMouseMove(int mouseX, int mouseY, int mouseButton, long time) {}

    /** Handles the message data from the server. The message from the client will be stored in <code>in</code> */
    @SideOnly(Side.CLIENT)
    public void handleClientPacketData(PacketBuffer in) throws IOException {}

    /** Handles the message data from the client. The message from the client will be stored in <code>in</code> */
    public void handleServerPacketData(PacketBuffer in) throws IOException {}

    @SideOnly(Side.CLIENT)
    public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
        gui.drawTexturedModalRect(guiX + x, guiY + y, u, v, w, h);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ToolTip getToolTip() {
        return null;
    }

    @Override
    public boolean isToolTipVisible() {
        return true;
    }

    public void initWidget(IContainerListener player) {}

    public void updateWidget(IContainerListener player) {}
}
