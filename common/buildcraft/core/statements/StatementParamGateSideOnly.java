package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.core.BCCoreSprites;

public enum StatementParamGateSideOnly implements IStatementParameter {
    ANY(false),
    SPECIFIC(true);

    public final boolean isSpecific;

    private static final StatementParamGateSideOnly[] POSSIBLE_ANY = { ANY, SPECIFIC };
    private static final StatementParamGateSideOnly[] POSSIBLE_SPECIFIC = { SPECIFIC, ANY };

    StatementParamGateSideOnly(boolean isSpecific) {
        this.isSpecific = isSpecific;
    }

    public static StatementParamGateSideOnly readFromNbt(NBTTagCompound nbt) {
        if (nbt.getBoolean("isOn")) {
            return SPECIFIC;
        }
        return ANY;
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        compound.setBoolean("isOn", isSpecific);
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        if (isSpecific) {
            return BCCoreSprites.PARAM_GATE_SIDE_ONLY.getSprite();
        } else {
            return null;
        }
    }

    @Override
    public StatementParamGateSideOnly onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public String getDescription() {
        return isSpecific ? LocaleUtil.localize("gate.parameter.redstone.gateSideOnly") : "";
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:redstoneGateSideOnly";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source, IStatement stmt) {
        return isSpecific ? POSSIBLE_SPECIFIC : POSSIBLE_ANY;
    }
}
