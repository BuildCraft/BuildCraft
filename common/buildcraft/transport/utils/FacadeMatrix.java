package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;

public class FacadeMatrix {
	private final int[] _blockIds = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private final int[] _blockMetas = new int[ForgeDirection.VALID_DIRECTIONS.length];

	private boolean dirty = false;

	public FacadeMatrix() {
	}

	public void setFacade(ForgeDirection direction, int blockId, int blockMeta){
		if (_blockIds[direction.ordinal()] != blockId || _blockMetas[direction.ordinal()] != blockMeta){
			_blockIds[direction.ordinal()] = blockId;
			_blockMetas[direction.ordinal()] = blockMeta;
			dirty = true;
		}
	}
	
	public int getFacadeBlockId(ForgeDirection direction){
		return _blockIds[direction.ordinal()];
	}
	
	public int getFacadeMetaId(ForgeDirection direction){
		return _blockMetas[direction.ordinal()];
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			_blockIds[i] = data.readInt();
			_blockMetas[i] = data.readInt();
		}
	}

	public void writeData(DataOutputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeInt(_blockIds[i]);
			data.writeInt(_blockMetas[i]);
		}
	}
}
