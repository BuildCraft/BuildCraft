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
import buildcraft.core.PowerMode;
import buildcraft.core.triggers.BCActionPassive;

public class ActionPowerLimiter extends BCActionPassive {

	public final PowerMode limit;

	public ActionPowerLimiter(PowerMode limit) {
		super("buildcraft:power.limiter." + limit.name().toLowerCase(Locale.ENGLISH), "buildcraft.power.limiter." + limit.name().toLowerCase(Locale.ENGLISH));

		this.limit = limit;
	}

	@Override
	public String getDescription() {
		return limit.maxPower + " RF/t Limit";
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_limiter_" + limit.name().toLowerCase(Locale.ENGLISH));
	}
}
