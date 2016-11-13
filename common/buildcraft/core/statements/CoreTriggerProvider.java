/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.statements.*;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.tiles.IHasWork;

import buildcraft.core.BCCoreStatements;

public enum CoreTriggerProvider implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> res, IStatementContainer container) {
        res.add(BCCoreStatements.TRIGGER_TRUE);
        if (container instanceof IRedstoneStatementContainer) {
            res.add(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_REDSTONE_INACTIVE);
        }

        // if (TriggerEnergy.isTriggeringPipe(container.getTile()) ||
        // TriggerEnergy.getTriggeringNeighbor(container.getTile()) != null) {
        // res.add((ITriggerInternal) BuildCraftCore.triggerEnergyHigh);
        // res.add((ITriggerInternal) BuildCraftCore.triggerEnergyLow);
        // }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> res, IStatementContainer container, EnumFacing side) {

    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> res, EnumFacing side, TileEntity tile) {

        boolean blockInventoryTriggers = false;
        boolean blockFluidHandlerTriggers = false;

        if (tile instanceof IBlockDefaultTriggers) {
            blockInventoryTriggers = ((IBlockDefaultTriggers) tile).blockInventoryTriggers(side);
            blockFluidHandlerTriggers = ((IBlockDefaultTriggers) tile).blockFluidHandlerTriggers(side);
        }

        if (!blockInventoryTriggers) {
            IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
            if (itemHandler != null) {
                // res.add(BuildCraftCore.triggerEmptyInventory);
                // res.add(BuildCraftCore.triggerContainsInventory);
                // res.add(BuildCraftCore.triggerSpaceInventory);
                // res.add(BuildCraftCore.triggerFullInventory);
                // res.add(BuildCraftCore.triggerInventoryBelow25);
                // res.add(BuildCraftCore.triggerInventoryBelow50);
                // res.add(BuildCraftCore.triggerInventoryBelow75);
            }
        }

        if (!blockFluidHandlerTriggers) {
            IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
            if (fluidHandler != null) {
                // FluidTankInfo[] tanks = ((IFluidHandler) tile).getTankInfo(side.getOpposite());
                // if (tanks != null && tanks.length > 0) {
                // res.add(BuildCraftCore.triggerEmptyFluid);
                // res.add(BuildCraftCore.triggerContainsFluid);
                // res.add(BuildCraftCore.triggerSpaceFluid);
                // res.add(BuildCraftCore.triggerFullFluid);
                // res.add(BuildCraftCore.triggerFluidContainerBelow25);
                // res.add(BuildCraftCore.triggerFluidContainerBelow50);
                // res.add(BuildCraftCore.triggerFluidContainerBelow75);
                // }
            }
        }

        if (tile instanceof IHasWork) {
            res.add(BCCoreStatements.TRIGGER_MACHINE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_MACHINE_INACTIVE);
        }
    }
}
