/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.statements;

import java.util.Locale;

import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.BCTransportStatements;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionPipeDirection extends BCStatement implements IActionInternal {
    public final Direction direction;

    public ActionPipeDirection(Direction direction) {
        super("buildcraft:pipe.dir." + direction.name().toLowerCase(Locale.ROOT), "buildcraft.pipe.dir." + direction.name().toLowerCase(Locale.ROOT));
        this.direction = direction;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.pipe.direction", direction);//, ColourUtil.getTextFullTooltip(direction));
    }//TODO fina Direction Locale

    @Override
    public IStatement rotateLeft() {
        Direction face = direction.getAxis() == Axis.Y ? direction : direction.getClockWise();
        return BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public String toString() {
        return "ActionPipeDirection[" + direction + "]";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCTransportSprites.getPipeDirection(direction);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_DIRECTION;
    }
}
