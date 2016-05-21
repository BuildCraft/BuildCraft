/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.schematics;

import java.util.Set;
import com.google.common.collect.ImmutableSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

// A block that is placed on a wall, and requires said wall
public class SchematicWallSide extends SchematicBlock {
    @Override
    public Set<BlockPos> getPrerequisiteBlocks(IBuilderContext context) {
        EnumFacing face = state.getValue(getFacingProp());
        return ImmutableSet.of(new BlockPos(0, 0, 0).offset(face));
    }
}
