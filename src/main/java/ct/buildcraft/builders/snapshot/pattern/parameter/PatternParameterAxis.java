/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot.pattern.parameter;

import java.util.Locale;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementMouseClick;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum PatternParameterAxis implements IStatementParameter {
    X(Axis.X),
    Y(Axis.Y),
    Z(Axis.Z);

    public final Direction.Axis axis;

    PatternParameterAxis(Axis axis) {
        this.axis = axis;
    }

    public static PatternParameterAxis readFromNbt(CompoundTag nbt) {
        byte ord = nbt.getByte("a");
        if (ord <= 0) {
            return X;
        }
        if (ord >= 2) {
            return Z;
        }
        return Y;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:filler_parameter_axis";
    }

    @Override
    public Component getDescription() {
        return Component.translatable("buildcraft.param.axis." + name().toLowerCase(Locale.ROOT));
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_AXIS.get(axis);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putByte("a", (byte) ordinal());
    }

    @Override
    public IStatementParameter rotateLeft() {
        switch (this) {
            case X:
                return Z;
            case Y:
                return Y;
            case Z:
                return X;
            default:
                throw new IllegalStateException("Unknown axis " + this);
        }
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return values();
    }
}
