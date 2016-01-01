package buildcraft.transport.utils;

import net.minecraft.util.EnumFacing;

import io.netty.buffer.ByteBuf;

public class TextureMatrix {
    private final int[] iconIndexes = new int[7];
    private boolean dirty = false;

    public int getTextureIndex(EnumFacing direction) {
        return iconIndexes[direction == null ? 6 : direction.ordinal()];
    }

    public void setIconIndex(EnumFacing direction, int value) {
        if (iconIndexes[direction == null ? 6 : direction.ordinal()] != value) {
            iconIndexes[direction == null ? 6 : direction.ordinal()] = value;
            dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clean() {
        dirty = false;
    }

    public void writeData(ByteBuf data) {
        for (int iconIndex : iconIndexes) {
            data.writeByte(iconIndex);
        }
    }

    public void readData(ByteBuf data) {
        for (int i = 0; i < iconIndexes.length; i++) {
            int icon = data.readUnsignedByte();
            if (iconIndexes[i] != icon) {
                iconIndexes[i] = icon;
                dirty = true;
            }
        }
    }
}
