/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import buildcraft.api.gates.IAction;
import buildcraft.core.triggers.BCAction;
import buildcraft.transport.pipes.PipePowerIron.PowerMode;

public class ActionPowerLimiter extends BCAction {

	public final PowerMode limit;
	private IIcon icon;

	public ActionPowerLimiter(PowerMode limit) {
		super("buildcraft:power.limiter." + limit.name().toLowerCase(Locale.ENGLISH), "buildcraft.power.limiter." + limit.name().toLowerCase(Locale.ENGLISH));

		this.limit = limit;
	}

	@Override
	public String getDescription() {
		return limit.maxPower + " MJ/t Limit";
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_limiter_" + limit.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public IAction rotateLeft() {
		return this;
	}
}
