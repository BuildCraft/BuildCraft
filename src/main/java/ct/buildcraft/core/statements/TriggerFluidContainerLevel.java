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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TriggerFluidContainerLevel extends BCStatement implements ITriggerExternal {
    public final TriggerType type;

    public TriggerFluidContainerLevel(TriggerType type) {
        super(
            "buildcraft:fluid." + type.name().toLowerCase(Locale.ROOT),
            "buildcraft.fluid." + type.name().toLowerCase(Locale.ROOT)
        );
        this.type = type;
    }

    @Override
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_FLUID_LEVEL.get(type);
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.fluidlevel.below", (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite()).orElse(null);
        if (handler == null) {
            return false;
        }
        FluidStack searchedFluid = FluidStack.EMPTY;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack() .isEmpty()) {
            searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack()).orElse(searchedFluid);
            if (!searchedFluid.isEmpty()) {
                searchedFluid.setAmount(1);
            }
        }

        int tanks = handler.getTanks();
        if (tanks == 0) {
            return false;
        }

        for (int i = 0; i < tanks ; i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            if (fluid.isEmpty()) {
                return searchedFluid.isEmpty() || handler.fill(searchedFluid, FluidAction.SIMULATE) > 0;
            }

            if (searchedFluid.isEmpty() || searchedFluid.isFluidEqual(fluid)) {
                float percentage = fluid.getAmount() / (float) handler.getTankCapacity(i);
                return percentage < type.level;
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

    public enum TriggerType {
        BELOW25(0.25F),
        BELOW50(0.5F),
        BELOW75(0.75F);

        TriggerType(float level) {
            this.level = level;
        }

        public static final TriggerType[] VALUES = values();

        public final float level;
    }
}
