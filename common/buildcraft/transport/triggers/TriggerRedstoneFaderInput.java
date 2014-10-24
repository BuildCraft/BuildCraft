/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.gates.IGate;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.TileGenericPipe;

public class TriggerRedstoneFaderInput extends BCTrigger {

	public final int level;

	public TriggerRedstoneFaderInput(int level) {
		super(String.format("buildcraft:redtone.input.%02d", level));

		this.level = level;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.redstone.input.level"), level);
	}

	@Override
	public boolean isTriggerActive(IGate gate, ITriggerParameter[] parameter) {
		TileGenericPipe tile = (TileGenericPipe) gate.getPipe().getTile();

		return tile.redstoneInputSide[gate.getSide().ordinal()] == level;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon(String.format("buildcraft:triggers/redstone_%02d", level));
	}
}
