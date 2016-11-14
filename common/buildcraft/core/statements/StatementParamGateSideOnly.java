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

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StringUtilBC;

public class StatementParamGateSideOnly implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    public static SpriteHolder sprite;

    public boolean isOn = false;

    public StatementParamGateSideOnly() {

    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public TextureAtlasSprite getGuiSprite() {
        if (!isOn) {
            return null;
        } else {
            return sprite.getSprite();
        }
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        isOn = !isOn;
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
        return isOn ? StringUtilBC.localize("gate.parameter.redstone.gateSideOnly") : "";
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:redstoneGateSideOnly";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }
}
