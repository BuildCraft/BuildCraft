package buildcraft.builders.schematics;

import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicVine extends SchematicBlock {
    @Override
    public void rotateLeft(IBuilderContext context) {
        // Dammit vine, you just had to be different...
        IBlockState newState = state;
        for (EnumFacing oldFace : EnumFacing.HORIZONTALS) {
            PropertyBool oldProp = BlockVine.getPropertyFor(oldFace);
            EnumFacing newFace = oldFace.rotateY();
            PropertyBool newProp = BlockVine.getPropertyFor(newFace);
            newState = newState.withProperty(newProp, state.getValue(oldProp));
        }
        state = newState;
    }
}
