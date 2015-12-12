/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.utils.BCStringUtils;

public class StatementParameterDirection implements IStatementParameter {

    private static TextureAtlasSprite[] sprites;

    public EnumFacing direction = null;

    public static void registerIcons(TextureMap map) {
        sprites = new TextureAtlasSprite[5];
        sprites[0] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_down"));
        sprites[1] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_up"));
        sprites[2] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_north"));
        sprites[3] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_south"));
        sprites[4] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_west"));
        sprites[5] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_east"));
    }

    public StatementParameterDirection() {

    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (direction == null) {
            return null;
        } else {
            return sprites[direction.ordinal()];
        }
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (source.getTile() instanceof IPipeTile) {
            for (int i = 0; i < 6; i++) {
                direction = EnumFacing.VALUES[(direction.ordinal() + (mouse.getButton() > 0 ? -1 : 1)) % 6];
                if (((IPipeTile) source.getTile()).isPipeConnected(direction)) {
                    return;
                }
            }
            direction = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setByte("direction", (byte) direction.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("direction")) {
            direction = EnumFacing.VALUES[nbt.getByte("direction")];
        } else {
            direction = null;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterDirection) {
            StatementParameterDirection param = (StatementParameterDirection) object;
            return param.direction == this.direction;
        }
        return false;
    }

    @Override
    public String getDescription() {
        if (direction == null) {
            return "";
        } else {
            return BCStringUtils.localize("direction." + direction.name().toLowerCase());
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeActionDirection";
    }

    @Override
    public IStatementParameter rotateLeft() {
        StatementParameterDirection d = new StatementParameterDirection();
        if (d.direction != null && d.direction.getAxis() != Axis.Y) {
            d.direction = d.direction.rotateY();
        }
        return d;
    }
}
