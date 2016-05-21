/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.statements;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftEnergy;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.energy.TileEngineIron;

public class EnergyStatementProvider implements ITriggerProvider {

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {

    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, EnumFacing side, TileEntity tile) {
        if (tile instanceof TileEngineBase) {
            triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
            triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
            triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
            triggers.add(BuildCraftEnergy.triggerRedEngineHeat);
            triggers.add(BuildCraftEnergy.triggerEngineOverheat);
        }

        if (tile instanceof TileEngineIron) {
            triggers.add(BuildCraftEnergy.triggerCoolantBelow25);
            triggers.add(BuildCraftEnergy.triggerCoolantBelow50);

            triggers.add(BuildCraftEnergy.triggerFuelBelow25);
            triggers.add(BuildCraftEnergy.triggerFuelBelow50);
        }
    }
}
