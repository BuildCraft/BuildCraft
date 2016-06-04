package buildcraft.lib.bpt;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;

public class Template extends BlueprintBase {
    /** Stores all of the blocks, using {@link BlueprintBase#min} as the origin. */
    private boolean[][][] contentBlocks;

    public Template(BlockPos size, EnumFacing direction) {
        super(size, direction);
        contentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
    }

    public Template(NBTTagCompound nbt) {
        super(nbt);

    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();

        return nbt;
    }

    @Override
    protected void rotateContentsBy(Rotation rotation) {
        BlockPos oldMax = max.subtract(min);
        BlockPos newMax = rotate(oldMax, rotation);
        BlockPos size = newMax.add(1, 1, 1);
        boolean[][][] newContentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
        BlockPos arrayOffset = newMax.subtract(oldMax);

        for (int x = 0; x < contentBlocks.length; x++) {
            boolean[][] inXLayer = contentBlocks[x];
            for (int y = 0; y < inXLayer.length; y++) {
                boolean[] inYLayer = inXLayer[y];
                for (int z = 0; z < inYLayer.length; z++) {
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = rotate(original, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = inYLayer[z];
                }
            }
        }
    }

    @Override
    public List<Iterable<IBptTask>> createTasks(IBuilder builder, BlockPos pos) {
        return ImmutableList.of();
    }
}
