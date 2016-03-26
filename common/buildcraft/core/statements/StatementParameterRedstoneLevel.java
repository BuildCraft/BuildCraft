/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Objects;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

public class StatementParameterRedstoneLevel implements IStatementParameter {
    private static TextureAtlasSprite[] sprites;
    public int level = 0;
    public int minLevel = 0, maxLevel = 15;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap map) {
        sprites = new TextureAtlasSprite[16];
        for (int i = 0; i < 16; i++) {
            sprites[i] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/parameter_redstone_" + i));
        }
    }

    public StatementParameterRedstoneLevel() {

    }

    public StatementParameterRedstoneLevel(int min, int max) {
        minLevel = min;
        maxLevel = max;
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return sprites[level & 15];
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (mouse.getButton() == 0) {
            level = (level + 1) & 15;
            while (level < minLevel || level > maxLevel) {
                level = (level + 1) & 15;
            }
        } else {
            level = (level - 1) & 15;
            while (level < minLevel || level > maxLevel) {
                level = (level - 1) & 15;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setByte("l", (byte) level);
        nbt.setByte("mi", (byte) minLevel);
        nbt.setByte("ma", (byte) maxLevel);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        level = nbt.hasKey("l") ? nbt.getByte("l") : 15;
        minLevel = nbt.hasKey("mi") ? nbt.getByte("mi") : 0;
        maxLevel = nbt.hasKey("ma") ? nbt.getByte("ma") : 15;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterRedstoneLevel) {
            StatementParameterRedstoneLevel param = (StatementParameterRedstoneLevel) object;
            return param.level == this.level;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

    @Override
    public String getDescription() {
        return String.format(StatCollector.translateToLocal("gate.trigger.redstone.input.level"), level);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:redstoneLevel";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }
}
