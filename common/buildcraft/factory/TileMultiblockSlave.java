package buildcraft.factory;

import buildcraft.api.core.Position;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileMultiblockSlave extends TileBuildCraft {

	private static final int SCAN_RADIUS = 10;

	@NetworkData
	protected Position masterPosition;

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

	public boolean onBlockActivated(EntityPlayer player) {
		if (formed) {
			((TileMultiblockMaster) worldObj.getTileEntity((int) masterPosition.x, (int) masterPosition.y, (int) masterPosition.z)).onBlockActivated(player);
			return true;
		} else {
			// Try and find master
			int lastDistance = Integer.MAX_VALUE;
			TileEntity lastTile = null;

			for (int i = -SCAN_RADIUS; i <= SCAN_RADIUS; i++) {
				for (int j = -SCAN_RADIUS; j <= SCAN_RADIUS; j++) {
					for (int k = -SCAN_RADIUS; k <= SCAN_RADIUS; k++) {
						TileEntity tile = worldObj.getTileEntity(xCoord + i, yCoord + j, zCoord + k);

						if (tile != null && (tile instanceof TileMultiblockMaster) && !(((TileMultiblockMaster) tile).formed)) {
							Position thisPos = new Position(this);
							Position thatPos = new Position(tile);

							int distance = (int) Math.abs(thisPos.getDistance(thatPos));

							if (distance < lastDistance) {
								lastDistance = distance;
								lastTile = tile;
							}
						}
					}
				}
			}

			if (lastTile != null) {
				((TileMultiblockMaster) lastTile).onBlockActivated(player);
			}
		}

		return false;
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
