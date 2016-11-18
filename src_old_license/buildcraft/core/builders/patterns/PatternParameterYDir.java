package buildcraft.core.builders.patterns;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.StringUtilBC;

public class PatternParameterYDir implements IStatementParameter {
    private static TextureAtlasSprite spriteUp, spriteDown;

    public boolean up = false;

    public PatternParameterYDir() {
        super();
    }

    public PatternParameterYDir(boolean up) {
        this();
        this.up = up;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap map) {
        spriteUp = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/stairs_ascend"));
        spriteDown = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/stairs_descend"));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterYDir";
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("direction." + (up ? "up" : "down"));
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        up = !up;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        up = compound.getBoolean("up");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("up", up);
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public TextureAtlasSprite getGuiSprite() {
        return up ? spriteUp : spriteDown;
    }
}
