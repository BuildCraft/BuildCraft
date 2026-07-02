/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.statements;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.render.ISprite;
import com.google.common.collect.ImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

public class StatementParameterItemStack implements IStatementParameter {
    // needed because ItemStack.EMPTY doesn't have @Nonnull applied to it :/
    @Nonnull
    private static final ItemStack EMPTY_STACK;

    /** Immutable parameter that has the {@link ItemStack#EMPTY} as it's {@link #stack}. */
    public static final StatementParameterItemStack EMPTY;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new Error("Somehow ItemStack.EMPTY was null!");
        EMPTY_STACK = stack;
        EMPTY = new StatementParameterItemStack();
    }

    @Nonnull
    protected final ItemStack stack;

    public StatementParameterItemStack() {
        stack = EMPTY_STACK;
    }

    public StatementParameterItemStack(@Nonnull ItemStack stack) {
        this.stack = stack;
    }

    public StatementParameterItemStack(CompoundTag nbt) {
        ItemStack read = ItemStack.of(nbt);
        if (read.isEmpty()) {
            stack = EMPTY_STACK;
        } else {
            stack = read;
        }
    }

    @Override
    public void writeToNbt(CompoundTag compound) {
        if (!stack.isEmpty()) {
            stack.save(compound);
        }
    }

    @Override
    public ISprite getSprite() {
        return null;
    }

    @Override
    @Nonnull
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public StatementParameterItemStack onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (stack.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            return new StatementParameterItemStack(newStack);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStack) {
            StatementParameterItemStack param = (StatementParameterItemStack) object;

            return ItemStack.isSame(stack, param.stack)
            && ItemStack.isSameItemSameTags(stack, param.stack);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescription() {
        throw new UnsupportedOperationException("Don't call getDescription directly!");
    }

    @SuppressWarnings("deprecation")
	@Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getTooltip() {
        if (stack.isEmpty()) {
            return ImmutableList.of();
        }
        List<Component> tooltip = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
        if (!tooltip.isEmpty()) {
            tooltip.set(0, MutableComponent.create(tooltip.get(0).getContents()).withStyle(stack.getRarity().color));
            for (int i = 1; i < tooltip.size(); i++) {
                tooltip.set(i, MutableComponent.create(tooltip.get(i).getContents()).withStyle(ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stack";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return null;
    }
}
