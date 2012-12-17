/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.triggers;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.api.transport.IPipe;
import buildcraft.core.DefaultProps;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerPipeSignal extends Trigger implements ITriggerPipe {

	boolean active;
	IPipe.WireColor color;

	public TriggerPipeSignal(int id, boolean active, IPipe.WireColor color) {
		super(id);

		this.active = active;
		this.color = color;
	}

	@Override
	public int getIndexInTexture() {
		if (active) {
			switch (color) {
			case Red:
				return 0 * 16 + 3;
			case Blue:
				return 0 * 16 + 5;
			case Green:
				return 0 * 16 + 7;
			case Yellow:
				return 0 * 16 + 9;
			}
		} else {
			switch (color) {
			case Red:
				return 0 * 16 + 2;
			case Blue:
				return 0 * 16 + 4;
			case Green:
				return 0 * 16 + 6;
			case Yellow:
				return 0 * 16 + 8;
			}
		}

		return 0;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		if (active) {
			switch (color) {
			case Red:
				return "Red Pipe Signal On";
			case Blue:
				return "Blue Pipe Signal On";
			case Green:
				return "Green Pipe Signal On";
			case Yellow:
				return "Yellow Pipe Signal On";
			}
		} else {
			switch (color) {
			case Red:
				return "Red Pipe Signal Off";
			case Blue:
				return "Blue Pipe Signal Off";
			case Green:
				return "Green Pipe Signal Off";
			case Yellow:
				return "Yellow Pipe Signal Off";
			}
		}

		return "";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (active)
			return pipe.signalStrength[color.ordinal()] > 0;
		else
			return pipe.signalStrength[color.ordinal()] == 0;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
