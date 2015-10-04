/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.network.PacketIds;

import io.netty.buffer.ByteBuf;

public class PacketGuiWidget extends Packet {

    private byte windowId, widgetId;
    private byte[] payload;

    public PacketGuiWidget() {
        super();
    }

    public PacketGuiWidget(int windowId, int widgetId, byte[] data) {
        this.windowId = (byte) windowId;
        this.widgetId = (byte) widgetId;
        this.payload = data;
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);
        data.writeByte(windowId);
        data.writeByte(widgetId);
        data.writeBytes(payload);
    }

    @Override
    public void readData(ByteBuf data, World world, EntityPlayer player) {
        super.readData(data, world, player);
        windowId = data.readByte();
        widgetId = data.readByte();

        if (player.openContainer instanceof BuildCraftContainer && player.openContainer.windowId == windowId) {
            ((BuildCraftContainer) player.openContainer).handleWidgetClientData(widgetId, data);
        }
    }

    @Override
    public int getID() {
        return PacketIds.GUI_WIDGET;
    }

    @Override
    public void applyData(World world) {
        // TODO Auto-generated method stub

    }

}
