package buildcraft.core.network;

import java.util.HashMap;
import java.util.Map;

public class ClassSerializer {

	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> classSerializers = new HashMap<Class, TilePacketWrapper>();


	/*if (!updateWrappers.containsKey(this.getClass())) {
		updateWrappers.put(this.getClass(), new TilePacketWrapper(this.getClass()));
	}

	if (!descriptionWrappers.containsKey(this.getClass())) {
		descriptionWrappers.put(this.getClass(), new TilePacketWrapper(this.getClass()));
	}

	updatePacket = updateWrappers.get(this.getClass());
	descriptionPacket = descriptionWrappers.get(this.getClass());*/

}
