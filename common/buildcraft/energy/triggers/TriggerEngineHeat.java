/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.triggers;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngine;
import buildcraft.energy.TileEngine.EnergyStage;

public class TriggerEngineHeat extends BCTrigger implements ITileTrigger {

	public EnergyStage stage;
	@SideOnly(Side.CLIENT)
	private IIcon icon;

	public TriggerEngineHeat(EnergyStage stage) {
		super("buildcraft:engine.stage." + stage.name().toLowerCase(Locale.ENGLISH), "buildcraft.engine.stage." + stage.name().toLowerCase(Locale.ENGLISH));

		this.stage = stage;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.engine." + stage.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileEngine) {
			TileEngine engine = (TileEngine) tile;

			return engine.getEnergyStage() == stage;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_" + stage.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
