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

public class TriggerQuartzTimer extends BCTrigger {
	
	public enum Time {
		FiveSeconds, FifteenSeconds, ThirtySeconds
	}
	
	public Time time;

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
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		System.out.println("isTriggerActive");
		long worldTime = tile.worldObj.getWorldTime();
		int divisor;
		
		switch (time) {  // Sets the divisor to the time required in ticks
			case FiveSeconds:
				divisor = 100;
			case FifteenSeconds:
				divisor = 300;
			default:
				divisor = 600;
		}
		
		if (worldTime % divisor == 0) {
			return true;
		} else {
			return false;
		}
		
	}

}
