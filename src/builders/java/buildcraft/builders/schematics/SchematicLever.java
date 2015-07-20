package buildcraft.builders.schematics;

import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicLever extends SchematicBlock {
    @Override
    public void rotateLeft(IBuilderContext context) {
        BlockLever.EnumOrientation orientation = (EnumOrientation) state.getValue(BlockLever.FACING);
        BlockLever.EnumOrientation newOrientation = null;
        switch (orientation) {
            case DOWN_X: {
                newOrientation = BlockLever.EnumOrientation.DOWN_Z;
                break;
            }
            case DOWN_Z: {
                newOrientation = BlockLever.EnumOrientation.DOWN_X;
                break;
            }
            case EAST: {
                newOrientation = BlockLever.EnumOrientation.SOUTH;
                break;
            }
            case NORTH: {
                newOrientation = BlockLever.EnumOrientation.EAST;
                break;
            }
            case SOUTH: {
                newOrientation = BlockLever.EnumOrientation.WEST;
                break;
            }
            case UP_X: {
                newOrientation = BlockLever.EnumOrientation.UP_Z;
                break;
            }
            case UP_Z: {
                newOrientation = BlockLever.EnumOrientation.UP_X;
                break;
            }
            case WEST: {
                newOrientation = BlockLever.EnumOrientation.NORTH;
                break;
            }
        }
        state = state.withProperty(BlockLever.FACING, newOrientation);
    }
}
