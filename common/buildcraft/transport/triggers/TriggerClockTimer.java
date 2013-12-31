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

		private Time(int delay) {
			this.delay = delay;
		}
	}
	public final Time time;
	private Icon icon;

	public TriggerClockTimer(Time time) {
		super("buildcraft:timer." + time.name().toLowerCase(Locale.ENGLISH));

		this.time = time;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.timer"), time.delay);
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_timer_" + time.name().toLowerCase(Locale.ENGLISH));
	}
}
