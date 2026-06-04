/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.statement;

import java.util.Locale;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.world.World;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;

public class TriggerTimer extends BCStatement implements ITriggerInternal {

    public enum Duration {
        SHORT(5),
        MEDIUM(10),
        LONG(15);

        public final int duration;

        private Duration(int duration) {
            this.duration = duration;
        }
    }

    private final Duration duration;

    public TriggerTimer(Duration duration) {
        super("buildcraft:timer_" + duration.name().toLowerCase(Locale.ROOT));
        this.duration = duration;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.timer", duration.duration);
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        World world = source.getTile().getWorld();
        return world.getTime() % (20 * duration.duration) == 0;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_TIMER;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public SpriteHolder getSprite() {
        switch (duration) {
            case SHORT: {
                return BCSiliconSprites.TRIGGER_TIMER_SHORT;
            }
            case MEDIUM: {
                return BCSiliconSprites.TRIGGER_TIMER_MEDIUM;
            }
            case LONG:
            default: {
                return BCSiliconSprites.TRIGGER_TIMER_LONG;
            }
        }
    }
}
