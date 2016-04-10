package buildcraft.core.lib.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.properties.BuildCraftProperties;

public class FakeChunkProvider implements IChunkProvider {
    private Map<ChunkCoordIntPair, Chunk> chunks = Maps.newHashMap();
    private final FakeWorld world;
    private final EnumDecoratedBlock type;

    public FakeChunkProvider(FakeWorld world, EnumDecoratedBlock decor) {
        this.world = world;
        this.type = decor;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return chunks.containsKey(new ChunkCoordIntPair(x, z));
    }

    @Override
    public Chunk provideChunk(int chunkXPos, int chunkZPos) {
        ChunkCoordIntPair ccip = new ChunkCoordIntPair(chunkXPos, chunkZPos);
        if (!chunks.containsKey(ccip)) {
            IBlockState state = BuildCraftCore.decoratedBlock.getDefaultState();
            state = state.withProperty(BuildCraftProperties.DECORATED_BLOCK, type);
            ChunkPrimer primer = new ChunkPrimer();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    primer.setBlockState(x, 0, z, state);
                }
            }
            Chunk chunk = new Chunk(world, primer, chunkXPos, chunkZPos);
            for (int i = 0; i < chunk.getBiomeArray().length; i++) {
                chunk.getBiomeArray()[i] = 0;
            }
            chunk.generateSkylightMap();
            chunks.put(ccip, chunk);
            BCLog.logger.info("Created a new chunk @ " + ccip);
        }
        return chunks.get(ccip);
    }

    @Override
    public Chunk provideChunk(BlockPos pos) {
        return provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {}

    @Override
    public boolean populateChunk(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
        return false;
    }

    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public String makeString() {
        return "";
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return chunks.size();
    }

    @Override
    public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_) {}

    @Override
    public void saveExtraData() {}
}
