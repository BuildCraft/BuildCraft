/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DimensionProperty implements IWorldAccess {

	private LongHashMap chunkMapping = new LongHashMap();
	private World world;
	private int worldHeight;
	private WorldProperty worldProperty;

	public DimensionProperty(World iWorld, WorldProperty iProp) {
		world = iWorld;
		worldHeight = iWorld.getActualHeight();
		world.addWorldAccess(this);
		worldProperty = iProp;
	}

	public synchronized boolean get(BlockPos pos) {
		int xChunk = pos.getX() >> 4;
		int zChunk = pos.getZ() >> 4;
		long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		ChunkProperty property;

		if (!chunkMapping.containsItem(chunkId)) {
			property = new ChunkProperty(world, world.getHeight(), xChunk, zChunk);
			chunkMapping.add(chunkId, property);
			load(world.getChunkFromChunkCoords(xChunk, zChunk), property);
		} else {
			property = (ChunkProperty) chunkMapping.getValueByKey(chunkId);
		}


		return property.get(pos.getX() & 0xF, pos.getY(), pos.getZ() & 0xF);
	}

	private void load(Chunk chunk, ChunkProperty property) {
		synchronized (world) {
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < worldHeight; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        boolean prop = worldProperty.
                                get(world, chunk.getBlockState(new BlockPos(x, y, z)), new BlockPos(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z));
                        property.set(x, y, z, prop);
                    }
                }
            }
		}
	}

	@Override
	public synchronized void markBlockForUpdate(BlockPos pos) {
		int xChunk = pos.getX() >> 4;
		int zChunk = pos.getZ() >> 4;
		long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);

		if (chunkMapping.containsItem(chunkId)) {
			ChunkProperty property = (ChunkProperty) chunkMapping.getValueByKey(chunkId);

			boolean prop = worldProperty.
					get(world, world.getBlockState(pos), pos);
			property.set(pos.getX() & 0xF, pos.getY(), pos.getZ() & 0xF, prop);
		}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {

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
	public void func_180442_a(int p_180442_1_, boolean p_180442_2_, double p_180442_3_, double p_180442_5_, double p_180442_7_, double p_180442_9_, double p_180442_11_, double p_180442_13_, int... p_180442_15_) {

	}

	@Override
	public void onEntityAdded(Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(Entity entityIn) {

	}

	@Override
	public void func_174961_a(String p_174961_1_, BlockPos p_174961_2_) {

	}

	@Override
	public void func_180440_a(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_) {

	}

	@Override
	public void func_180439_a(EntityPlayer p_180439_1_, int p_180439_2_, BlockPos p_180439_3_, int p_180439_4_) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	public void clear() {
		world.removeWorldAccess(this);
	}

}
