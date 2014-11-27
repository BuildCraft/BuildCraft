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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Gate;

public class ActionRedstoneOutput extends BCStatement implements IActionInternal {

	public ActionRedstoneOutput(String s) {
		// Used by fader output
		super(s);
	}

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

	protected boolean isSideOnly(IStatementParameter[] parameters) {
		if (parameters != null && parameters.length >= 1 && parameters[0] instanceof StatementParameterRedstoneGateSideOnly) {
			return ((StatementParameterRedstoneGateSideOnly) parameters[0]).isOn;
		}

		return false;
	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		if (source instanceof Gate) {
			((Gate) source).setRedstoneOutput(isSideOnly(parameters), getSignalLevel());
		}
	}

	protected int getSignalLevel() {
		return 15;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("buildcraft:triggers/action_redstoneoutput");
	}
}
