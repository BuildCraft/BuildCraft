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

import buildcraft.core.BCCoreSprites;

public enum PatternParameterRotation implements IStatementParameter {
    NONE,
    QUARTER,
    HALF,
    THREE_QUARTERS;

    private static final PatternParameterRotation[] POSSIBLE_ORDER =
        { null, null, NONE, null, QUARTER, null, HALF, null, THREE_QUARTERS };

    public final int rotationCount;

    PatternParameterRotation() {
        this.rotationCount = ordinal();
    }

    public static PatternParameterRotation readFromNbt(NBTTagCompound nbt) {
        int d = nbt.getByte("d");
        return values()[d & 3];
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setByte("d", (byte) rotationCount);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterRotation";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.PARAM_ROTATION[rotationCount];
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("buildcraft.param.rotation." + rotationCount);
    }

    @Override
    public PatternParameterRotation onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
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
