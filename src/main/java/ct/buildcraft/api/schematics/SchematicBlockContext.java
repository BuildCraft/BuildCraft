package ct.buildcraft.api.schematics;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SchematicBlockContext {
    @Nonnull
    public final Level world;
    @Nonnull
    public final BlockPos basePos;
    @Nonnull
    public final BlockPos pos;
    @Nonnull
    public final BlockState blockState;
    @Nonnull
    public final Block block;

    public SchematicBlockContext(@Nonnull Level world,
                                 @Nonnull BlockPos basePos,
                                 @Nonnull BlockPos pos,
                                 @Nonnull BlockState blockState,
                                 @Nonnull Block block) {
        this.world = world;
        this.basePos = basePos;
        this.pos = pos;
        this.blockState = blockState;
        this.block = block;
    }
}
