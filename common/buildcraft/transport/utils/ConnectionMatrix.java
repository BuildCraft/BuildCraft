/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.utils;

import java.util.Set;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IConnectionMatrix;

import io.netty.buffer.ByteBuf;

public class ConnectionMatrix implements IConnectionMatrix {
    private int mask = 0;
    private boolean dirty = false;

    private Set s;

    public ConnectionMatrix() {
        s = getSet();
    }

    public static Set getSet() {
        return null;
    }

    @Override
    public boolean isConnected(EnumFacing direction) {
        // test if the direction.ordinal()'th bit of mask is set
        return (mask & (1 << direction.ordinal())) != 0;
    }

    public void setConnected(EnumFacing direction, boolean value) {
        if (isConnected(direction) != value) {
            // invert the direction.ordinal()'th bit of mask
            mask ^= 1 << direction.ordinal();
            dirty = true;
        }
    }

    /** Return a mask representing the connectivity for all sides.
     *
     * @return mask in EnumFacing order, least significant bit = first entry */
    public int getMask() {
        return mask;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clean() {
        dirty = false;
    }

    public void writeData(ByteBuf data) {
        data.writeByte(mask);
    }

    public void readData(ByteBuf data) {
        byte newMask = data.readByte();

        if (newMask != mask) {
            mask = newMask;
            dirty = true;
        }
    }
}
