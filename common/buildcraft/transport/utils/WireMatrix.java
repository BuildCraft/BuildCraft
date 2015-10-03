/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.utils;

import java.util.BitSet;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeWire;

import io.netty.buffer.ByteBuf;

public class WireMatrix {

    // private final boolean[] _hasWire = new boolean[IPipe.WireColor.values().length];
    private final BitSet hasWire = new BitSet(PipeWire.values().length);
    private final BitSetCodec bitSetCodec = new BitSetCodec();

    private final ConnectionMatrix[] wires = new ConnectionMatrix[PipeWire.values().length];
    private final BitSet lit = new BitSet(PipeWire.values().length);

    private boolean dirty = false;

    public WireMatrix() {
        for (int i = 0; i < PipeWire.values().length; i++) {
            wires[i] = new ConnectionMatrix();
        }
    }

    public boolean hasWire(PipeWire color) {
        return hasWire.get(color.ordinal());
    }

    public void setWire(PipeWire color, boolean value) {
        if (hasWire.get(color.ordinal()) != value) {
            hasWire.set(color.ordinal(), value);
            dirty = true;
        }
    }

    public boolean isWireLit(PipeWire wire) {
        return lit.get(wire.ordinal());
    }

    public void setWireLit(PipeWire wire, boolean lit) {
        if (this.lit.get(wire.ordinal()) != lit) {
            this.lit.set(wire.ordinal(), lit);
            dirty = true;
        }
    }

    public boolean isWireConnected(PipeWire color, EnumFacing direction) {
        return wires[color.ordinal()].isConnected(direction);
    }

    public void setWireConnected(PipeWire color, EnumFacing direction, boolean value) {
        wires[color.ordinal()].setConnected(direction, value);
    }

    public boolean isDirty() {

        for (int i = 0; i < PipeWire.values().length; i++) {
            if (wires[i].isDirty()) {
                return true;
            }
        }

        return dirty;
    }

    public void clean() {
        for (int i = 0; i < PipeWire.values().length; i++) {
            wires[i].clean();
        }
        dirty = false;
    }

    public void writeData(ByteBuf data) {
        data.writeByte(bitSetCodec.encode(hasWire));
        data.writeByte(bitSetCodec.encode(lit));

        for (int i = 0; i < PipeWire.values().length; i++) {
            wires[i].writeData(data);
        }
    }

    public void readData(ByteBuf data) {
        bitSetCodec.decode(data.readByte(), hasWire);
        bitSetCodec.decode(data.readByte(), lit);

        for (int i = 0; i < PipeWire.values().length; i++) {
            wires[i].readData(data);
        }
    }
}
