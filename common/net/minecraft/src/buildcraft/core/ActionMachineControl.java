package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.buildcraft.api.Action;

public class ActionMachineControl extends Action {

	public enum Mode {Unknown, On, Off, Loop};

	Mode mode;

	public ActionMachineControl (int id, Mode mode) {
		super (id);

		this.mode = mode;
	}

	@Override
	public int getIndexInTexture () {
		switch (mode) {
		case On:
			return 4 * 16 + 2;
		case Off:
			return 4 * 16 + 3;
		case Loop:
			return 4 * 16 + 4;
		}

		return 0;
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
		}

		return "";
	}

	@Override
	public String getTexture() {
		return BuildCraftCore.triggerTextures;
	}

}
