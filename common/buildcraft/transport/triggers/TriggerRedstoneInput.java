/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class TriggerRedstoneInput extends BCTrigger implements ITriggerPipe {

	boolean active;
	@SideOnly(Side.CLIENT)
	private Icon iconActive, iconInactive;

	public TriggerRedstoneInput(int legacyId, boolean active) {
		super(legacyId, active ? "buildcraft.redtone.input.active" : "buildcraft.redtone.input.inactive");

		this.active = active;
	}

	@Override
	public String getDescription() {
		if (active)
			return "Redstone Signal On";
		else
			return "Redstone Signal Off";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (active)
			return isBeingPowered(pipe);
		return !isBeingPowered(pipe);
	}

	private boolean isBeingPowered(Pipe pipe) {
		return pipe.container.redstonePowered;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon() {
		if (active)
			return iconActive;
		else
			return iconInactive;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		iconActive = iconRegister.registerIcon("buildcraft:triggers/trigger_redstoneinput_active");
		iconInactive = iconRegister.registerIcon("buildcraft:triggers/trigger_redstoneinput_inactive");
	}
}
