/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

public class StatementParameterItemStackExact implements IStatementParameter {
    protected ItemStack stack;

    @Nullable
    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (stack != null) {
            if (areItemsEqual(this.stack, stack)) {
                if (mouse.getButton() == 0) {
                    this.stack.stackSize = (this.stack.stackSize + ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.stackSize > 64) {
                        this.stack.stackSize = 64;
                    }
                } else {
                    this.stack.stackSize = (this.stack.stackSize - ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.stackSize < 0) {
                        this.stack.stackSize = 0;
                    }
                }
            } else {
                this.stack = stack.copy();
            }
        } else {
            if (this.stack != null) {
                if (mouse.getButton() == 0) {
                    this.stack.stackSize = (this.stack.stackSize + ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.stackSize > 64) {
                        this.stack.stackSize = 64;
                    }
                } else {
                    this.stack.stackSize = (this.stack.stackSize - ((mouse.isShift()) ? 16 : 1));
                    if (this.stack.stackSize < 0) {
                        this.stack = null;
                    }
                }
            }
        }
        return this;
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        if (stack != null) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            stack.writeToNBT(tagCompound);
            compound.setTag("stack", tagCompound);
        }
    }

    public static StatementParameterItemStackExact readFromNbt(NBTTagCompound nbt) {
        StatementParameterItemStackExact param = new StatementParameterItemStackExact();
        param.stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
        return param;
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
    public ISprite getSprite() {
        // What's rendered is not a sprite but the actual stack itself
        return null;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return null;
    }
}
