/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.buttons;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

/**
 * T should be an Enum of button states
 */
public final class MultiButtonController<T extends IMultiButtonState> {

	private int currentState;
	private final T[] validStates;

	private MultiButtonController(int startState, T... validStates) {
		this.currentState = startState;
		this.validStates = validStates;
	}

	public static <T extends IMultiButtonState> MultiButtonController<T> getController(int startState, T... validStates) {
		return new MultiButtonController<T>(startState, validStates);
	}

	public MultiButtonController<?> copy() {
		return new MultiButtonController<T>(currentState, validStates.clone());
	}

	public T[] getValidStates() {
		return validStates;
	}

	public int incrementState() {
		int newState = currentState + 1;
		if (newState >= validStates.length) {
			newState = 0;
		}
		currentState = newState;
		return currentState;
	}

	public void setCurrentState(int state) {
		currentState = state;
	}

	public void setCurrentState(T state) {
		for (int i = 0; i < validStates.length; i++) {
			if (validStates[i] == state) {
				currentState = i;
				return;
			}
		}
	}

	public int getCurrentState() {
		return currentState;
	}

	public T getButtonState() {
		return validStates[currentState];
	}

	public void writeToNBT(NBTTagCompound nbt, String tag) {
		nbt.setString(tag, getButtonState().name());
	}

	public void readFromNBT(NBTTagCompound nbt, String tag) {
		if (nbt.getTag(tag) instanceof NBTTagString) {
			String name = nbt.getString(tag);
			for (int i = 0; i < validStates.length; i++) {
				if (validStates[i].name().equals(name)) {
					currentState = i;
					break;
				}
			}
		} else if (nbt.getTag(tag) instanceof NBTTagByte) {
			currentState = nbt.getByte(tag);
		}
	}
}
