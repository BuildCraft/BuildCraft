/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementMouseClick;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

/** Directions *might* be replaced with individual triggers and actions per direction. Not sure yet. */
@Deprecated
public class StatementParameterDirection implements IStatementParameter {

    @OnlyIn(Dist.CLIENT)
    private static ResourceLocation[] sprites;

    @Nullable
    private Direction direction = null;

    @OnlyIn(Dist.CLIENT)
    public void registerIcons(Pre map) {
        sprites = new ResourceLocation[] {
            (new ResourceLocation("buildcraftcore:triggers/trigger_dir_down")),
            (new ResourceLocation("buildcraftcore:triggers/trigger_dir_up")),
            (new ResourceLocation("buildcraftcore:triggers/trigger_dir_north")),
            (new ResourceLocation("buildcraftcore:triggers/trigger_dir_south")),
            (new ResourceLocation("buildcraftcore:triggers/trigger_dir_west")),
            (new ResourceLocation("buildcraftcore:triggers/trigger_dir_east"))
        };
        for(ResourceLocation r : sprites) {
        	map.addSprite(r);
        }
    }

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
            return Component.empty();
        } else {
            return Component.translatable("direction." + dir.name().toLowerCase());
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
