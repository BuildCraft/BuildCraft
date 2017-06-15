/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternalSided;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;

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
    public boolean isTriggerActive(EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        TileEntity tile = source.getTile();
        BlockPos pos = tile.getPos().offset(side);
        int light = tile.getWorld().getLightFromNeighbors(pos);
        return (light < 8) ^ bright;
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.TRIGGER_LIGHT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSprite() {
        return bright ? BCTransportSprites.TRIGGER_LIGHT_HIGH : BCTransportSprites.TRIGGER_LIGHT_LOW;
    }
}
