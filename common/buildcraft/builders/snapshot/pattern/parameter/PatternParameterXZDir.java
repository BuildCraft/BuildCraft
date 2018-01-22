/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern.parameter;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.BCBuildersSprites;

public enum PatternParameterXZDir implements IStatementParameter {
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH);

    private static final PatternParameterXZDir[] POSSIBLE_ORDER =
        { null, null, NORTH, null, EAST, null, SOUTH, null, WEST };

    private static final Map<EnumFacing, PatternParameterXZDir> map;

    static {
        map = new EnumMap<>(EnumFacing.class);
        for (PatternParameterXZDir param : values()) {
            map.put(param.dir, param);
        }
    }

    public final EnumFacing dir;

    PatternParameterXZDir(EnumFacing dir) {
        this.dir = dir;
    }

    public static PatternParameterXZDir get(EnumFacing face) {
        PatternParameterXZDir param = map.get(face);
        if (param == null) {
            throw new IllegalArgumentException("Can only accept horizontal EnumFacing's (was given " + face + ")");
        }
        return param;
    }

    public static PatternParameterXZDir readFromNbt(NBTTagCompound nbt) {
        EnumFacing dir;
        if (nbt.hasKey("dir", Constants.NBT.TAG_ANY_NUMERIC)) {
            // Older versions
            int d = nbt.getByte("dir") + 2;
            dir = EnumFacing.getHorizontal(d);
        } else {
            dir = EnumFacing.getHorizontal(nbt.getByte("d"));
        }
        PatternParameterXZDir param = map.get(dir);
        if (param == null) {
            throw new IllegalStateException("Map lookup failed for " + dir);
        }
        return param;
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setByte("d", (byte) dir.getHorizontalIndex());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterXZDir";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_XZ_DIR.get(dir);
    }

    @Nullable
    @Override
    public ItemStack getItemStack() {
        return null;
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
