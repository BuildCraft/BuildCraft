/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerLightSensor extends BCStatement implements ITriggerInternalSided {
    private final boolean bright;

    public TriggerLightSensor(boolean bright) {
        super("buildcraft:light_" + (bright ? "bright" : "dark"));
        this.bright = bright;
    }

    @Override
    public Component getDescription() {
//        return LocaleUtil.localize("gate.trigger.light." + (bright ? "bright" : "dark"));
        return Component.translatable("gate.trigger.light." + (bright ? "bright" : "dark"));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.light." + (bright ? "bright" : "dark");
    }

    @Override
    public boolean isTriggerActive(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        BlockEntity tile = source.getTile();
        BlockPos pos = tile.getBlockPos().relative(side);
//        int light = tile.getLevel().getLightFromNeighbors(pos);
        Level world = tile.getLevel();
        int light = world.getBrightness(LightLayer.SKY, pos) + world.getBrightness(LightLayer.BLOCK, pos) - world.getSkyDarken(); // DaylightDetectorBlock#updateSignalStrength()
        return (light < 8) ^ bright;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_LIGHT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return bright ? BCSiliconSprites.TRIGGER_LIGHT_HIGH : BCSiliconSprites.TRIGGER_LIGHT_LOW;
    }
}
