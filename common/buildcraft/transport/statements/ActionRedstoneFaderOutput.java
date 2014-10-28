/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.core.statements.BCActionPassive;
import buildcraft.core.statements.StatementParameterRedstoneGateSideOnly;
import buildcraft.core.utils.StringUtils;

public class ActionRedstoneFaderOutput extends BCActionPassive {

	public final int level;

	public ActionRedstoneFaderOutput(int level) {
		super(String.format("buildcraft:redstone.output.%02d", level));

		this.level = level;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.redstone.input.level"), level);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon(String.format("buildcraft:triggers/redstone_%02d", level));
	}
	
    @Override
    public IStatementParameter createParameter(int index) {
		IStatementParameter param = null;
	
		if (index == 0) {
		    param = new StatementParameterRedstoneGateSideOnly();
		}
	
		return param;
    }
	
	@Override
	public int maxParameters() {
		return 1;
	}
}
