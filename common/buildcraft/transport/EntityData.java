package buildcraft.transport;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipedItem;

public class EntityData {

	// TODO: Move passive data here too, like position, speed and all?
	// TODO: Create an object pool?
	public boolean toCenter = true;
	public IPipedItem item;

	public ForgeDirection input;
	public ForgeDirection output = ForgeDirection.UNKNOWN;

	public EnumSet<ForgeDirection> blacklist = EnumSet.noneOf(ForgeDirection.class);

	public EntityData(IPipedItem citem, ForgeDirection orientation) {
		item = citem;

		this.input = orientation;
	}
}
