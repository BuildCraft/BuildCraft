package buildcraft.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileMultiblockValve extends TileMultiblockSlave {

	private ForgeDirection orientation = ForgeDirection.UNKNOWN;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		orientation = ForgeDirection.getOrientation(nbt.getByte("orientation"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setByte("orientation", (byte) orientation.ordinal());
	}

}
