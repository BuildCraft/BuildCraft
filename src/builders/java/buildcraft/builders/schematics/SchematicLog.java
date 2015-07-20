package buildcraft.builders.schematics;

import net.minecraft.block.BlockLog;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicLog extends SchematicBlock {
    @Override
    public void rotateLeft(IBuilderContext context) {
        BlockLog.EnumAxis axis = (BlockLog.EnumAxis) state.getValue(BlockLog.LOG_AXIS);
        BlockLog.EnumAxis newAxis = null;
        switch ((BlockLog.EnumAxis) axis) {
            case X: {
                newAxis = BlockLog.EnumAxis.Z;
                break;
            }
            case Z: {
                newAxis = BlockLog.EnumAxis.X;
                break;
            }
            default:
                return;
        }
        state = state.withProperty(BlockLog.LOG_AXIS, newAxis);
    }
}
