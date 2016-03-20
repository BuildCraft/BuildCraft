package buildcraft.transport.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.transport.Pipe;

public class PacketPipeSyncRequest extends PacketCoordinates {
	public PacketPipeSyncRequest() {
		super();
	}

	public PacketPipeSyncRequest(TileEntity tile) {
		super(tile);
	}

	@Override
	public void applyData(World world, EntityPlayer player) {
		TileEntity entity = world.getTileEntity(pos);

		if (entity instanceof IPipeTile) {
			Pipe<?> pipe = (Pipe<?>) ((IPipeTile) entity).getPipe();
			if (pipe != null && pipe.transport != null) {
				pipe.transport.synchronizeNetwork(player);
			}
		}
	}
}
