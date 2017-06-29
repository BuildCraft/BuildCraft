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
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_FLUID_LEVEL.get(type);
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public String getDescription() {
        return String.format(LocaleUtil.localize("gate.trigger.fluidlevel.below"), (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite());
        if (handler == null) {
            return false;
        }
        FluidStack searchedFluid = null;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack() .isEmpty()) {
            searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack());
            if (searchedFluid != null) {
                searchedFluid.amount = 1;
            }
        }

        IFluidTankProperties[] tankPropertiesArray = handler.getTankProperties();
        if (tankPropertiesArray == null || tankPropertiesArray.length == 0) {
            return false;
        }

        for (IFluidTankProperties tankProperties : tankPropertiesArray) {
            if (tankProperties == null) {
                continue;
            }
            FluidStack fluid = tankProperties.getContents();
            if (fluid == null) {
                return searchedFluid == null || handler.fill(searchedFluid, false) > 0;
            }

            if (searchedFluid == null || searchedFluid.isFluidEqual(fluid)) {
                float percentage = fluid.amount / (float) tankProperties.getCapacity();
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
