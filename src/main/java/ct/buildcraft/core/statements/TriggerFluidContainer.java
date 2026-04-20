/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import java.util.Locale;

import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerExternal;
import ct.buildcraft.api.statements.StatementParameterItemStack;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.core.BCCoreStatements;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import ct.buildcraft.lib.misc.CapUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

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
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite()).orElse(null);

        if (handler != null) {
            FluidStack searchedFluid = FluidStack.EMPTY;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
                searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack()).orElse(searchedFluid);
            }

            if (!searchedFluid.isEmpty()) {
                searchedFluid.setAmount(1);
            }

            int liquids = handler.getTanks();
            if (liquids == 0) {
                return false;
            }

            switch (state) {
                case EMPTY:
                    FluidStack drained = handler.drain(1, FluidAction.SIMULATE);
                    return drained.isEmpty() || drained.getAmount() <= 0;
                case CONTAINS:
                    for (int i = 0; i < liquids ; i++) {
                        FluidStack fluid = handler.getFluidInTank(i);
                        if (!fluid.isEmpty() && (searchedFluid.isEmpty()|| searchedFluid.isFluidEqual(fluid))) {
                            return true;
                        }
                    }
                    return false;
                case SPACE:
                    if (searchedFluid.isEmpty()) {
                        for (int i = 0; i < liquids ; i++) {
                            FluidStack fluid = handler.getFluidInTank(i);
                            if ((fluid.isEmpty() || fluid.getAmount() < handler.getTankCapacity(i))) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return handler.fill(searchedFluid, FluidAction.SIMULATE) > 0;
                case FULL:
                    if (searchedFluid.isEmpty()) {
                        for (int i = 0; i < liquids ; i++) {
                            FluidStack fluid = handler.getFluidInTank(1);
                            if ((fluid.isEmpty() || fluid.getAmount() < handler.getTankCapacity(i))) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return handler.fill(searchedFluid, FluidAction.SIMULATE) <= 0;
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
