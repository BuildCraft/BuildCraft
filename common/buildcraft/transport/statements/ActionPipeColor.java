/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.statements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.DyeColor;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportStatements;

public class ActionPipeColor extends BCStatement implements IActionInternal {

    public final DyeColor color;

    public ActionPipeColor(DyeColor color) {
        super("buildcraft:pipe.color." + color.getName(), "buildcraft.pipe." + color.getName());
        this.color = color;
    }

    @Override
    public String getDescription() {
        return "gate.action.pipe.item.color." + color.getName();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_COLOUR;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return null; // STUB(R.Chen): Phase 5 — BCTransportSprites.ACTION_PIPE_COLOUR.
    }
}
