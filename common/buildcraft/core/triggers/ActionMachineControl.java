package buildcraft.core.triggers;

import java.util.Locale;


public class ActionMachineControl extends BCAction {

	public enum Mode {
		Unknown, On, Off, Loop
	};

	Mode mode;

	public ActionMachineControl(int legacyId, Mode mode) {
		super(legacyId, "buildcraft.machine." + mode.name().toLowerCase(Locale.ENGLISH));

		this.mode = mode;
	}

	@Override
	public String getDescription() {
		switch (mode) {
		case On:
			return "On";
		case Off:
			return "Off";
		case Loop:
			return "Loop";
		default:
			return "";
		}
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
