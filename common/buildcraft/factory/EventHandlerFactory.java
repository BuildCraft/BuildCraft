package buildcraft.factory;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

public class EventHandlerFactory {
	@ForgeSubscribe
	public void handleEntityCanUpdate(EntityEvent.CanUpdate evt) {
		if(evt.entity instanceof EntityMechanicalArm)
			evt.canUpdate = true;
	}
}
