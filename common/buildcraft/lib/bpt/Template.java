/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public class Template extends BlueprintBase {
    private boolean[][][] contentBlocks;

    private Template(BlockPos size, BlockPos offset) {
        super(size, offset);
        contentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
    }

    public Template(boolean[][][] blocks, BlockPos offset) {
        super(new BlockPos(blocks.length, blocks[0].length, blocks[0][0].length), offset);
        contentBlocks = blocks;
    }

    public Template(NBTTagCompound nbt) {
        super(nbt);
        contentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
        byte[] packedData = nbt.getByteArray("data");
        int arrayIndex = 0;
        int flagIndex = 0;
        for (boolean[][] ar1 : contentBlocks) {
            for (boolean[] ar2 : ar1) {
                for (int i = 0; i < ar2.length; i++) {
                    byte c = packedData[arrayIndex];
                    int val = c >> flagIndex;
                    if ((val & 1) == 1) {
                        ar2[i] = true;
                    }
                    flagIndex++;
                    if (flagIndex == 8) {
                        arrayIndex++;
                        flagIndex = 0;
                    }
                }
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        int booleans = size.getX() * size.getY() * size.getZ();
        int numBytes = booleans / 8;
        if (numBytes * 8 < booleans) {
            numBytes++;
        }
        byte[] packedData = new byte[numBytes];
        int arrayIndex = 0;
        int flagIndex = 0;
        for (boolean[][] ar1 : contentBlocks) {
            for (boolean[] ar2 : ar1) {
                for (boolean b : ar2) {
                    if (b) {
                        packedData[arrayIndex] |= 1 << flagIndex;
                    }
                    flagIndex++;
                    if (flagIndex == 8) {
                        arrayIndex++;
                        flagIndex = 0;
                    }
                }
            }
        }

        nbt.setByteArray("data", packedData);
        return nbt;
    }

    @Override
    protected void rotateContentsBy(Axis axis, Rotation rotation) {
        BlockPos oldSize = this.size;
        BlockPos newSize = VecUtil.absolute(PositionUtil.rotatePos(oldSize, axis, rotation));
        boolean[][][] newContentBlocks = new boolean[newSize.getX()][newSize.getY()][newSize.getZ()];
        Box to = new Box(BlockPos.ORIGIN, newSize.add(-1, -1, -1));
        BlockPos newMax = PositionUtil.rotatePos(size.add(-1, -1, -1), axis, rotation);
        BlockPos arrayOffset = to.closestInsideTo(newMax).subtract(newMax);

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    boolean from = contentBlocks[x][y][z];
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = PositionUtil.rotatePos(original, axis, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = from;
                }
            }
        }

        contentBlocks = newContentBlocks;
    }

    @Override
    public void mirror(Axis axis) {
        boolean[][][] newContentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    boolean from = contentBlocks[x][y][z];
                    BlockPos mirrored = new BlockPos(x, y, z);
                    int value = VecUtil.getValue(size, axis) - 1 - VecUtil.getValue(mirrored, axis);
                    mirrored = VecUtil.replaceValue(mirrored, axis, value);
                    newContentBlocks[mirrored.getX()][mirrored.getY()][mirrored.getZ()] = from;
                }
            }
        }

        contentBlocks = newContentBlocks;
    }

    public boolean isSolidAt(BlockPos pos) {
        return contentBlocks[pos.getX()][pos.getY()][pos.getZ()];
    }
}
