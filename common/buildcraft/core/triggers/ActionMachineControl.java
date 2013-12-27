package buildcraft.core.triggers;

import buildcraft.core.utils.StringUtils;
import java.util.Locale;

public class ActionMachineControl extends BCAction {

	public enum Mode {

		Unknown, On, Off, Loop
	};
	public final Mode mode;

	public ActionMachineControl(Mode mode) {
		super("buildcraft:machine." + mode.name().toLowerCase(Locale.ENGLISH), "buildcraft.machine." + mode.name().toLowerCase(Locale.ENGLISH));

		this.mode = mode;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public int getIconIndex() {
		switch (mode) {
			case On:
				return ActionTriggerIconProvider.Action_MachineControl_On;
			case Off:
				return ActionTriggerIconProvider.Action_MachineControl_Off;
			case Loop:
			default:
				return ActionTriggerIconProvider.Action_MachineControl_Loop;
		}
	}
}
