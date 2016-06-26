/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.lib.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.VecUtil;

public class Template extends BlueprintBase {
    /** Stores all of the blocks, using {@link BlueprintBase#min} as the origin. */
    private boolean[][][] contentBlocks;

    private Template(BlockPos size) {
        super(size);
        contentBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
    }

    public Template(boolean[][][] blocks) {
        super(new BlockPos(blocks.length, blocks[0].length, blocks[0][0].length));
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
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");

    }

    @Override
    public void mirror(Axis axis) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");

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

    public boolean getIsSolidAt(BlockPos pos) {
        return contentBlocks[pos.getX()][pos.getY()][pos.getZ()];
    }
}
