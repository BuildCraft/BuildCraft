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

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IAction;
import buildcraft.core.triggers.BCAction;

public class ActionPipeDirection extends BCAction {

	public final ForgeDirection direction;
	private IIcon icon;

	public ActionPipeDirection(ForgeDirection direction) {
		super("buildcraft:pipe.dir." + direction.name().toLowerCase(Locale.ENGLISH), "buildcraft.pipe.dir." + direction.name().toLowerCase(Locale.ENGLISH));

		this.direction = direction;
	}

	@Override
	public String getDescription() {
		return direction.name().substring(0, 1) + direction.name().substring(1).toLowerCase(Locale.ENGLISH) + " Pipe Direction";
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_dir_" + direction.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public IAction rotateLeft() {
		return BuildCraftTransport.actionPipeDirection [direction.getRotation(ForgeDirection.UP).ordinal()];
	}
}
