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

public class PatternParameterXZDir implements IStatementParameter {
    private static final String[] names = { "west", "east", "north", "south" };
    private static final int[] shiftLeft = { 3, 2, 0, 1 };
    private static final int[] shiftRight = { 2, 3, 1, 0 };

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] sprites;
    private int direction;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap map) {
        sprites = new TextureAtlasSprite[4];
        sprites[0] = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/arrow_left"));
        sprites[1] = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/arrow_right"));
        sprites[2] = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/arrow_up"));
        sprites[3] = map.registerSprite(new ResourceLocation("buildcraftcore:filler/parameters/arrow_down"));
    }

    public PatternParameterXZDir() {
        super();
    }

    public PatternParameterXZDir(int direction) {
        this();
        this.direction = direction;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterXZDir";
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return sprites[direction & 3];
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("direction." + names[direction & 3]);
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        direction = shiftRight[direction & 3];
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
        return new PatternParameterXZDir(shiftLeft[direction & 3]);
    }

    public int getDirection() {
        return direction;
    }
}
