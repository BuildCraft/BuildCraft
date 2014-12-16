package buildcraft.api.pipes;

import net.minecraftforge.common.util.ForgeDirection;

public interface IPipePluggableContainer {
	PipePluggable getPipePluggable(ForgeDirection direction);
	boolean hasPipePluggable(ForgeDirection direction);
}
