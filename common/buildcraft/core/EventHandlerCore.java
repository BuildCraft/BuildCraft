package buildcraft.core;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

public class EventHandlerCore {
	@ForgeSubscribe
	public void handleEntityCanUpdate(EntityEvent.CanUpdate evt) {
		if(!evt.canUpdate && evt.entity instanceof EntityRobot)
			evt.canUpdate = ((EntityRobot)evt.entity).alwaysUpdate;
	}
}
