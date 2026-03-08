/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Locale;

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
    public ITextComponent getDescription() {
//        return String.format(LocaleUtil.localize("gate.trigger.fluidlevel.below"), (int) (type.level * 100));
        return new TranslationTextComponent("gate.trigger.fluidlevel.below", (int) (type.level * 100));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.fluidlevel.below." + (int) (type.level * 100);
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite()).orElse(null);
        if (handler == null) {
            return false;
        }
        FluidStack searchedFluid = null;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
            searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack()).orElse(null);
            if (searchedFluid != null) {
                searchedFluid.setAmount(1);
            }
        }

//        IFluidTankProperties[] tankPropertiesArray = handler.getTankProperties();
        int tankPropertiesArray = handler.getTanks();
//        if (tankPropertiesArray == null || tankPropertiesArray.length == 0)
        if (tankPropertiesArray == 0) {
            return false;
        }

//        for (IFluidTankProperties tankProperties : tankPropertiesArray)
        for (int i = 0; i < tankPropertiesArray; i++) {
//            if (tankProperties == null) { continue; }
//            FluidStack fluid = tankProperties.getContents();
            FluidStack fluid = handler.getFluidInTank(i);
//            if (fluid == null)
            if (fluid.isEmpty()) {
                return searchedFluid == null || handler.fill(searchedFluid, IFluidHandler.FluidAction.SIMULATE) > 0;
            }

            if (searchedFluid == null || searchedFluid.isFluidEqual(fluid)) {
//                float percentage = fluid.amount / (float) tankProperties.getCapacity();
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
