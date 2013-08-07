package buildcraft.core;

import buildcraft.api.core.SafeTimeTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import java.util.EnumSet;
import net.minecraft.world.World;

public class TickHandlerTimeTracker implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		SafeTimeTracker.worldTime = ((World) tickData[0]).getWorldTime();
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "BuildCraft - World update tick";
	}
}
