/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.ISerializable;
import buildcraft.api.core.IZone;

public class ZonePlan implements IZone, ISerializable {
	private HashMap<ChunkIndex, ZoneChunk> chunkMapping = new HashMap<ChunkIndex, ZoneChunk>();

	public boolean get(int x, int z) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		ChunkIndex chunkId = new ChunkIndex(xChunk, zChunk);
		ZoneChunk property;

		if (!chunkMapping.containsKey(chunkId)) {
			return false;
		} else {
			property = chunkMapping.get(chunkId);
			return property.get(x & 0xF, z & 0xF);
		}
	}

	public void set(int x, int z, boolean val) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		ChunkIndex chunkId = new ChunkIndex(xChunk, zChunk);
		ZoneChunk property;

		if (!chunkMapping.containsKey(chunkId)) {
			if (val) {
				property = new ZoneChunk();
				chunkMapping.put(chunkId, property);
			} else {
				return;
			}
		} else {
			property = chunkMapping.get(chunkId);
		}

		property.set(x & 0xF, z & 0xF, val);

		if (property.isEmpty()) {
			chunkMapping.remove(chunkId);
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();

		for (Map.Entry<ChunkIndex, ZoneChunk> e : chunkMapping.entrySet()) {
			NBTTagCompound subNBT = new NBTTagCompound();
			e.getKey().writeToNBT(subNBT);
			e.getValue().writeToNBT(subNBT);
			list.appendTag(subNBT);
		}

		nbt.setTag("chunkMapping", list);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("chunkMapping", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound subNBT = list.getCompoundTagAt(i);

			ChunkIndex id = new ChunkIndex();
			id.readFromNBT(subNBT);

			ZoneChunk chunk = new ZoneChunk();
			chunk.readFromNBT(subNBT);

			chunkMapping.put(id, chunk);
		}
	}

	@Override
	public double distanceTo(BlockPos pos) {
		double maxSqrDistance = Double.MAX_VALUE;

		for (Map.Entry<ChunkIndex, ZoneChunk> e : chunkMapping.entrySet()) {
			double cx = e.getKey().x << 4 + 8;
			double cz = e.getKey().x << 4 + 8;

			double dx = cx - pos.getX();
			double dz = cz - pos.getZ();

			double sqrDistance = dx * dx + dz * dz;

			if (sqrDistance < maxSqrDistance) {
				maxSqrDistance = sqrDistance;
			}
		}

		return Math.sqrt(maxSqrDistance);
	}

	@Override
	public boolean contains(double x, double y, double z) {
		int xBlock = (int) Math.floor(x);
		int zBlock = (int) Math.floor(z);

		return get(xBlock, zBlock);
	}

	@Override
	public BlockPos getRandomBlockPos(Random rand) {
		if (chunkMapping.size() == 0) {
			return null;
		}

		int chunkId = rand.nextInt(chunkMapping.size());

		for (Map.Entry<ChunkIndex, ZoneChunk> e : chunkMapping.entrySet()) {
			if (chunkId == 0) {
				BlockPos i = e.getValue().getRandomBlockPos(rand);
				return i.add((e.getKey().x << 4), 0, (e.getKey().z << 4));
			}

			chunkId--;
		}

		return null;
	}

	@Override
	public void readData(ByteBuf stream) {
		chunkMapping.clear();
		int size = stream.readInt();
		for (int i = 0; i < size; i++) {
			ChunkIndex key = new ChunkIndex();
			ZoneChunk value = new ZoneChunk();
			key.readData(stream);
			value.readData(stream);
			chunkMapping.put(key, value);
		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		stream.writeInt(chunkMapping.size());
		for (Map.Entry<ChunkIndex, ZoneChunk> e : chunkMapping.entrySet()) {
			e.getKey().writeData(stream);
			e.getValue().writeData(stream);
		}
	}
}
