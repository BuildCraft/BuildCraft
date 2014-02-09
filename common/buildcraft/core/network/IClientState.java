package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implemented by classes representing serializable client state
 * 
 * @author Krapht
 * 
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
