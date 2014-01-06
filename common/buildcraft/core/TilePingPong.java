package buildcraft.core;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import net.minecraft.tileentity.TileEntity;

public class TilePingPong extends TileBuildCraft {

	@RPC
	public void ping (int time, RPCMessageInfo info) {
		System.out.println ("ping " + time);

		RPCHandler.rpcPlayer(this, "pong", info.sender, time);
	}

	@RPC
	public void pong (int time) {
		System.out.println ("pong " + time);
	}

	SafeTimeTracker tracker;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			if (tracker == null) {
				tracker = new SafeTimeTracker();
				tracker.markTimeIfDelay(worldObj, 50);
			}

			if (tracker.markTimeIfDelay(worldObj, 50)) {
				RPCHandler.rpcServer(this, "ping", (int) worldObj.getWorldTime());
			}
		}

	}

}
