package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;

public class FacadeMatrix extends ConnectionMatrix {
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

	@Override
	public boolean isDirty() {
		return dirty || super.isDirty();
	}

	@Override
	public void clean() {
		super.clean();
		dirty = false;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			_blockIds[i] = data.readInt();
			_blockMetas[i] = data.readInt();
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeInt(_blockIds[i]);
			data.writeInt(_blockMetas[i]);
		}
	}
}
