/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.CapUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Locale;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {
    public State state;

    public TriggerFluidContainer(State state) {
        super(
                "buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT),
                "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT)
        );
        this.state = state;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
        return new TranslationTextComponent("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite()).orElse(null);

        if (handler != null) {
            FluidStack searchedFluid = null;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
                searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack()).orElse(null);
            }

            if (searchedFluid != null) {
                searchedFluid.setAmount(1);
            }

//            IFluidTankProperties[] liquids = handler.getTankProperties();
            int liquids = handler.getTanks();
//            if (liquids == null || liquids.length == 0)
            if (liquids == 0) {
                return false;
            }

            switch (state) {
                case EMPTY:
                    FluidStack drained = handler.drain(1, IFluidHandler.FluidAction.SIMULATE);
                    return drained == null || drained.isEmpty() || drained.getAmount() <= 0;
                case CONTAINS:
//                    for (IFluidTankProperties c : liquids)
                    for (int i = 0; i < liquids; i++) {
//                        if (c == null) continue;
//                        FluidStack fluid = c.getContents();
                        FluidStack fluid = handler.getFluidInTank(i);
//                        if (fluid != null && fluid.getAmount() > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(fluid)))
                        if ((!fluid.isEmpty()) && (searchedFluid == null || searchedFluid.isFluidEqual(fluid))) {
                            return true;
                        }
                    }
                    return false;
                case SPACE:
                    if (searchedFluid == null) {
//                        for (IFluidTankProperties c : liquids)
                        for (int i = 0; i < liquids; i++) {
//                            if (c == null) continue;
//                            FluidStack fluid = c.getContents();
                            FluidStack fluid = handler.getFluidInTank(i);
//                            if ((fluid == null || fluid.getAmount() < c.getCapacity()))
                            if ((fluid.isEmpty() || fluid.getAmount() < handler.getTankCapacity(i))) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return handler.fill(searchedFluid, IFluidHandler.FluidAction.SIMULATE) > 0;
                case FULL:
                    if (searchedFluid == null) {
//                        for (IFluidTankProperties c : liquids)
                        for (int i = 0; i < liquids; i++) {
//                            if (c == null) continue;
//                            FluidStack fluid = c.getContents();
                            FluidStack fluid = handler.getFluidInTank(i);
//                            if ((fluid == null || fluid.getAmount() < c.getCapacity()))
                            if ((fluid.isEmpty() || fluid.getAmount() < handler.getTankCapacity(i))) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return handler.fill(searchedFluid, IFluidHandler.FluidAction.SIMULATE) <= 0;
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

    public enum State {
        EMPTY,
        CONTAINS,
        SPACE,
        FULL;

        public static final State[] VALUES = values();
    }
}
