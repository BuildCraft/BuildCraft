/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.io.IOException;
import net.minecraft.network.Packet;

public interface ISynchronizedTile {

	public void handleDescriptionPacket(PacketUpdate packet) throws IOException;

	public void handleUpdatePacket(PacketUpdate packet) throws IOException;

	public void postPacketHandling(PacketUpdate packet);

	public BuildCraftPacket getUpdatePacket();

	public Packet getDescriptionPacket();

	public PacketPayload getPacketPayload();
}
