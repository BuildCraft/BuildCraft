/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot.pattern.parameter;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementMouseClick;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.lib.misc.MathUtil;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum PatternParameterFacing implements IStatementParameter {
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST);

    public final Direction face;

    private static final Map<Direction, PatternParameterFacing> faceToParam;

    static {
        faceToParam = new EnumMap<>(Direction.class);
        for (PatternParameterFacing param : values()) {
            faceToParam.put(param.face, param);
        }
    }

    PatternParameterFacing(Direction face) {
        this.face = face;
    }

    public static PatternParameterFacing readFromNbt(CompoundTag nbt) {
        return values()[MathUtil.clamp(nbt.getByte("v"), 0, 6)];
    }

    public static PatternParameterFacing get(Direction face) {
        return faceToParam.get(face);
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putByte("v", (byte) ordinal());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterFacing";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_FACE.get(face);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("buildcraft.param.facing." + face.getName());
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
