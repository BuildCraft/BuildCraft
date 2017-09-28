/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import java.util.Objects;

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

public class StatementParameterRedstoneLevel implements IStatementParameter {
    public final int level;
    private final int minLevel, maxLevel;

    public StatementParameterRedstoneLevel() {
        this(0, 0, 15);
    }

    public StatementParameterRedstoneLevel(int min, int max) {
        this(0, min, max);
    }

    public StatementParameterRedstoneLevel(int def, int min, int max) {
        level = def;
        minLevel = min;
        maxLevel = max;
    }

    public StatementParameterRedstoneLevel(NBTTagCompound nbt) {
        level = nbt.getByte("l");
        minLevel = nbt.getByte("ml");
        maxLevel = nbt.getByte("ma");
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setByte("l", (byte) level);
        nbt.setByte("mi", (byte) minLevel);
        nbt.setByte("ma", (byte) maxLevel);
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.PARAM_REDSTONE_LEVEL[level & 15];
    }

    @Override
    public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        int l;
        if (mouse.getButton() == 0) {
            l = (level + 1) & 15;
            while (level < minLevel || level > maxLevel) {
                l = (level + 1) & 15;
            }
        } else {
            l = (level - 1) & 15;
            while (level < minLevel || level > maxLevel) {
                l = (level - 1) & 15;
            }
        }
        return new StatementParameterRedstoneLevel(l, minLevel, maxLevel);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterRedstoneLevel) {
            StatementParameterRedstoneLevel param = (StatementParameterRedstoneLevel) object;
            return param.level == this.level;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

    @Override
    public String getDescription() {
        return String.format(LocaleUtil.localize("gate.trigger.redstone.input.level"), level);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:redstoneLevel";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        IStatementParameter[] possible = new IStatementParameter[maxLevel - minLevel];
        for (int i = 0; i < maxLevel - minLevel; i++) {
            int l = minLevel + i;
            if (level == l) {
                possible[i] = this;
            } else {
                possible[i] = new StatementParameterRedstoneLevel(l, minLevel, maxLevel);
            }
        }
        return possible;
    }
}
