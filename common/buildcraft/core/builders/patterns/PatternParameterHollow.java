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
import buildcraft.core.lib.utils.StringUtils;

public class PatternParameterHollow implements IStatementParameter {
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite iconHollow, iconFilled;

    public boolean filled = false;

    public PatternParameterHollow() {
        super();
    }

    public PatternParameterHollow(boolean hollow) {
        this();
        this.filled = !hollow;
    }

    @SideOnly(Side.CLIENT)
    public static void registerSprites(TextureMap map ) {
        iconFilled = map.registerSprite(new ResourceLocation("buildcraftcore:fillerParameters/filled"));
        iconHollow = map.registerSprite(new ResourceLocation("buildcraftcore:fillerParameters/hollow"));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterHollow";
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return filled ? iconFilled : iconHollow;
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("fillerpattern.parameter." + (filled ? "filled" : "hollow"));
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        filled = !filled;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        filled = compound.getBoolean("filled");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("filled", filled);
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }
}
