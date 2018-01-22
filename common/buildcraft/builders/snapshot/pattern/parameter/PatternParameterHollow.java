/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern.parameter;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.BCBuildersSprites;

public enum PatternParameterHollow implements IStatementParameter {
    FILLED_INNER(true, false),
    FILLED_OUTER(true, true),
    HOLLOW(false, false);

    public final boolean filled;
    public final boolean outerFilled;

    PatternParameterHollow(boolean filled, boolean outerFilled) {
        this.filled = filled;
        this.outerFilled = outerFilled;
    }

    public static PatternParameterHollow readFromNbt(NBTTagCompound nbt) {
        if (nbt.getBoolean("filled")) {
            if (nbt.getBoolean("outer")) {
                return FILLED_OUTER;
            } else {
                return FILLED_INNER;
            }
        }
        return HOLLOW;
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        compound.setBoolean("filled", filled);
        if (filled) {
            compound.setBoolean("outer", outerFilled);
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterHollow";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        if (filled) {
            if (outerFilled) {
                return BCBuildersSprites.PARAM_FILLED_OUTER;
            } else {
                return BCBuildersSprites.PARAM_FILLED_INNER;
            }
        }
        return BCBuildersSprites.PARAM_HOLLOW;
    }

    @Nullable
    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public String getDescription() {
        String after = filled ? (outerFilled ? "filled_outer" : "filled") : "hollow";
        return LocaleUtil.localize("fillerpattern.parameter." + after);
    }

    @Override
    public PatternParameterHollow onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public PatternParameterHollow[] getPossible(IStatementContainer source) {
        return values();
    }
}
