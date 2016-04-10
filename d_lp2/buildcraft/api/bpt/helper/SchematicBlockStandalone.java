package buildcraft.api.bpt.helper;

import java.util.Collections;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.SchematicBlock;

public class SchematicBlockStandalone extends SchematicBlock {
    private IBlockState state;

    @Override
    public void rotate(Rotation rotation) {
        state = state.withRotation(rotation);
    }

    @Override
    public void mirror(Mirror mirror) {
        state = state.withMirror(mirror);
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilder builder, BuildStage stage) {
        if (stage == BuildStage.BEFORE) {
            ImmutableSet.of(BptTaskBlockClear.create(builder, offset));
        } else if (stage == BuildStage.STANDALONE) {
            ImmutableSet.of(BptTaskBlockStandalone.create(builder, offset, state));
        }
        return Collections.emptyList();
    }

    @Override
    public void onTaskComplete(IBuilder builder, BuildStage stage, IBptTask task) {}
}
