/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.utils.StringUtils;

public class ActionRedstoneOutput extends BCStatement implements IActionInternal {

	public ActionRedstoneOutput() {
		super("buildcraft:redstone.output", "buildcraft.redstone.output");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.redstone.signal");
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

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("buildcraft:triggers/action_redstoneoutput");
	}
}
