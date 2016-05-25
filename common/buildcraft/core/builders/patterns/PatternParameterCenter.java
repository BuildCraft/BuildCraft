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
import buildcraft.core.lib.utils.BCStringUtils;

public class PatternParameterCenter implements IStatementParameter {
    private static final int[] shiftLeft = { 6, 3, 0, 7, 4, 1, 8, 5, 2 };

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] sprites;

    private int direction;

    public static void registerSprites(TextureMap map) {
        sprites = new TextureAtlasSprite[9];
        for (int i = 0; i < 9; i++) {
            sprites[i] = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/center_" + i));
        }
    }

    public PatternParameterCenter() {
        super();
    }

    public PatternParameterCenter(int direction) {
        this();
        this.direction = direction;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterCenter";
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return sprites[direction % 9];
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public String getDescription() {
        return BCStringUtils.localize("direction.center." + direction);
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        direction = (direction + 1) % 9;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        direction = compound.getByte("dir");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setByte("dir", (byte) direction);
    }

    @Override
    public IStatementParameter rotateLeft() {
        return new PatternParameterCenter(shiftLeft[direction % 9]);
    }

    public int getDirection() {
        return direction;
    }
}
