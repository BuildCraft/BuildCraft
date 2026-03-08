package buildcraft.builders.snapshot;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

// Calen
public class FakeChunk extends Chunk {
    final World level;

    public FakeChunk(World world, ChunkPrimer chunkPrimer) {
        super(world, chunkPrimer);
        this.level = world;
    }

    @Override
    public void addBlockEntity(TileEntity te) {
        this.setBlockEntity(te.getBlockPos(), te);
        // Calen: don't call TE#onLoad, or -> IllegalArgumentException: Cannot use model data for a level other than the current client level
    }

    @Override
    public void setBlockEntity(BlockPos blockpos, TileEntity te) {
        if (this.getBlockState(blockpos).hasTileEntity()) {
            te.setLevelAndPosition(this.level, blockpos);
//            p_156374_.clearRemoved();
            TileEntity blockentity = this.blockEntities.put(blockpos.immutable(), te);
            // Calen: setRemoved -> IllegalArgumentException: Cannot use model data for a level other than the current client level
//            if (blockentity != null && blockentity != p_156374_) {
//                blockentity.setRemoved();
//            }
        }
    }
}
