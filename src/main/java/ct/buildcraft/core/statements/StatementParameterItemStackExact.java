/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementMouseClick;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class StatementParameterItemStackExact implements IStatementParameter {
    protected ItemStack stack;

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        int delta = mouse.isShift() ? 16 : 1;

        if (stack != null && !stack.isEmpty()) {
            if (areItemsEqual(this.stack, stack)) {
                int count = this.stack.getCount() + (mouse.getButton() == 0 ? delta : -delta);
                if (count <= 0) {
                    this.stack = null;
                } else {
                    this.stack.setCount(Math.min(count, this.stack.getMaxStackSize()));
                }
            } else {
                this.stack = stack.copy();
            }
        } else if (this.stack != null && !this.stack.isEmpty()) {
            int count = this.stack.getCount() + (mouse.getButton() == 0 ? delta : -delta);
            if (count <= 0) {
                this.stack = null;
            } else {
                this.stack.setCount(Math.min(count, this.stack.getMaxStackSize()));
            }
        }
        return this;
    }

    @Override
    public void writeToNbt(CompoundTag compound) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tagCompound = new CompoundTag();
            stack.save(tagCompound);
            compound.put("stack", tagCompound);
        }
    }

    public static StatementParameterItemStackExact readFromNbt(CompoundTag nbt) {
        StatementParameterItemStackExact param = new StatementParameterItemStackExact();
        ItemStack read = ItemStack.of(nbt.getCompound("stack"));
        param.stack = read.isEmpty() ? null : read;
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
            return stack2 != null && stack1.sameItem(stack2) && ItemStack.isSameItemSameTags(stack1, stack2);
        } else {
            return stack2 == null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    @Override
    public Component getDescription() {
        if (stack != null && !stack.isEmpty()) {
            return stack.getDisplayName();
        } else {
            return Component.empty();
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
