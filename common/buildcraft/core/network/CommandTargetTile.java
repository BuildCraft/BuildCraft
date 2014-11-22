package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CommandTargetTile extends CommandTarget {
	@Override
	public Class getHandledClass() {
		return TileEntity.class;
	}

	@Override
	public void write(ByteBuf data, Object target) {
		TileEntity tile = (TileEntity) target;
		data.writeInt(tile.xCoord);
		data.writeShort(tile.yCoord);
		data.writeInt(tile.zCoord);
	}

	@Override
	public ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world) {
		int posX = data.readInt();
		int posY = data.readShort();
		int posZ = data.readInt();
		if (world.blockExists(posX, posY, posZ)) {
			TileEntity tile = world.getTileEntity(posX, posY, posZ);
			if (tile instanceof ICommandReceiver) {
				return (ICommandReceiver) tile;
			}
		}
		return null;
	}
}
