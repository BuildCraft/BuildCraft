/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.pluggable.IConnectionMatrix;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.transport.utils.ConnectionMatrix;
import buildcraft.transport.utils.TextureMatrix;
import buildcraft.transport.utils.WireMatrix;

import io.netty.buffer.ByteBuf;

public class PipeRenderState implements ISerializable, IPipeRenderState, Comparable<PipeRenderState> {
    public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
    public final ConnectionMatrix pipeConnectionExtensions = new ConnectionMatrix();
    public final ConnectionMatrix pipeConnectionBanned = new ConnectionMatrix();
    public final TextureMatrix textureMatrix = new TextureMatrix();
    public final WireMatrix wireMatrix = new WireMatrix();
    boolean glassColorDirty = false;
    private byte glassColor = -127;
    public final float[] customConnections = new float[6];

    private boolean dirty = true;

    public void clean() {
        dirty = false;
        glassColorDirty = false;
        pipeConnectionMatrix.clean();
        textureMatrix.clean();
        wireMatrix.clean();
        pipeConnectionExtensions.clean();
        pipeConnectionBanned.clean();
    }

    public byte getGlassColor() {
        return glassColor;
    }

    public void setGlassColor(byte color) {
        if (this.glassColor != color) {
            this.glassColor = color;
            this.glassColorDirty = true;
        }
    }

    public boolean isDirty() {
        return dirty || pipeConnectionMatrix.isDirty() || pipeConnectionBanned.isDirty() || pipeConnectionExtensions.isDirty() || glassColorDirty
            || textureMatrix.isDirty() || wireMatrix.isDirty();
    }

    public boolean needsRenderUpdate() {
        return pipeConnectionMatrix.isDirty() || pipeConnectionBanned.isDirty() || pipeConnectionExtensions.isDirty() || glassColorDirty
            || textureMatrix.isDirty()
            // remove when wires have been fully pushed to the TESR
            || wireMatrix.isDirty();
    }

    public void setExtension(EnumFacing direction, float extension) {
        if (extension <= -4 / 16f) {
            pipeConnectionBanned.setConnected(direction, false);
            pipeConnectionExtensions.setConnected(direction, false);
            customConnections[direction.ordinal()] = extension;
        } else {
            pipeConnectionBanned.setConnected(direction, true);
            pipeConnectionExtensions.setConnected(direction, extension != 0);
            customConnections[direction.ordinal()] = extension;
        }
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeByte(glassColor < -1 ? -1 : glassColor);
        pipeConnectionMatrix.writeData(data);
        pipeConnectionExtensions.writeData(data);
        pipeConnectionBanned.writeData(data);
        textureMatrix.writeData(data);
        wireMatrix.writeData(data);
        for (int i = 0; i < customConnections.length; i++) {
            float f = customConnections[i];
            if (pipeConnectionExtensions.isConnected(EnumFacing.VALUES[i]) && pipeConnectionBanned.isConnected(EnumFacing.VALUES[i])) {
                data.writeFloat(f);
            }
        }
    }

    @Override
    public void readData(ByteBuf data) {
        byte g = data.readByte();
        if (g != glassColor) {
            this.glassColor = g;
            this.glassColorDirty = true;
        }
        pipeConnectionMatrix.readData(data);
        pipeConnectionExtensions.readData(data);
        pipeConnectionBanned.readData(data);
        textureMatrix.readData(data);
        wireMatrix.readData(data);
        for (int i = 0; i < customConnections.length; i++) {
            EnumFacing face = EnumFacing.VALUES[i];
            if (pipeConnectionExtensions.isConnected(face) && pipeConnectionBanned.isConnected(face)) {
                customConnections[i] = data.readFloat();
            } else {
                customConnections[i] = 0;
            }
        }
    }

    @Override
    public IConnectionMatrix getPipeConnectionMatrix() {
        return pipeConnectionMatrix;
    }

    @Override
    public int compareTo(PipeRenderState o) {
        return 0;
    }
}
