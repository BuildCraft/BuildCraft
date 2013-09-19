package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * 
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class PacketGuiReturn extends BuildCraftPacket {
	private EntityPlayer sender;
	private IGuiReturnHandler obj;
	private byte[] extraData;

	public PacketGuiReturn(EntityPlayer sender) {
		this.sender = sender;
	}

	public PacketGuiReturn(IGuiReturnHandler obj) {
		this.obj = obj;
		this.extraData = null;
	}

	public PacketGuiReturn(IGuiReturnHandler obj, byte[] extraData) {
		this.obj = obj;
		this.extraData = extraData;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(obj.getWorld().provider.dimensionId);
		if (obj instanceof TileEntity) {
			TileEntity tile = (TileEntity) obj;
			data.writeBoolean(true);
			data.writeInt(tile.xCoord);
			data.writeInt(tile.yCoord);
			data.writeInt(tile.zCoord);
		} else if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
			data.writeBoolean(false);
			data.writeInt(entity.entityId);
		} else
			return;
		obj.writeGuiData(data);
		if (extraData != null)
			data.write(extraData);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		int dim = data.readInt();
		World world = DimensionManager.getWorld(dim);
		boolean tileReturn = data.readBoolean();
		if (tileReturn) {
			int x = data.readInt();
			int y = data.readInt();
			int z = data.readInt();

			TileEntity t = world.getBlockTileEntity(x, y, z);

			if (t instanceof IGuiReturnHandler)
				((IGuiReturnHandler) t).readGuiData(data, sender);

		} else {
			int entityId = data.readInt();
			Entity entity = world.getEntityByID(entityId);

			if (entity instanceof IGuiReturnHandler)
				((IGuiReturnHandler) entity).readGuiData(data, sender);
		}
	}

	public void sendPacket() {
		PacketDispatcher.sendPacketToServer(getPacket());
	}

	@Override
	public int getID() {
		return PacketIds.GUI_RETURN;
	}
}