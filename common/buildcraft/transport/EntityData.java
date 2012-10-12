package buildcraft.transport;

import buildcraft.api.core.Orientations;
import buildcraft.api.transport.IPipedItem;
import java.util.EnumSet;

public class EntityData {

	// TODO: Move passive data here too, like position, speed and all?
	// TODO: Create an object pool?
	public boolean toCenter = true;
	public IPipedItem item;

	public Orientations input;
	public Orientations output = Orientations.Unknown;

	public EnumSet<Orientations> blacklist = EnumSet.noneOf(Orientations.class);

	public EntityData(IPipedItem citem, Orientations orientation) {
		item = citem;

		this.input = orientation;
	}
}