package buildcraft.api.gates;

import buildcraft.api.transport.IPipe;
import net.minecraftforge.common.util.ForgeDirection;

public interface IGate {
	@Deprecated
	void setPulsing(boolean pulse);
	
	ForgeDirection getSide();

	IPipe getPipe();
}
