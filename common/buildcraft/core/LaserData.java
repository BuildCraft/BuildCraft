/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.NetworkUtils;

import io.netty.buffer.ByteBuf;

public class LaserData implements ISerializable {
    public Vec3 head = new Vec3(0, 0, 0);
    public Vec3 tail = new Vec3(0, 0, 0);
    public boolean isVisible = true;
    public boolean isGlowing = false;

    public double renderSize = 1.0 / 16.0;
    public double angleY = 0;
    public double angleZ = 0;

    public double wavePosition = 0;
    public int laserTexAnimation = 0;

    // Size of the wave, from 0 to 1
    public float waveSize = 1F;

    public LaserData() {

    }

    public LaserData(Vec3 tail, Vec3 head) {
        this.tail = tail;
        this.head = head;
    }

    public void update() {
        Vec3 delta = head.subtract(tail);
        double dx = delta.xCoord;
        double dy = delta.yCoord;
        double dz = delta.zCoord;

        renderSize = Math.sqrt(dx * dx + dy * dy + dz * dz);
        angleZ = 360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0);
        dx = Math.sqrt(renderSize * renderSize - dy * dy);
        angleY = -Math.atan2(dy, dx) * 180.0 / Math.PI;
    }

    public void iterateTexture() {
        laserTexAnimation = (laserTexAnimation + 1) % 40;
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("head", NBTUtils.writeVec3(head));
        nbt.setTag("tail", NBTUtils.writeVec3(tail));
        nbt.setBoolean("isVisible", isVisible);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        head = NBTUtils.readVec3(nbt, "head");
        tail = NBTUtils.readVec3(nbt, "tail");
        isVisible = nbt.getBoolean("isVisible");
    }

    @Override
    public void readData(ByteBuf stream) {
        head = NetworkUtils.readVec3(stream);
        tail = NetworkUtils.readVec3(stream);
        int flags = stream.readUnsignedByte();
        isVisible = (flags & 1) != 0;
        isGlowing = (flags & 2) != 0;
    }

    @Override
    public void writeData(ByteBuf stream) {
        NetworkUtils.writeVec3(stream, head);
        NetworkUtils.writeVec3(stream, tail);
        int flags = (isVisible ? 1 : 0) | (isGlowing ? 2 : 0);
        stream.writeByte(flags);
    }
}
