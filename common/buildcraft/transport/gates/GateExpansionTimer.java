/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.triggers.TriggerClockTimer;
import buildcraft.transport.triggers.TriggerClockTimer.Time;

public final class GateExpansionTimer extends GateExpansionBuildcraft implements IGateExpansion {

	public static GateExpansionTimer INSTANCE = new GateExpansionTimer();

	private GateExpansionTimer() {
		super("timer");
	}

	@Override
	public GateExpansionController makeController(TileEntity pipeTile) {
		return new GateExpansionControllerTimer(pipeTile);
	}

	private class GateExpansionControllerTimer extends GateExpansionController {

		private class Timer {

			private static final int ACTIVE_TIME = 5;
			private final TriggerClockTimer.Time time;
			private int clock;

			public Timer(TriggerClockTimer.Time time) {
				this.time = time;
			}

			public void tick() {
				if (clock > -ACTIVE_TIME) {
					clock--;
				} else {
					clock = time.delay * 20 + ACTIVE_TIME;
				}
			}

			public boolean isActive() {
				return clock < 0;
			}
		}
		private final Timer[] timers = new Timer[TriggerClockTimer.Time.VALUES.length];

		public GateExpansionControllerTimer(TileEntity pipeTile) {
			super(GateExpansionTimer.this, pipeTile);
			for (TriggerClockTimer.Time time : TriggerClockTimer.Time.VALUES) {
				timers[time.ordinal()] = new Timer(time);
			}
		}

		@Override
		public boolean isTriggerActive(ITrigger trigger, ITriggerParameter parameter) {
			if (trigger instanceof TriggerClockTimer) {
				TriggerClockTimer timerTrigger = (TriggerClockTimer) trigger;
				return timers[timerTrigger.time.ordinal()].isActive();
			}
			return super.isTriggerActive(trigger, parameter);
		}

		@Override
		public void addTriggers(List<ITrigger> list) {
			super.addTriggers(list);
			for (Time time : TriggerClockTimer.Time.VALUES) {
				list.add(BuildCraftTransport.triggerTimer[time.ordinal()]);
			}
		}

		@Override
		public void tick() {
			for (Timer timer : timers) {
				timer.tick();
			}
		}
	}
}
