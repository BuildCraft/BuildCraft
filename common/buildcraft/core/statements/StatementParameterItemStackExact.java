/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.StackUtil;

public class StatementParameterItemStackExact implements IStatementParameter {
    protected ItemStack stack;

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public boolean onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (stack != null) {
            if (areItemsEqual(this.stack, stack)) {
                if (mouse.getButton() == 0) {
                    this.stack.setCount(this.stack.getCount() + ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.getCount() > 64) {
                        this.stack.setCount(64);
                    }
                } else {
                    this.stack.setCount(this.stack.getCount() - ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.getCount() < 0) {
                        this.stack.setCount(0);
                    }
                }
            } else {
                this.stack = stack.copy();
            }
        } else {
            if (this.stack != null) {
                if (mouse.getButton() == 0) {
                    this.stack.setCount(this.stack.getCount() + ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.getCount() > 64) {
                        this.stack.setCount(64);
                    }
                } else {
                    this.stack.setCount(this.stack.getCount() - ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.getCount() < 0) {
                        this.stack = null;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if (stack != null) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            stack.writeToNBT(tagCompound);
            compound.setTag("stack", tagCompound);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        stack = new ItemStack(compound.getCompoundTag("stack"));
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStackExact) {
            StatementParameterItemStackExact param = (StatementParameterItemStackExact) object;

            return areItemsEqual(stack, param.stack);
        } else {
            return false;
        }
    }

    private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 != null) {
            return stack2 != null && stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
        } else {
            return stack2 == null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    @Override
    public String getDescription() {
        if (stack != null) {
            return stack.getDisplayName();
        } else {
            return "";
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stackExact";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public TextureAtlasSprite getGuiSprite() {
        // Whats rendered is not a sprite but the actual stack itself
        return null;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source, IStatement stmt) {
        return null;
    }
}
