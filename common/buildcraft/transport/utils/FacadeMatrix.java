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

	public void setFacade(ForgeDirection direction, int blockId, int blockMeta) {
		if (_blockIds[direction.ordinal()] != blockId || _blockMetas[direction.ordinal()] != blockMeta) {
			_blockIds[direction.ordinal()] = blockId;
			_blockMetas[direction.ordinal()] = blockMeta;
			dirty = true;
		}
	}

	public int getFacadeBlockId(ForgeDirection direction) {
		return _blockIds[direction.ordinal()];
	}

	public int getFacadeMetaId(ForgeDirection direction) {
		return _blockMetas[direction.ordinal()];
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(DataOutputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeShort(_blockIds[i]);
			data.writeByte(_blockMetas[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			short id = data.readShort();
			if (_blockIds[i] != id) {
				_blockIds[i] = id;
				dirty = true;
			}
			byte meta = data.readByte();
			if (_blockMetas[i] != meta) {
				_blockMetas[i] = meta;
				dirty = true;
			}
		}
	}
}
