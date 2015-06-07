package buildcraft.core.builders.patterns;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.lib.utils.StringUtils;

public class PatternParameterYDir implements IStatementParameter {
    public boolean up = false;

    public PatternParameterYDir() {
        super();
    }

    public PatternParameterYDir(boolean up) {
        this();
        this.up = up;
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
        return StringUtils.localize("direction." + (up ? "up" : "down"));
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
        return null;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        // return Minecraft.getMinecraft().getTextureManager().getTexture(new ResourceLocation("TODO"));
        return null;
    }
}
