/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DimensionProperty implements IWorldAccess {

	private final LongHashMap chunkMapping = new LongHashMap();
	private final World world;
	private final int worldHeight;
	private final WorldProperty worldProperty;

	public DimensionProperty(World iWorld, WorldProperty iProp) {
		world = iWorld;
		worldHeight = iWorld.getHeight();
		world.addWorldAccess(this);
		worldProperty = iProp;
	}

	public synchronized boolean get(int x, int y, int z) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;

		if (world.getChunkProvider().chunkExists(xChunk, zChunk)) {
			long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
			ChunkProperty property;
			if (!chunkMapping.containsItem(chunkId)) {
				property = new ChunkProperty(world, world.getHeight(), xChunk, zChunk);
				chunkMapping.add(chunkId, property);
				load(world.getChunkFromChunkCoords(xChunk, zChunk), property);
			} else {
				property = (ChunkProperty) chunkMapping.getValueByKey(chunkId);
			}

			return property.get(x & 0xF, y, z & 0xF);
		} else {
			return false;
		}
	}

	private void load(Chunk chunk, ChunkProperty property) {
		synchronized (world) {
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < worldHeight; ++y) {
					for (int z = 0; z < 16; ++z) {
						Block block = chunk.getBlock(x, y, z);
						int meta = chunk.getBlockMetadata(x, y, z);

						boolean prop = worldProperty.
								get(world, block, meta, chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z);
						property.set(x, y, z, prop);
					}
				}
			}
		}
	}

	@Override
	public synchronized void markBlockForUpdate(int x, int y, int z) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		if (world.getChunkProvider().chunkExists(xChunk, zChunk)) {
			long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);

			if (chunkMapping.containsItem(chunkId)) {
				ChunkProperty property = (ChunkProperty) chunkMapping.getValueByKey(chunkId);

				Block block = world.getBlock(x, y, z);
				int meta = world.getBlockMetadata(x, y, z);
				boolean prop = worldProperty.
						get(world, block, meta, x, y, z);
				property.set(x & 0xF, y, z & 0xF, prop);
			}
		}
	}

	@Override
	public void markBlockForRenderUpdate(int var1, int var2, int var3) {
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
	}

	@Override
	public void playSound(String var1, double var2, double var4, double var6, float var8, float var9) {
	}

	@Override
	public void playSoundToNearExcept(EntityPlayer var1, String var2, double var3, double var5, double var7,
									  float var9, float var10) {
	}

	@Override
	public void spawnParticle(String var1, double var2, double var4, double var6, double var8, double var10,
							  double var12) {
	}

	@Override
	public void onEntityCreate(Entity var1) {
	}

	@Override
	public void onEntityDestroy(Entity var1) {
	}

	@Override
	public void playRecord(String var1, int var2, int var3, int var4) {
	}

	@Override
	public void broadcastSound(int var1, int var2, int var3, int var4, int var5) {
	}

	@Override
	public void playAuxSFX(EntityPlayer var1, int var2, int var3, int var4, int var5, int var6) {
	}

	@Override
	public void destroyBlockPartially(int var1, int var2, int var3, int var4, int var5) {
	}

	@Override
	public void onStaticEntitiesChanged() {
	}

	public void clear() {
		world.removeWorldAccess(this);
	}

}
