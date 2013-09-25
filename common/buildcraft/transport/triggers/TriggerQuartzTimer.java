package buildcraft.transport.triggers;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerQuartzTimer extends BCTrigger implements ITriggerPipe {
	
	public enum Time {
		FiveSeconds, FifteenSeconds, ThirtySeconds
	}
	
	public Time time;
	public long lastPulseTime;
	public int delay;

	public TriggerQuartzTimer(int legacyId, Time time) {
		super(legacyId, "buildcraft.timer." + time.name().toLowerCase(Locale.ENGLISH));
		
		this.time = time;
	}
	
	@Override
	public int getIconIndex() {
		switch (time) {
			case FiveSeconds:
				return ActionTriggerIconProvider.Trigger_Timer_Five;
			case FifteenSeconds:
				return ActionTriggerIconProvider.Trigger_Timer_Fifteen;
			default:
				return ActionTriggerIconProvider.Trigger_Timer_Thirty;
		}
	}
	
	@Override
	public String getDescription() {
		switch (time) {
			case FiveSeconds:
				return "5 Second Timer";
			case FifteenSeconds:
				return "15 Second Timer";
			default:
				return "30 Second Timer";
		}
	}
	
	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		long worldTime = pipe.getWorld().getWorldTime();
		
		if (time == Time.FiveSeconds) {
			delay = 100;
		} else if (time == Time.FifteenSeconds) {
			delay = 300;
		} else {
			delay = 600;
		}
		
		if (Math.abs(worldTime - lastPulseTime) >= delay) {
			lastPulseTime = worldTime;
			return true;
		} else {
			return false;
		}
	}

}
