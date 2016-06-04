package buildcraft.lib.bpt;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.*;
import buildcraft.lib.misc.VecUtil;

public class Blueprint extends BlueprintBase {
    /** Stores all of the blocks, using {@link BlueprintBase#min} as the origin. */
    private SchematicBlock[][][] contentBlocks;
    private List<SchematicEntity> contentEntities;// TODO: Store the offsets!

    public Blueprint(NBTTagCompound nbt) {
        super(nbt);
    }

    public Blueprint(BlockPos size, EnumFacing direction) {
        super(size, direction);
        contentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        contentEntities = new ArrayList<>();
    }

    public Blueprint(World world, BlockPos from, BlockPos size, EnumFacing direction) throws SchematicException {
        this(size, direction);
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = from.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldFactoryFor(block);
                    if (factory != null) {
                        contentBlocks[x][y][z] = factory.createFromWorld(world, pos);
                    }
                }
            }
        }
        // TODO: Entities
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList list = new NBTTagList();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    SchematicBlock block = contentBlocks[x][y][z];
                    list.appendTag(block.serializeNBT());
                }
            }
        }
        nbt.setTag("blocks", list);
        return nbt;
    }

    @Override
    protected void rotateContentsBy(Rotation rotation) {
        BlockPos oldSize = this.size;
        BlockPos newSize = VecUtil.absolute(rotate(oldSize, rotation));
        SchematicBlock[][][] newContentBlocks = new SchematicBlock[newSize.getX()][newSize.getY()][newSize.getZ()];
        BlockPos arrayOffset = newSize.subtract(oldSize);// FIXME: This might be the wrong offset!

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    SchematicBlock schematic = contentBlocks[x][y][z];
                    schematic.rotate(rotation);
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = rotate(original, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = schematic;
                }
            }
        }

        for (SchematicEntity schematic : contentEntities) {
            schematic.rotate(rotation);
        }
    }

    @Override
    public List<Iterable<IBptTask>> createTasks(IBuilder builder, BlockPos pos) {
        List<Iterable<IBptTask>> tasks = new ArrayList<>();
        for (SchematicBlock[][] ar2 : contentBlocks) {
            for (SchematicBlock[] ar1 : ar2) {
                for (SchematicBlock schematic : ar1) {
                    if (schematic == null) continue;
                    tasks.add(schematic.createTasks(builder, pos));
                }
            }
        }

        for (SchematicEntity schematic : contentEntities) {
            tasks.add(schematic.createTasks(builder, pos));
        }

        return tasks;
    }
}
