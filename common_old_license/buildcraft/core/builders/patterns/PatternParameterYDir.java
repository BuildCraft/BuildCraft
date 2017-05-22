package buildcraft.core.builders.patterns;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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
    public PatternParameterYDir onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source, IStatement stmt) {
        return values();
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public TextureAtlasSprite getGuiSprite() {
        return (up ? BCCoreSprites.PARAM_STAIRS_UP : BCCoreSprites.PARAM_STAIRS_DOWN).getSprite();
    }
}
