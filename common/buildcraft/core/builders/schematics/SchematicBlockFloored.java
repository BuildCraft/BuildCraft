package buildcraft.core.builders.schematics;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicBlockFloored extends SchematicBlock {
    @Override
    public Set<BlockPos> getPrerequisiteBlocks(IBuilderContext context) {
        return Sets.newHashSet(new BlockPos(0, -1, 0));
    }
}
