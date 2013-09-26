/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.core.triggers.BCAction;
import buildcraft.transport.pipes.PipePowerIron.PowerMode;
import java.util.Locale;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class ActionPowerLimiter extends BCAction {

	private Icon icon;
	public final PowerMode limit;

	public ActionPowerLimiter(int id, PowerMode limit) {
		super(id, "buildcraft.power.limiter." + limit.name().toLowerCase(Locale.ENGLISH));

		this.limit = limit;
	}

	@Override
	public String getDescription() {
		return limit.maxPower + " MJ/t Limit";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_limiter_" + limit.name().toLowerCase(Locale.ENGLISH));
	}
}
