package buildcraft.builders.schematics;

import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockQuartz.EnumType;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicQuartz extends SchematicBlock {
    @Override
    public void rotateLeft(IBuilderContext context) {
        BlockQuartz.EnumType type = (EnumType) state.getValue(BlockQuartz.VARIANT);
        switch (type) {
            case LINES_X: {
                state = state.withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.LINES_Z);
                return;
            }
            case LINES_Z: {
                state = state.withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.LINES_X);
                return;
            }
            default:
                return;
        }
    }
}
