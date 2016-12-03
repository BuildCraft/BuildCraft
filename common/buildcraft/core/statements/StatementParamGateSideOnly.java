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

import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

public class StatementParamGateSideOnly implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    public static SpriteHolder sprite;

    public boolean isOn = false;

    public StatementParamGateSideOnly() {

    }

    StatementParamGateSideOnly(boolean def) {
        isOn = def;
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        if (isOn) {
            return BCCoreSprites.PARAM_GATE_SIDE_ONLY.getSprite();
        } else {
            return null;
        }
    }

    @Override
    public boolean onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setByte("isOn", isOn ? (byte) 1 : (byte) 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("isOn")) {
            isOn = compound.getByte("isOn") == 1;
        }
    }

    @Override
    public String getDescription() {
        return isOn ? LocaleUtil.localize("gate.parameter.redstone.gateSideOnly") : "";
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
        IStatementParameter[] possible = new IStatementParameter[2];
        possible[0] = isOn ? this : new StatementParamGateSideOnly(true);
        possible[1] = !isOn ? this : new StatementParamGateSideOnly(false);
        return possible;
    }
}
