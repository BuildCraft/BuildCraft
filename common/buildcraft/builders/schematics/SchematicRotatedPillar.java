package buildcraft.builders.schematics;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicRotatedPillar extends SchematicBlock {
    @Override
    public void rotateLeft(IBuilderContext context) {
        EnumFacing.Axis axis = (Axis) state.getValue(BlockRotatedPillar.AXIS);
        Comparable<?> newAxis = null;
        switch ((EnumFacing.Axis) axis) {
            case X: {
                newAxis = EnumFacing.Axis.Z;
                break;
            }
            case Z: {
                newAxis = EnumFacing.Axis.X;
                break;
            }
            default:
                return;
        }
        state = state.withProperty(BlockRotatedPillar.AXIS, newAxis);
    }
}
