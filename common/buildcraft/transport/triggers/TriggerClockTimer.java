package buildcraft.transport.triggers;

import java.util.Locale;

import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class TriggerClockTimer extends BCTrigger {

	public enum Time {

		SHORT(5), MEDIUM(10), LONG(15);
		public static final Time[] VALUES = values();
		public final int delay;
		private Icon icon;

		private Time(int delay) {
			this.delay = delay;
		}
	}
	public final Time time;

	public TriggerClockTimer(Time time) {
		super(-1, "buildcraft.timer." + time.name().toLowerCase(Locale.ENGLISH));

		this.time = time;
	}

	@Override
	public Icon getIcon() {
		return time.icon;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.timer"), time.delay);
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		Time.SHORT.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_timer_short");
		Time.MEDIUM.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_timer_medium");
		Time.LONG.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_timer_long");
	}
}
