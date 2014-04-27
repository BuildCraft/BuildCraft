/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

public class FacadeMatrix {

	public static final int STATE_NORMAL = 0;
	public static final int STATE_PHASED = 1;

	private final int[] _types = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private final Block[] _blocks = new Block[ForgeDirection.VALID_DIRECTIONS.length];
	private final int[] _blockMetas = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private final int[] _state = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private boolean dirty = false;

	public FacadeMatrix() {
	}

	public void setFacade(ForgeDirection direction, int type, Block block, int blockMeta) {
		if (_types[direction.ordinal()] != type || _blocks[direction.ordinal()] != block || _blockMetas[direction.ordinal()] != blockMeta) {
			_types[direction.ordinal()] = type;
			_blocks[direction.ordinal()] = block;
			_blockMetas[direction.ordinal()] = blockMeta;
			_state[direction.ordinal()] = STATE_NORMAL;
			dirty = true;
		}
	}

	public void setFacadeState(ForgeDirection direction, int state) {
		_state[direction.ordinal()] = state;
		dirty = true;
	}

	public int getFacadeType(ForgeDirection direction) {
		return _types[direction.ordinal()];
	}

	public Block getFacadeBlock(ForgeDirection direction) {
		return _blocks[direction.ordinal()];
	}

	public int getFacadeMetaId(ForgeDirection direction) {
		return _blockMetas[direction.ordinal()];
	}

	public int getFacadeState(ForgeDirection direction) {
		return _state[direction.ordinal()];
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(ByteBuf data) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeInt(_types[i]);

			if (_blocks [i] == null) {
				data.writeShort(0);
			} else {
				data.writeShort(Block.blockRegistry.getIDForObject(_blocks[i]));
			}
			
			data.writeByte(_blockMetas[i]);
			data.writeInt(_state[i]);
		}
	}

	public void readData(ByteBuf data) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			_types[i] = data.readInt();

			short id = data.readShort();
			
			Block block;
			
			if (id == 0) {
				block = null;
			} else {
				block = (Block) Block.blockRegistry.getObjectById(id);
			}
			
			if (_blocks[i] != block) {
				_blocks[i] = block;
				dirty = true;
			}
			byte meta = data.readByte();
			if (_blockMetas[i] != meta) {
				_blockMetas[i] = meta;
				dirty = true;
			}

			_state[i] = data.readInt();
		}
	}
}
