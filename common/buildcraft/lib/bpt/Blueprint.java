/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.bpt.SchematicFactoryWorldBlock;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.bpt.builder.SchematicEntityOffset;
import buildcraft.lib.bpt.vanilla.SchematicAir;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

public class Blueprint extends BlueprintBase {
    /** Stores all of the blocks, using {@link BlueprintBase#min} as the origin. */
    private SchematicBlock[][][] contentBlocks;
    private List<SchematicEntityOffset> contentEntities;

    public Blueprint(NBTTagCompound nbt) {
        super(nbt);
    }

    private Blueprint(BlockPos size) {
        super(size);
        contentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        contentEntities = new ArrayList<>();
    }

    public Blueprint(SchematicBlock[][][] blocks, List<SchematicEntityOffset> entities) {
        super(new BlockPos(blocks.length, blocks[0].length, blocks[0][0].length));
        contentBlocks = blocks;
        if (entities == null) {
            contentEntities = new ArrayList<>();
        } else {
            contentEntities = new ArrayList<>(entities);
        }
    }

    public Blueprint(World world, BlockPos from, BlockPos size) throws SchematicException {
        this(size);
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = from.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(block);
                    if (factory != null) {
                        contentBlocks[x][y][z] = factory.createFromWorld(world, pos);
                    } else {
                        contentBlocks[x][y][z] = SchematicAir.INSTANCE;
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
    protected void rotateContentsBy(Axis axis, Rotation rotation) {
        BlockPos oldSize = this.size;
        BlockPos newSize = VecUtil.absolute(PositionUtil.rotatePos(oldSize, axis, rotation));
        SchematicBlock[][][] newContentBlocks = new SchematicBlock[newSize.getX()][newSize.getY()][newSize.getZ()];
        Box to = new Box(BlockPos.ORIGIN, newSize.add(-1, -1, -1));
        BlockPos newMax = PositionUtil.rotatePos(size.add(-1, -1, -1), axis, rotation);
        BlockPos arrayOffset = to.closestInsideTo(newMax).subtract(newMax);// FIXME: This might be the wrong offset!

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    SchematicBlock schematic = contentBlocks[x][y][z];
                    schematic.rotate(axis, rotation);
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = PositionUtil.rotatePos(original, axis, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = schematic;
                }
            }
        }

        contentBlocks = newContentBlocks;

        for (SchematicEntityOffset schematic : contentEntities) {
            schematic.rotate(axis, rotation, oldSize);
        }
    }

    @Override
    public void mirror(Axis axis) {
        SchematicBlock[][][] newContentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    SchematicBlock schematic = contentBlocks[x][y][z];
                    schematic.mirror(axis);
                    BlockPos mirrored = new BlockPos(x, y, z);
                    int value = Utils.getValue(size, axis) - 1 - Utils.getValue(mirrored, axis);
                    mirrored = Utils.withValue(mirrored, axis, value);
                    newContentBlocks[mirrored.getX()][mirrored.getY()][mirrored.getZ()] = schematic;
                }
            }
        }

        contentBlocks = newContentBlocks;

        for (SchematicEntityOffset schematic : contentEntities) {
            schematic.mirror(axis, size);
        }
    }

    public SchematicBlock getSchematicAt(BlockPos pos) {
        return contentBlocks[pos.getX()][pos.getY()][pos.getZ()];
    }
}
