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
        return ItemStack.EMPTY;
    }

    @Override
    public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
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
        return this;
    }

    @Override
    public void writeToNbt(CompoundTag compound) {
        if (stack != null) {
            CompoundTag tagCompound = new CompoundTag();
            stack.deserializeNBT(tagCompound);
            compound.put("stack", tagCompound);
        }
    }

    public static StatementParameterItemStackExact readFromNbt(CompoundTag nbt) {
        StatementParameterItemStackExact param = new StatementParameterItemStackExact();
        param.stack = ItemStack.of(nbt.getCompound("stack"));
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
        if (stack != null) {
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
