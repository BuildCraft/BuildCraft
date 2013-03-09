/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.triggers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.api.transport.IPipe;
import buildcraft.core.DefaultProps;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.IconItemConstants;
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
	@SideOnly(Side.CLIENT)
	public Icon getTextureIcon() {
		if (active) {
			switch (color) {
			case Red:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Red_Active];
			case Blue:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Blue_Active];
			case Green:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Green_Active];
			case Yellow:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Yellow_Active];
			}
		} else {
			switch (color) {
			case Red:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Red_Inactive];
			case Blue:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Blue_Inactive];
			case Green:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Green_Inactive];
			case Yellow:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Yellow_Inactive];
			}
		}

		return null;
	}
}
