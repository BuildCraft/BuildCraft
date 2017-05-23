/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nonnull;
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

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

/** Directions *might* be replaced with indervidual triggers and actions per direction. Not sure yet. */
@Deprecated
public class StatementParameterDirection implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] sprites;

    @Nullable
    private EnumFacing direction = null;

    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap map) {
        sprites = new TextureAtlasSprite[] {
            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_down")),
            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_up")),
            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_north")),
            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_south")),
            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_west")),
            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_east"))
        };
    }

    public StatementParameterDirection() {

    }

    public StatementParameterDirection(EnumFacing face) {
        this.direction = face;
    }

    @Nullable
    public EnumFacing getDirection() {
        return direction;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
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
    public boolean onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return false;
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
            return LocaleUtil.localize("direction." + dir.name().toLowerCase());
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

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source, IStatement stmt) {
        IStatementParameter[] possible = new IStatementParameter[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            if (part.face == direction) {
                possible[part.getIndex()] = this;
            } else {
                possible[part.getIndex()] = new StatementParameterDirection(part.face);
            }
        }
        return possible;
    }
}
