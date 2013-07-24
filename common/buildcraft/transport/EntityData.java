package buildcraft.transport;

import buildcraft.core.utils.EnumColor;
import java.util.EnumSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class EntityData {

	// TODO: Move passive data here too, like position, speed and all?
	// TODO: Create an object pool?
	public boolean toCenter = true;
	public final IPipedItem item;
	public EnumColor color;
	public ForgeDirection input = ForgeDirection.UNKNOWN;
	public ForgeDirection output = ForgeDirection.UNKNOWN;
	public EnumSet<ForgeDirection> blacklist = EnumSet.noneOf(ForgeDirection.class);

	public EntityData(IPipedItem item, ForgeDirection orientation) {
		this.item = item;
		this.input = orientation;
	}

	public EntityData(IPipedItem item) {
		this.item = item;
	}

	public void reset() {
		toCenter = true;
		blacklist.clear();
		output = ForgeDirection.UNKNOWN;
	}

	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("toCenter", toCenter);
		data.setInteger("input", input.ordinal());
		data.setInteger("output", output.ordinal());

		data.setByte("color", color != null ? (byte) color.ordinal() : -1);
	}

	public void readFromNBT(NBTTagCompound data) {
		toCenter = data.getBoolean("toCenter");
		input = ForgeDirection.getOrientation(data.getInteger("input"));
		output = ForgeDirection.getOrientation(data.getInteger("output"));

		byte c = data.getByte("color");
		if (c != -1)
			color = EnumColor.fromId(c);
	}
}
