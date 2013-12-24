package buildcraft.transport.triggers;

import java.util.Locale;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.IPipeTrigger;
import buildcraft.transport.Pipe;

public class TriggerQuartzTimer extends BCTrigger implements IPipeTrigger {
	
	public enum Time {
		Short, Medium, Long
	}
	
	public Time time;
	public SafeTimeTracker pulseTracker;
	public long delay;

	public TriggerQuartzTimer(int legacyId, Time time) {
		super(legacyId, "buildcraft.timer." + time.name().toLowerCase(Locale.ENGLISH));
		
		this.time = time;
		pulseTracker = new SafeTimeTracker();
	}
	
	@Override
	public int getIconIndex() {
		switch (time) {
			case Short:
				return ActionTriggerIconProvider.Trigger_Timer_Short;
			case Medium:
				return ActionTriggerIconProvider.Trigger_Timer_Medium;
			default:
				return ActionTriggerIconProvider.Trigger_Timer_Long;
		}
	}
	
	@Override
	public String getDescription() {
		switch (time) {
			case Short:
				return BuildCraftSilicon.timerIntervalShort + " Localize Me!";
			case Medium:
				return BuildCraftSilicon.timerIntervalMedium + " Localize Me!";
			default:
				return BuildCraftSilicon.timerIntervalLong + " Localize Me!";
		}
	}
	
	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		
		if (time == Time.Short) {
			delay = BuildCraftSilicon.timerIntervalShort * 20; // Multiply the seconds by 20 to convert to ticks
		} else if (time == Time.Medium) {
			delay = BuildCraftSilicon.timerIntervalMedium * 20;
		} else {
			delay = BuildCraftSilicon.timerIntervalLong * 20;
		}
		
		return pulseTracker.markTimeIfDelay(pipe.getWorld(), delay);
	}

}
