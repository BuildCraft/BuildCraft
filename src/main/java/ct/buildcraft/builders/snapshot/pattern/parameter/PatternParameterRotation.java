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

    public static PatternParameterRotation readFromNbt(CompoundTag nbt) {
        int d = nbt.getByte("d");
        return values()[d & 3];
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putByte("d", (byte) rotationCount);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterRotation";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_ROTATION[rotationCount];
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("buildcraft.param.rotation." + rotationCount);
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
