/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern.parameter;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.builders.BCBuildersSprites;

public enum PatternParameterFacing implements IStatementParameter {
    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST);

    public final EnumFacing face;

    private static final Map<EnumFacing, PatternParameterFacing> faceToParam;

    static {
        faceToParam = new EnumMap<>(EnumFacing.class);
        for (PatternParameterFacing param : values()) {
            faceToParam.put(param.face, param);
        }
    }

    PatternParameterFacing(EnumFacing face) {
        this.face = face;
    }

    public static PatternParameterFacing readFromNbt(NBTTagCompound nbt) {
        return values()[MathUtil.clamp(nbt.getByte("v"), 0, 6)];
    }

    public static PatternParameterFacing get(EnumFacing face) {
        return faceToParam.get(face);
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setByte("v", (byte) ordinal());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterFacing";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_FACE.get(face);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("buildcraft.param.facing." + face.getName());
    }

    @Override
    public PatternParameterFacing onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return values();
    }

    @Override
    public boolean isPossibleOrdered() {
        return false;
    }
}
