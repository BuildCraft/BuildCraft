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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import net.minecraftforge.common.util.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.builders.BCBuildersSprites;

public enum PatternParameterXZDir implements IStatementParameter {
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH);

    private static final PatternParameterXZDir[] POSSIBLE_ORDER =
        { null, null, NORTH, null, EAST, null, SOUTH, null, WEST };

    private static final Map<Direction, PatternParameterXZDir> map;

    static {
        map = new EnumMap<>(Direction.class);
        for (PatternParameterXZDir param : values()) {
            map.put(param.dir, param);
        }
    }

    public final Direction dir;

    PatternParameterXZDir(Direction dir) {
        this.dir = dir;
    }

    public static PatternParameterXZDir get(Direction face) {
        PatternParameterXZDir param = map.get(face);
        if (param == null) {
            throw new IllegalArgumentException("Can only accept horizontal Direction's (was given " + face + ")");
        }
        return param;
    }

    public static PatternParameterXZDir readFromNbt(NbtCompound nbt) {
        Direction dir;
        if (nbt.contains("dir", Constants.NBT.TAG_ANY_NUMERIC)) {
            // Older versions
            int d = nbt.getByte("dir") + 2;
            dir = Direction.fromHorizontal(d);
        } else {
            dir = Direction.fromHorizontal(nbt.getByte("d"));
        }
        PatternParameterXZDir param = map.get(dir);
        if (param == null) {
            throw new IllegalStateException("Map lookup failed for " + dir);
        }
        return param;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putByte("d", (byte) dir.getHorizontalIndex());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterXZDir";
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_XZ_DIR.get(dir);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("direction." + dir.getName());
    }

    @Override
    public PatternParameterXZDir onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
        return get(dir.rotateY());
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
