/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern.parameter;

import javax.annotation.Nonnull;

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
import buildcraft.lib.misc.StackUtil;

import buildcraft.builders.BCBuildersSprites;

public enum PatternParameterCenter implements IStatementParameter {
    NORTH_WEST(-1, -1),
    NORTH(0, -1),
    NORTH_EAST(1, -1),
    WEST(-1, 0),
    CENTER(0, 0),
    EAST(1, 0),
    SOUTH_WEST(-1, 1),
    SOUTH(0, 1),
    SOUTH_EAST(1, 1);

    public static final PatternParameterCenter[] POSSIBLE_ORDER = {//
        CENTER, NORTH_WEST, NORTH, NORTH_EAST, EAST,//
        SOUTH_EAST, SOUTH, SOUTH_WEST, WEST//
    };

    public final int offsetX, offsetZ;

    PatternParameterCenter(int x, int z) {
        offsetX = x;
        offsetZ = z;
    }

    public static PatternParameterCenter readFromNbt(NBTTagCompound nbt) {
        int ord = nbt.getByte("dir");
        if (ord < 0 || ord >= values().length) {
            return CENTER;
        }
        return values()[ord];
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setByte("dir", (byte) ordinal());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterCenter";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_CENTER.get(this);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("direction.center." + ordinal());
    }

    @Override
    public PatternParameterCenter onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
        // return new PatternParameterCenter(shiftLeft[direction % 9]);
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return POSSIBLE_ORDER;
    }

    @Override
    public boolean isPossibleOrdered() {
        return true;
    }
}
