package buildcraft.transport;

import buildcraft.core.network.IClientState;
import buildcraft.transport.utils.ConnectionMatrix;
import buildcraft.transport.utils.FacadeMatrix;
import buildcraft.transport.utils.TextureMatrix;
import buildcraft.transport.utils.WireMatrix;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.util.Icon;

public class PipeRenderState implements IClientState {

	private boolean hasGate = false;
	private int gateIconIndex = 0;
	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final WireMatrix wireMatrix = new WireMatrix();
	public final ConnectionMatrix plugMatrix = new ConnectionMatrix();
	public final FacadeMatrix facadeMatrix = new FacadeMatrix();
	private boolean dirty = true;

	/*
	 * This is a placeholder for the pipe renderer to set to a value that the BlockGenericPipe->TileGenericPipe will then return the the WorldRenderer
	 */
	@SideOnly(Side.CLIENT)
	public Icon currentTexture;
	@SideOnly(Side.CLIENT)
	public Icon[] textureArray;

	public void setHasGate(boolean value) {
		if (hasGate != value) {
			hasGate = value;
			dirty = true;
		}
	}

	public boolean hasGate() {
		return hasGate;
	}

	public void setGateIconIndex(int value) {
		if (gateIconIndex != value) {
			gateIconIndex = value;
			dirty = true;
		}
	}

	public int getGateIconIndex() {
		return gateIconIndex;
	}

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		facadeMatrix.clean();
		wireMatrix.clean();
		plugMatrix.clean();
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || wireMatrix.isDirty() || facadeMatrix.isDirty() || plugMatrix.isDirty();
	}

	public boolean needsRenderUpdate() {
		return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || facadeMatrix.isDirty() || plugMatrix.isDirty();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeBoolean(hasGate);
		data.writeInt(gateIconIndex);
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		wireMatrix.writeData(data);
		facadeMatrix.writeData(data);
		plugMatrix.writeData(data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		hasGate = data.readBoolean();
		gateIconIndex = data.readInt();
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		wireMatrix.readData(data);
		facadeMatrix.readData(data);
		plugMatrix.readData(data);
	}
}
