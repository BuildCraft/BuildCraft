package buildcraft.builders.snapshot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

// Calen
public class FakeChunk extends LevelChunk {
    final Level level;

    public FakeChunk(Level world, ChunkPos chunkPos) {
        super(world, chunkPos);
        this.level = world;
    }

    @Override
    public void addAndRegisterBlockEntity(BlockEntity p_156391_) {
        this.setBlockEntity(p_156391_);
        // Calen: don't call TE#onLoad, or -> IllegalArgumentException: Cannot use model data for a level other than the current client level
    }

    @Override
    public void setBlockEntity(BlockEntity p_156374_) {
        BlockPos blockpos = p_156374_.getBlockPos();
        if (this.getBlockState(blockpos).hasBlockEntity()) {
            p_156374_.setLevel(this.level);
//            p_156374_.clearRemoved();
            BlockEntity blockentity = this.blockEntities.put(blockpos.immutable(), p_156374_);
            // Calen: setRemoved -> IllegalArgumentException: Cannot use model data for a level other than the current client level
//            if (blockentity != null && blockentity != p_156374_) {
//                blockentity.setRemoved();
//            }
        }
    }
}
