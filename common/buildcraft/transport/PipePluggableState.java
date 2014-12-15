package buildcraft.transport;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.ISerializable;
import buildcraft.api.pipes.IPipe;
import buildcraft.api.pipes.IPipePluggable;
import buildcraft.api.pipes.PipeManager;
import buildcraft.transport.utils.ConnectionMatrix;

public class PipePluggableState implements ISerializable {
	private IPipePluggable[] pluggables = new IPipePluggable[6];
	private ConnectionMatrix pluggableMatrix = new ConnectionMatrix();

	public PipePluggableState() {

	}

	public IPipePluggable[] getPluggables() {
		return pluggables;
	}

	public void setPluggables(IPipePluggable[] pluggables) {
		this.pluggables = pluggables;
		this.pluggableMatrix.clean();
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			this.pluggableMatrix.setConnected(dir, pluggables[dir.ordinal()] != null);
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		this.pluggableMatrix.writeData(data);
		for (IPipePluggable p : pluggables) {
			if (p != null) {
				data.writeShort(PipeManager.pipePluggables.indexOf(p.getClass()));
				p.writeData(data);
			}
		}
	}

	@Override
	public void readData(ByteBuf data) {
		this.pluggableMatrix.readData(data);
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (this.pluggableMatrix.isConnected(dir)) {
				try {
					IPipePluggable p = PipeManager.pipePluggables.get(data.readUnsignedShort()).newInstance();
					p.readData(data);
					pluggables[dir.ordinal()] = p;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
