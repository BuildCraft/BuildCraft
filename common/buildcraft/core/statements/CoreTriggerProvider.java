/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Collection;

import buildcraft.api.tiles.TilesAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.statements.*;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.tiles.IHasWork;

import buildcraft.core.BCCoreStatements;
import buildcraft.lib.misc.CapUtil;

public enum CoreTriggerProvider implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> res, IStatementContainer container) {
        res.add(BCCoreStatements.TRIGGER_TRUE);
        if (container instanceof IRedstoneStatementContainer) {
            res.add(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_REDSTONE_INACTIVE);
        }

        if (TriggerPower.isTriggeringTile(container.getTile())) {
            res.add(BCCoreStatements.TRIGGER_POWER_HIGH);
            res.add(BCCoreStatements.TRIGGER_POWER_LOW);
        }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> res, IStatementContainer container, EnumFacing side) {}

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> res, EnumFacing side, TileEntity tile) {

        if (TriggerPower.isTriggeringTile(tile, side.getOpposite())) {
            res.add(BCCoreStatements.TRIGGER_POWER_HIGH);
            res.add(BCCoreStatements.TRIGGER_POWER_LOW);
        }

        boolean blockInventoryTriggers = false;
        boolean blockFluidHandlerTriggers = false;

        if (tile instanceof IBlockDefaultTriggers) {
            blockInventoryTriggers = ((IBlockDefaultTriggers) tile).blockInventoryTriggers(side);
            blockFluidHandlerTriggers = ((IBlockDefaultTriggers) tile).blockFluidHandlerTriggers(side);
        }

        if (!blockInventoryTriggers) {
            IItemHandler itemHandler = tile.getCapability(CapUtil.CAP_ITEMS, side.getOpposite());
            if (itemHandler != null) {
                res.add(BCCoreStatements.TRIGGER_INVENTORY_EMPTY);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_SPACE);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_CONTAINS);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_FULL);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_25);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_50);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_75);
            }
        }

        if (!blockFluidHandlerTriggers) {
            IFluidHandler fluidHandler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite());
            if (fluidHandler != null) {

                IFluidTankProperties[] liquids = fluidHandler.getTankProperties();
                if (liquids != null && liquids.length > 0) {
                    res.add(BCCoreStatements.TRIGGER_FLUID_EMPTY);
                    res.add(BCCoreStatements.TRIGGER_FLUID_SPACE);
                    res.add(BCCoreStatements.TRIGGER_FLUID_CONTAINS);
                    res.add(BCCoreStatements.TRIGGER_FLUID_FULL);
                    res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_25);
                    res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_50);
                    res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_75);
                }
            }
        }

        if (tile.hasCapability(TilesAPI.CAP_HAS_WORK, null)) {
            res.add(BCCoreStatements.TRIGGER_MACHINE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_MACHINE_INACTIVE);
        }
    }
}
