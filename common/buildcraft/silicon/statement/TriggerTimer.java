/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

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
    public ITextComponent getDescription() {
        // return LocaleUtil.localize("gate.trigger.timer", duration.duration);
        return new TranslationTextComponent("gate.trigger.timer", duration.duration);
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.timer." + duration.duration;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        World world = source.getTile().getLevel();
        // return world.getTotalWorldTime() % (20 * duration.duration) == 0;
        return world.getGameTime() % (20 * duration.duration) == 0;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_TIMER;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
