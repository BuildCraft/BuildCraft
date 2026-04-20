/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot.pattern.parameter;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementMouseClick;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    public static PatternParameterCenter readFromNbt(CompoundTag nbt) {
        int ord = nbt.getByte("dir");
        if (ord < 0 || ord >= values().length) {
            return CENTER;
        }
        return values()[ord];
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putByte("dir", (byte) ordinal());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterCenter";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_CENTER.get(this);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("direction.center." + ordinal());
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
