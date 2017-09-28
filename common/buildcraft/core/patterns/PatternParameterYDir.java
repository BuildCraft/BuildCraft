package buildcraft.core.patterns;

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

public enum PatternParameterYDir implements IStatementParameter {
    UP(true),
    DOWN(false);

    private static final PatternParameterYDir[] POSSIBLE_ORDER = { null, null, UP, null, null, null, DOWN };

    public final boolean up;

    PatternParameterYDir(boolean up) {
        this.up = up;
    }

    public static PatternParameterYDir readFromNbt(NBTTagCompound nbt) {
        if (nbt.getBoolean("up")) {
            return UP;
        }
        return DOWN;
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setBoolean("up", up);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterYDir";
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("direction." + (up ? "up" : "down"));
    }

    @Override
    public PatternParameterYDir onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return POSSIBLE_ORDER;
    }

    @Override
    public boolean isPossibleOrdered() {
        return true;
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return up ? BCCoreSprites.PARAM_STAIRS_UP : BCCoreSprites.PARAM_STAIRS_DOWN;
    }
}
