/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.BitSet;
import java.util.Random;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.ISerializable;
import buildcraft.core.utils.BitSetUtils;
import buildcraft.core.utils.Utils;

public class ZoneChunk implements ISerializable {

	public BitSet property;
	private boolean fullSet = false;

	public ZoneChunk() {
	}

	public boolean get(int xChunk, int zChunk) {
		if (fullSet) {
			return true;
		} else if (property == null) {
			return false;
		} else {
			return property.get(xChunk + zChunk * 16);
		}
	}

	public void set(int xChunk, int zChunk, boolean value) {
		if (value) {
			if (fullSet) {
				return;
			}

			if (property == null) {
				property = new BitSet(16 * 16);
			}

			property.set(xChunk + zChunk * 16, value);

			if (property.cardinality() >= 16 * 16) {
				property = null;
				fullSet = true;
			}
		} else {
			if (fullSet) {
				property = new BitSet(16 * 16);
				property.flip(0, 16 * 16 - 1);
				fullSet = false;
			} else if (property == null) {
				// Note - ZonePlan should usually destroy such chunks
				property = new BitSet(16 * 16);
			}

			property.set(xChunk + zChunk * 16, value);
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("fullSet", fullSet);

		if (property != null) {
			nbt.setByteArray("bits", BitSetUtils.toByteArray(property));
		}
	}

	public void readFromNBT(NBTTagCompound nbt) {
		fullSet = nbt.getBoolean("fullSet");

		if (nbt.hasKey("bits")) {
			property = BitSetUtils.fromByteArray(nbt.getByteArray("bits"));
		}
	}

	public BlockIndex getRandomBlockIndex(Random rand) {
		int x, z;

		if (fullSet) {
			x = rand.nextInt(16);
			z = rand.nextInt(16);
		} else {
			int bitId = rand.nextInt(property.cardinality());
			int bitPosition = property.nextSetBit(0);

			while (bitId > 0) {
				bitId--;

				bitPosition = property.nextSetBit(bitPosition);
			}

			z = bitPosition / 16;
			x = bitPosition - 16 * z;
		}

		return new BlockIndex(x, 0, z);
	}

	public boolean isEmpty() {
		return !fullSet && property.isEmpty();
	}

	@Override
	public void readData(ByteBuf stream) {
		if (stream.readBoolean()) {
			property = BitSetUtils.fromByteArray(Utils.readByteArray(stream));
		}
		fullSet = stream.readBoolean();
	}

	@Override
	public void writeData(ByteBuf stream) {
		if (property != null) {
			stream.writeBoolean(true);
			Utils.writeByteArray(stream, BitSetUtils.toByteArray(property));
		} else {
			stream.writeBoolean(false);
		}
		stream.writeBoolean(fullSet);
	}
}
