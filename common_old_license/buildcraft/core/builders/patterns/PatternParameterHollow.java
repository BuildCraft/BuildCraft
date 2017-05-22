package buildcraft.core.builders.patterns;

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

public enum PatternParameterHollow implements IStatementParameter {
    FILLED(true),
    HOLLOW(false);

    public final boolean filled;

    PatternParameterHollow(boolean filled) {
        this.filled = filled;
    }

    public static PatternParameterHollow readFromNbt(NBTTagCompound nbt) {
        if (nbt.getBoolean("filled")) {
            return FILLED;
        }
        return HOLLOW;
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        compound.setBoolean("filled", filled);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterHollow";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getGuiSprite() {
        return filled ? BCCoreSprites.PARAM_FILLED : BCCoreSprites.PARAM_HOLLOW;
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("fillerpattern.parameter." + (filled ? "filled" : "hollow"));
    }

    @Override
    public PatternParameterHollow onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public PatternParameterHollow[] getPossible(IStatementContainer source, IStatement stmt) {
        return values();
    }
}
