/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.properties;

import net.minecraft.block.state.IBlockState;
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

            return property.get(pos.getX() & 0xF, pos.getY(), pos.getZ() & 0xF);
        } else {
            return false;
        }
    }

    private void load(Chunk chunk, ChunkProperty property) {
        synchronized (world) {
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < worldHeight; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        BlockPos pos = new BlockPos(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z);
                        IBlockState state = chunk.getBlockState(pos);

                        boolean prop = worldProperty.get(world, state, pos);
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
        if (world.getChunkProvider().chunkExists(xChunk, zChunk)) {
            long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);

            if (chunkMapping.containsItem(chunkId)) {
                ChunkProperty property = (ChunkProperty) chunkMapping.getValueByKey(chunkId);
                IBlockState state = world.getBlockState(pos);

                boolean prop = worldProperty.get(world, state, pos);
                property.set(pos.getX() & 0xF, pos.getY(), pos.getZ() & 0xF, prop);
            }
        }
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

    @Override
    public void playSound(String var1, double var2, double var4, double var6, float var8, float var9) {}

    @Override
    public void playSoundToNearExcept(EntityPlayer var1, String var2, double var3, double var5, double var7, float var9, float var10) {}

    public void clear() {
        world.removeWorldAccess(this);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {}

    @Override
    public void spawnParticle(int particleID, boolean p_180442_2_, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset,
            double zOffset, int... p_180442_15_) {}

    @Override
    public void onEntityAdded(Entity entityIn) {}

    @Override
    public void onEntityRemoved(Entity entityIn) {}

    @Override
    public void playRecord(String recordName, BlockPos blockPosIn) {}

    @Override
    public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_) {}

    @Override
    public void playAusSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_) {}

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
}
