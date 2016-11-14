/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.transport.IPipeTile;

import buildcraft.lib.misc.StringUtilBC;

/** Directions *might* be replaced with indervidual triggers and actions per direction. Not sure yet. */
@Deprecated
public class StatementParameterDirection implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] sprites;

    @Nullable
    private EnumFacing direction = null;

    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap map) {
        sprites = new TextureAtlasSprite[6];
        sprites[0] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_down"));
        sprites[1] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_up"));
        sprites[2] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_north"));
        sprites[3] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_south"));
        sprites[4] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_west"));
        sprites[5] = map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_east"));
    }

    public StatementParameterDirection() {

    }

    @Nullable
    public EnumFacing getDirection() {
        return direction;
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        EnumFacing dir = getDirection();
        if (dir == null) {
            return null;
        } else {
            return null;// sprites[dir.ordinal()];
        }
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        EnumFacing direction = getDirection();
        if (source.getTile() instanceof IPipeTile) {
            for (int i = 0; i < 6; i++) {
                int ord;
                if (direction == null) {
                    ord = 0;
                } else {
                    ord = direction.ordinal() + (mouse.getButton() > 0 ? -1 : 1);
                }
                direction = EnumFacing.VALUES[ord % 6];
                if (((IPipeTile) source.getTile()).isPipeConnected(getDirection())) {
                    return;
                }
            }
            direction = null;
        }
        this.direction = direction;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (direction != null) {
            nbt.setByte("direction", (byte) direction.ordinal());
        }
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
            return param.getDirection() == this.getDirection();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirection());
    }

    @Override
    public String getDescription() {
        EnumFacing dir = getDirection();
        if (dir == null) {
            return "";
        } else {
            return StringUtilBC.localize("direction." + dir.name().toLowerCase());
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeActionDirection";
    }

    @Override
    public IStatementParameter rotateLeft() {
        StatementParameterDirection d = new StatementParameterDirection();
        EnumFacing dir = d.getDirection();
        if (dir != null && dir.getAxis() != Axis.Y) {
            d.direction = dir.rotateY();
        }
        return d;
    }
}
