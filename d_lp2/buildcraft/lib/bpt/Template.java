package buildcraft.lib.bpt;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.Schematic;

public class Template extends BlueprintBase {
    /** Stores all of the blocks, using {@link BlueprintBase#min} as the origin. */
    private boolean[][][] contentBlocks;

    public Template(NBTTagCompound nbt) {
        super(nbt);
        deserializeNBT(nbt);
    }

    public Template(BlockPos anchor, BlockPos min, BlockPos max, EnumFacing direction) {
        super(anchor, min, max, direction);
        BlockPos size = max.subtract(min).add(1, 1, 1);
        contentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
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
    protected void mirrorContents(Mirror mirror) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");

    }

    @Override
    protected void translateContentsBy(Vec3i by) {
        // NO-OP
    }

    @Override
    public Map<Schematic, Iterable<IBptTask>> createTasks(IBuilder builder) {
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();

        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        
    }
}
