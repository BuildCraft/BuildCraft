/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StatementParameterItemStackExact implements IStatementParameter {
    @Nonnull
    protected final ItemStack stack;
    public static final StatementParameterItemStackExact EMPTY = new StatementParameterItemStackExact(StackUtil.EMPTY);

    public StatementParameterItemStackExact() {
        this(StackUtil.EMPTY);
    }

    public StatementParameterItemStackExact(ItemStack stack) {
        this.stack = stack;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, @Nonnull ItemStack stack, StatementMouseClick mouse) {
        // if (stack != null)
        if (!stack.isEmpty()) {
            if (areItemsEqual(this.stack, stack)) {
                ItemStack retStack = this.stack.copy();
                if (mouse.getButton() == 0) {
                    // this.stack.grow((mouse.isShift()) ? 16 : 1);
                    retStack.grow((mouse.isShift()) ? 16 : 1);
                    // if (this.stack.getCount() > 64)
                    if (retStack.getCount() > 64) {
                        // this.stack.setCount(64);
                        retStack.setCount(64);
                    }
                } else {
                    // this.stack.shrink((mouse.isShift()) ? 16 : 1);
                    retStack.shrink((mouse.isShift()) ? 16 : 1);
                    // if (this.stack.getCount() < 0)
                    if (retStack.getCount() < 0) {
                        // this.stack.setCount(0);
                        retStack.setCount(0);
                    }
                }
                return new StatementParameterItemStackExact(retStack);
            } else {
                // this.stack = stack.copy();
                return new StatementParameterItemStackExact(stack.copy());
            }
        } else {
//            if (!this.stack.isEmpty()) {
//                if (mouse.getButton() == 0) {
//                    this.stack.grow((mouse.isShift()) ? 16 : 1);
//                    if (this.stack.getCount() > 64) {
//                        this.stack.setCount(64);
//                    }
//                } else {
//                    this.stack.shrink((mouse.isShift()) ? 16 : 1);
//                    if (this.stack.getCount() < 0) {
//                        this.stack = StackUtil.EMPTY;
//                    }
//                }
//            }
            // this.stack = StackUtil.EMPTY;
            return EMPTY;
        }
        // return this;
    }

    @Override
    public IStatementParameter onScroll(IStatementContainer source, IStatement stmt, @Nonnull ItemStack stack, double delta) {
        if (this.stack.isEmpty()) {
            return EMPTY;
        }
        ItemStack retStack = this.stack.copy();
        int deltaInt = (int) delta;
        int multiplier = StackUtil.isSameItemSameDamageSameTag(retStack, stack) ? stack.getCount() : 1;
        deltaInt *= multiplier;
        if (deltaInt > 0) {
            int inc = MathHelper.clamp(deltaInt, 0, retStack.getMaxStackSize() - retStack.getCount());
            retStack.grow(inc);
        } else if (deltaInt < 0) {
            int dec = MathHelper.clamp(-deltaInt, 0, retStack.getCount());
            retStack.shrink(dec);
        }
        return new StatementParameterItemStackExact(retStack);
    }

    @Override
    public void writeToNbt(CompoundNBT compound) {
        // if (stack != null)
        if (!stack.isEmpty()) {
            CompoundNBT tagCompound = new CompoundNBT();
            stack.save(tagCompound);
            compound.put("stack", tagCompound);
        }
    }

    public static StatementParameterItemStackExact readFromNbt(CompoundNBT nbt) {
        return new StatementParameterItemStackExact(ItemStack.of(nbt.getCompound("stack")));
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
//            return stack2 != null && stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
            return stack2 != null && StackUtil.isSameItemSameDamageSameTag(stack1, stack2);
        } else {
            return stack2 == null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getDescription() {
//        if (stack != null) {
//            return stack.getDisplayName();
//        } else {
//            return new TextComponent("");
//        }
        throw new UnsupportedOperationException("Don't call getDescription directly!");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getDescriptionKey() {
//        if (stack != null) {
//            return stack.getDisplayName().getString();
//        } else {
//            return "";
//        }
        throw new UnsupportedOperationException("Don't call getDescription directly!");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ITextComponent> getTooltip() {
        if (stack.isEmpty()) {
            return ImmutableList.of();
        }
        List<ITextComponent> tooltip = stack.getTooltipLines(null, ITooltipFlag.TooltipFlags.NORMAL);
        if (!tooltip.isEmpty()) {
            tooltip.set(0, new StringTextComponent(stack.getRarity().color.toString()).append(tooltip.get(0)));
            for (int i = 1; i < tooltip.size(); i++) {
                tooltip.set(i, new StringTextComponent(TextFormatting.GRAY.toString()).append(tooltip.get(i)));
            }
        }
        return tooltip;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<String> getTooltipKey() {
        if (stack.isEmpty()) {
            return ImmutableList.of();
        }
        List<ITextComponent> tooltip = stack.getTooltipLines(null, ITooltipFlag.TooltipFlags.NORMAL);
        List<String> toolTipRet = new ArrayList<>(tooltip.size());
        if (!tooltip.isEmpty()) {
            toolTipRet.set(0, new StringTextComponent(stack.getRarity().color.toString()).append(tooltip.get(0)).getString());
            for (int i = 1; i < tooltip.size(); i++) {
                toolTipRet.set(i, new StringTextComponent(TextFormatting.GRAY.toString()).append(tooltip.get(i)).getString());
            }
        }
        return toolTipRet;
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
