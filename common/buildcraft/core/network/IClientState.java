package buildcraft.core.network;

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
	public void writeData(DataOutputStream data) throws IOException;

	/**
	 * Deserializes the state from the stream
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void readData(DataInputStream data) throws IOException;
}
