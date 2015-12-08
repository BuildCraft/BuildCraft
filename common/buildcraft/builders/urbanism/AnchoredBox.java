/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.ISerializable;
import buildcraft.core.Box;

import io.netty.buffer.ByteBuf;

public class AnchoredBox implements ISerializable {
    public Box box = new Box();
    public int x1, y1, z1;

    public void setP2(int x2, int y2, int z2) {
        box.initialize(x1, y1, z1, x2, y2, z2);
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("anchorX", x1);
        nbt.setInteger("anchorY", y1);
        nbt.setInteger("anchorZ", z1);

        box.writeToNBT(nbt);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        x1 = nbt.getInteger("anchorX");
        y1 = nbt.getInteger("anchorY");
        z1 = nbt.getInteger("anchorZ");

        box.initialize(nbt);
    }

    @Override
    public void readData(ByteBuf stream) {
        box.readData(stream);
        x1 = stream.readInt();
        y1 = stream.readShort();
        z1 = stream.readInt();
    }

    @Override
    public void writeData(ByteBuf stream) {
        box.writeData(stream);
        stream.writeInt(x1);
        stream.writeShort(y1);
        stream.writeInt(z1);
    }
}
