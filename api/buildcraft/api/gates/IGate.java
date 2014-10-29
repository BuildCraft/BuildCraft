package buildcraft.api.gates;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipe;

public interface IGate {
	@Deprecated
	void setPulsing(boolean pulse);
	
	ForgeDirection getSide();

	IPipe getPipe();
}
