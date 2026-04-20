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

    public static PatternParameterHollow readFromNbt(CompoundTag nbt) {
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
    public void writeToNbt(CompoundTag compound) {
        compound.putBoolean("filled", filled);
        if (filled) {
            compound.putBoolean("outer", outerFilled);
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterHollow";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public Component getDescription() {
        String after = filled ? (outerFilled ? "filled_outer" : "filled") : "hollow";
        return Component.translatable("fillerpattern.parameter." + after);
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
