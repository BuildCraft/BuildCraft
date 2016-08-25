package buildcraft.lib.bpt.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IMaterialProvider;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;

public class SchematicBlockJSON extends SchematicBlock {
    // FIXME: More elements!
    
    public SchematicBlockJSON(IBlockState state) {
        super(state);
        // TODO Auto-generated constructor stub
    }

    public SchematicBlockJSON(NBTTagCompound nbt, BlockPos offset) throws SchematicException {
        super(nbt, offset);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Iterable<IBptTask> createTasks(IMaterialProvider builder) {
        return null;
    }
}
