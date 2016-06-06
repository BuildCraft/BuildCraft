package buildcraft.lib.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.SchematicBlock;
import buildcraft.lib.bpt.vanilla.SchematicAir;
import buildcraft.lib.misc.VecUtil;

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
        BlockPos oldSize = this.size;
        BlockPos newSize = VecUtil.absolute(rotate(oldSize, rotation));
        boolean[][][] newContentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
        BlockPos arrayOffset = newSize.subtract(oldSize);// FIXME: This might be the wrong offset!

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = rotate(original, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = contentBlocks[x][y][z];
                }
            }
        }
        contentBlocks = newContentBlocks;
    }

    @Override
    public SchematicBlock getSchematicAt(BlockPos pos) {
        if (contentBlocks[pos.getX()][pos.getY()][pos.getZ()]) {
            throw new AbstractMethodError("// TODO: Implement this!");
        } else {
            return SchematicAir.INSTANCE;
        }
    }
}
