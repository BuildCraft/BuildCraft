/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import buildcraft.core.gui.BuildCraftContainer;
import cpw.mods.fml.client.FMLClientHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.client.entity.EntityClientPlayerMP;
/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class PacketGuiWidget extends BuildCraftPacket {

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
    public void writeData(DataOutputStream data) throws IOException {
        data.writeByte(windowId);
        data.writeByte(widgetId);
        data.write(payload);
    }

    @Override
    public void readData(DataInputStream data) throws IOException {
        windowId = data.readByte();
        widgetId = data.readByte();

        EntityClientPlayerMP player = FMLClientHandler.instance().getClient().thePlayer;

        if (player.openContainer instanceof BuildCraftContainer && player.openContainer.windowId == windowId)
            ((BuildCraftContainer) player.openContainer).handleWidgetClientData(widgetId, data);
    }

    @Override
    public int getID() {
        return PacketIds.GUI_WIDGET;
    }

}
