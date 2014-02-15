/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implemented by classes representing serializable client state
 */
public interface IClientState {
	/**
	 * Serializes the state to the stream
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void writeData(ByteBuf data);

	/**
	 * Deserializes the state from the stream
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void readData(ByteBuf data);
}
