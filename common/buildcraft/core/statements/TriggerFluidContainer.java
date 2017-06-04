/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;

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
        return LocaleUtil.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite());

        if (handler != null) {
            FluidStack searchedFluid = null;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
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
