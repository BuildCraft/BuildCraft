package buildcraft.transport;

import io.netty.buffer.ByteBuf;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.utils.ConnectionMatrix;

public class PipePluggableState implements ISerializable {
    private PipePluggable[] pluggables = new PipePluggable[6];
    private final ConnectionMatrix pluggableMatrix = new ConnectionMatrix();

    public PipePluggableState() {

    }

    public PipePluggable[] getPluggables() {
        return pluggables;
    }

    public void setPluggables(PipePluggable[] pluggables) {
        this.pluggables = pluggables;
        this.pluggableMatrix.clean();
        for (EnumFacing dir : EnumFacing.VALID_DIRECTIONS) {
            this.pluggableMatrix.setConnected(dir, pluggables[dir.ordinal()] != null);
        }
    }

    @Override
    public void writeData(ByteBuf data) {
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
        for (EnumFacing dir : EnumFacing.VALID_DIRECTIONS) {
            if (this.pluggableMatrix.isConnected(dir)) {
                try {
                    PipePluggable p = PipeManager.pipePluggables.get(data.readUnsignedShort()).newInstance();
                    p.readData(data);
                    pluggables[dir.ordinal()] = p;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                pluggables[dir.ordinal()] = null;
            }
        }
    }
}
