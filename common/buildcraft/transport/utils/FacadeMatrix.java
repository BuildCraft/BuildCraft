package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

public class FacadeMatrix {

	private final Block[] _blocks = new Block[ForgeDirection.VALID_DIRECTIONS.length];
	private final int[] _blockMetas = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private boolean dirty = false;

	public FacadeMatrix() {
	}

	public void setFacade(ForgeDirection direction, Block block, int blockMeta) {
		if (_blocks[direction.ordinal()] != block || _blockMetas[direction.ordinal()] != blockMeta) {
			_blocks[direction.ordinal()] = block;
			_blockMetas[direction.ordinal()] = blockMeta;
			dirty = true;
		}
	}

	public Block getFacadeBlock(ForgeDirection direction) {
		return _blocks[direction.ordinal()];
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
			data.writeShort(Block.blockRegistry.getIDForObject(_blocks[i]));
			data.writeByte(_blockMetas[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			short id = data.readShort();
			
			Block block = (Block) Block.blockRegistry.getObjectById(id);
			
			if (_blocks[i] != block) {
				_blocks[i] = block;
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
