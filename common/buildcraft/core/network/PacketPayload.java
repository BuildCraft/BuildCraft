/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * FIXME: Now that packet PayloadArray is removed, it's probably worth removing
 * the abstraction, and having just one class for PayloadStream.
 */
public abstract class PacketPayload {
	public static PacketPayload makePayload() {
		return new PacketPayloadStream();
	}

	public abstract void writeData(ByteBuf data);

	public abstract void readData(ByteBuf data);
}
