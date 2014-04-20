package buildcraft.factory;

import buildcraft.api.core.Position;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileMultiblockSlave extends TileBuildCraft {

	private Position masterPosition;

	@NetworkData
	public boolean formed = false;

	public void setMaster(Position position) {
		this.masterPosition = position;
		this.masterPosition.orientation = ForgeDirection.UNKNOWN;
		this.formed = true;

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void clear() {
		this.masterPosition = null;
		this.formed = false;

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void onBlockActivated(EntityPlayer player) {
		if (formed) {
			((TileMultiblockMaster) worldObj.getTileEntity((int) masterPosition.x, (int) masterPosition.y, (int) masterPosition.z)).onBlockActivated(player);
		}
	}

	public void deformMultiblock() {
		if (formed) {
			((TileMultiblockMaster) worldObj.getTileEntity((int) masterPosition.x, (int) masterPosition.y, (int) masterPosition.z)).deformMultiblock();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if (nbt.hasKey("position")) {
			masterPosition = new Position();
			masterPosition.readFromNBT(nbt.getCompoundTag("position"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if (masterPosition != null) {
			NBTTagCompound tag = new NBTTagCompound();
			masterPosition.writeToNBT(tag);
			nbt.setTag("position", tag);
		}
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		super.postPacketHandling(packet);

		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}
}
