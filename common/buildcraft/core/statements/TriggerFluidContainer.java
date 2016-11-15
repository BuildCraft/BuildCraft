/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.*;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StringUtilBC;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {

    public enum State {

        Empty,
        Contains,
        Space,
        Full;

        public static final State[] VALUES = values();
    }

    public State state;

    public TriggerFluidContainer(State state) {
        super("buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT), "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT));
        this.state = state;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.Contains || state == State.Space ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());

        if (handler != null) {
            FluidStack searchedFluid = null;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && parameters[0].getItemStack() != null) {
                searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack());
            }

            if (searchedFluid != null) {
                searchedFluid.amount = 1;
            }

            IFluidTankProperties[] liquids = handler.getTankProperties();
            if (liquids == null || liquids.length == 0) {
                return false;
            }

            switch (state) {
                case Empty:
                    FluidStack drained = handler.drain(1, false);
                    return drained == null || drained.amount <= 0;
                case Contains:
                    for (IFluidTankProperties c : liquids) {
                        if (c == null) continue;
                        FluidStack fluid = c.getContents();
                        if (fluid != null && fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(fluid))) {
                            return true;
                        }
                    }
                    return false;
                case Space:
                    if (searchedFluid == null) {
                        for (IFluidTankProperties c : liquids) {
                            if (c == null) continue;
                            FluidStack fluid = c.getContents();
                            if ((fluid == null || fluid.amount < c.getCapacity())) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return handler.fill(searchedFluid, false) > 0;
                case Full:
                    if (searchedFluid == null) {
                        for (IFluidTankProperties c : liquids) {
                            if (c == null) continue;
                            FluidStack fluid = c.getContents();
                            if ((fluid == null || fluid.amount < c.getCapacity())) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return handler.fill(searchedFluid, false) <= 0;
            }
        }

        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_FLUID_ALL;
    }
}
