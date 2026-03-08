/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class TriggerPower extends BCStatement implements ITriggerInternal, ITriggerExternal {
    private final boolean high;

    public TriggerPower(boolean high) {
        super("buildcraft:energyStored" + (high ? "high" : "low"));
        this.high = high;
    }

    @Override
    public SpriteHolder getSprite() {
        return high ? BCCoreSprites.TRIGGER_POWER_HIGH : BCCoreSprites.TRIGGER_POWER_LOW;
    }

    @Override
    public Component getDescription() {
//        return LocaleUtil.localize("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
        return new TranslatableComponent("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.machine.energyStored." + (high ? "high" : "low");
    }

    public boolean isTriggeredMjConnector(IMjReadable readable) {
        if (readable == null) {
            return false;
        }
        long stored = readable.getStored();
        long max = readable.getCapacity();

        if (max > 0) {
            double level = stored / (double) max;
            if (high) {
                return level > 0.95;
            } else {
                return level < 0.05;
            }
        }
        return false;
    }

    public static boolean isTriggeringTile(BlockEntity tile) {
        return isTriggeringTile(tile, null);
    }

    public static boolean isTriggeringTile(BlockEntity tile, Direction face) {
//        return tile.getCapability(MjAPI.CAP_READABLE, face) != null;
        return tile.getCapability(MjAPI.CAP_READABLE, face).isPresent();
    }

    protected boolean isActive(ICapabilityProvider tile, EnumPipePart side) {
        return isTriggeredMjConnector(tile.getCapability(MjAPI.CAP_READABLE, side.face).orElse(null));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return isActive(source.getTile(), EnumPipePart.CENTER);
    }

    @Override
    public boolean isTriggerActive(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        return isActive(target, EnumPipePart.fromFacing(side.getOpposite()));
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER;
    }
}
