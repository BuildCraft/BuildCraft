/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.io.IOException;
import net.minecraft.network.packet.Packet;

public interface ISynchronizedTile {

	public void handleDescriptionPacket(PacketUpdate packet) throws IOException;

	public void handleUpdatePacket(PacketUpdate packet) throws IOException;

	public void postPacketHandling(PacketUpdate packet);

	public Packet getUpdatePacket();

	public Packet getDescriptionPacket();

	public PacketPayload getPacketPayload();
}
