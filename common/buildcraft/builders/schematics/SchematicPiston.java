package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Created by asie on 3/11/16.
 */
public class SchematicPiston extends SchematicBlock {
    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        context.world().setBlockState(pos, state.withProperty(BlockPistonBase.EXTENDED, false), 3);
    }

    @Override
    public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        state = state.withProperty(BlockPistonBase.EXTENDED, false);

        super.writeSchematicToNBT(nbt, registry);
    }
}
