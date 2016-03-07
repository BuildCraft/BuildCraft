/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.network.base.Packet;

import io.netty.buffer.ByteBuf;

public class PacketGuiWidget extends Packet {
    private byte windowId, widgetId;
    private byte[] payload;

    public PacketGuiWidget() {
        super();
    }

    public PacketGuiWidget(EntityPlayer player, int windowId, int widgetId, byte[] data) {
        super(player.worldObj);
        this.windowId = (byte) windowId;
        this.widgetId = (byte) widgetId;
        this.payload = data;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeByte(windowId);
        data.writeByte(widgetId);
        data.writeShort(payload.length);
        data.writeBytes(payload);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        windowId = data.readByte();
        widgetId = data.readByte();
        int length = data.readShort();
        payload = new byte[length];
        data.readBytes(payload);
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        if (player != null) {
            if (player.openContainer instanceof BuildCraftContainer && player.openContainer.windowId == windowId) {
                BuildCraftContainer bcContainer = (BuildCraftContainer) player.openContainer;
                bcContainer.handleWidgetData(widgetId, payload);
            }
        } else {
            BCLog.logger.warn("No player!");
        }
    }
}
