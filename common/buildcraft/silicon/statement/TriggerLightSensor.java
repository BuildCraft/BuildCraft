/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.statement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternalSided;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;

public class TriggerLightSensor extends BCStatement implements ITriggerInternalSided {
    private final boolean bright;

    public TriggerLightSensor(boolean bright) {
        super("buildcraft:light_" + (bright ? "bright" : "dark"));
        this.bright = bright;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.light." + (bright ? "bright" : "dark"));
    }

    @Override
    public boolean isTriggerActive(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        BlockEntity tile = source.getTile();
        BlockPos pos = tile.getPos().offset(side);
        int light = tile.getWorld().getLightLevel(pos);
        return (light < 8) ^ bright;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_LIGHT;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public SpriteHolder getSprite() {
        return bright ? BCSiliconSprites.TRIGGER_LIGHT_HIGH : BCSiliconSprites.TRIGGER_LIGHT_LOW;
    }
}
