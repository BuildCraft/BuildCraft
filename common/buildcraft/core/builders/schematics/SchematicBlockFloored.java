package buildcraft.core.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.BlockPos;

import java.util.Set;

public class SchematicBlockFloored extends SchematicBlock {
    @Override
    public Set<BlockPos> getPrerequisiteBlocks(IBuilderContext context) {
        return ImmutableSet.of(new BlockPos(0, -1, 0));
    }
}
