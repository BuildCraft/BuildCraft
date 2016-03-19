package buildcraft.transport;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.pluggable.IConnectionMatrix;
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.utils.ConnectionMatrix;

import io.netty.buffer.ByteBuf;

public class PipePluggableState implements ISerializable, IPipePluggableState, Comparable<PipePluggableState> {
    private PipePluggable[] pluggables = new PipePluggable[6];
    private final ConnectionMatrix pluggableMatrix = new ConnectionMatrix();
    private boolean isDirty = true;

    public PipePluggableState() {

    }

    public PipePluggable[] getPluggables() {
        return pluggables;
    }

    public void setPluggables(PipePluggable[] pluggables) {
        for (int i = 0; i < 6; i++) {
            if (pluggables[i] == null) {
                if (this.pluggables[i] != null) isDirty = true;
                else continue;
            } else if (this.pluggables[i] == null) {
                isDirty = true;
            } else if (this.pluggables[i].getClass() != pluggables[i].getClass()) {
                isDirty = true;
            } else {
                isDirty = true;
            }
        }
        this.pluggables = pluggables;
    }

    @Override
    public void writeData(ByteBuf data) {
        this.pluggableMatrix.clean();
        for (EnumFacing dir : EnumFacing.VALUES) {
            this.pluggableMatrix.setConnected(dir, pluggables[dir.ordinal()] != null);
        }

        this.pluggableMatrix.writeData(data);

        for (PipePluggable p : pluggables) {
            if (p != null) {
                data.writeShort(PipeManager.pipePluggables.indexOf(p.getClass()));
                p.writeData(data);
            }
        }
    }

    @Override
    public void readData(ByteBuf data) {
        this.pluggableMatrix.readData(data);
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (this.pluggableMatrix.isConnected(dir)) {
                PipePluggable old = pluggables[dir.ordinal()];
                try {
                    Class<? extends PipePluggable> pc = PipeManager.pipePluggables.get(data.readUnsignedShort());
                    if (pluggables[dir.ordinal()] == null || pc != pluggables[dir.ordinal()].getClass()) {
                        PipePluggable p = pc.newInstance();
                        pluggables[dir.ordinal()] = p;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (pluggables[dir.ordinal()] != null) {
                    pluggables[dir.ordinal()].readData(data);
                }
            } else {
                pluggables[dir.ordinal()] = null;
            }
        }
    }

    @Override
    public IConnectionMatrix getPluggableConnections() {
        return pluggableMatrix;
    }

    @Override
    public PipePluggable getPluggable(EnumFacing face) {
        if (face == null) {
            return null;
        }
        return pluggables[face.ordinal()];
    }

    @Override
    public int compareTo(PipePluggableState o) {
        return 0;
    }

    public void clean() {
        isDirty = false;
        pluggableMatrix.clean();
    }

    public boolean isDirty() {
        return isDirty || pluggableMatrix.isDirty();
    }
}
