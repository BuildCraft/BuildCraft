/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;

public class ActionEnergyPulsar extends BCStatement implements IActionInternal {

    public ActionEnergyPulsar() {
        super(new ResourceLocation("buildcrafttransport:triggers/action_pulsar"), "buildcraft:pulsar.constant", "buildcraft.pulser.constant");
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("gate.action.pulsar.constant");
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
