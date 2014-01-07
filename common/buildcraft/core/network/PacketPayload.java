package buildcraft.core.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

	public abstract void writeData(DataOutputStream data) throws IOException;

	public abstract void readData(DataInputStream data) throws IOException;
}
