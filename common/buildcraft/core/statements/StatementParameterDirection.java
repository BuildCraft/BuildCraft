/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

// Calen: never used in 1.12.2

/** Directions *might* be replaced with individual triggers and actions per direction. Not sure yet. */
//@Deprecated
@Deprecated(forRemoval = true)
public class StatementParameterDirection implements IStatementParameter {

    @OnlyIn(Dist.CLIENT)
    private static TextureAtlasSprite[] sprites;

    @Nullable
    private Direction direction = null;

//    @OnlyIn(Dist.CLIENT)
//    public void registerIcons(TextureAtlas map) {
//        sprites = new TextureAtlasSprite[] {
//            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_down")),
//            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_up")),
//            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_north")),
//            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_south")),
//            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_west")),
//            map.registerSprite(new ResourceLocation("buildcraftcore:triggers/trigger_dir_east"))
//        };
//    }

    public StatementParameterDirection() {

    }

    public StatementParameterDirection(Direction face) {
        this.direction = face;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        Direction dir = getDirection();
        if (dir == null) {
            return null;
        } else {
            return null;// sprites[dir.ordinal()];
        }
    }

    @Override
    public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        if (direction != null) {
            nbt.putByte("direction", (byte) direction.ordinal());
        }
    }

    //    @Override
    public void readFromNBT(CompoundTag nbt) {
        if (nbt.contains("direction")) {
            direction = Direction.values()[nbt.getByte("direction")];
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
    public Component getDescription() {
        Direction dir = getDirection();
        if (dir == null) {
            return new TextComponent("");
        } else {
            return new TranslatableComponent("direction." + dir.name().toLowerCase());
        }
    }

    @Override
    public String getDescriptionKey() {
        Direction dir = getDirection();
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
        Direction dir = d.getDirection();
        if (dir != null && dir.getAxis() != Axis.Y) {
//            d.direction = dir.rotateY();
            d.direction = dir.getClockWise();
        }
        return d;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
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
