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

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;

public abstract class ActionPowerLimit extends BCStatement implements IActionInternal {

    public final PipeDefinition pipe;
    public final int limitShift;

    public ActionPowerLimit(PipeDefinition pipe, int limitShift, String... uniqueTags) {
        super(uniqueTags);
        this.pipe = pipe;
        this.limitShift = limitShift;
    }

    public ActionPowerLimit(String suffix, PipeDefinition pipe, int limitShift) {
        this(pipe, limitShift, "buildcraft:pipe.power_limit." + suffix + "_s" + limitShift);
    }

    protected boolean isRf() {
        return false;
    }

    @Override
    public String getDescription() {
        return "gate.action.pipe." + (isRf() ? "rf" : "power") + "_limit.shift" + limitShift;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return null; // STUB(R.Chen): Phase 5 — BCTransportSprites.POWER_LIMIT[limitShift].
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public abstract IStatement[] getPossible();

    public static class ActionIronPowerLimit extends ActionPowerLimit {
        public ActionIronPowerLimit(int limitShift) {
            super("iron", BCTransportPipes.ironPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_POWER_LIMIT;
        }
    }

    public static class ActionDiamondPowerLimit extends ActionPowerLimit {
        public ActionDiamondPowerLimit(int limitShift) {
            super("diamond", BCTransportPipes.diamondPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT;
        }
    }

    public static class ActionIronRfLimit extends ActionPowerLimit {
        public ActionIronRfLimit(int limitShift) {
            super("iron_rf", BCTransportPipes.ironRf, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_RF_LIMIT;
        }

        @Override
        protected boolean isRf() {
            return true;
        }
    }

    public static class ActionDiamondRfLimit extends ActionPowerLimit {
        public ActionDiamondRfLimit(int limitShift) {
            super("diamond_rf", BCTransportPipes.diamondRf, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_RF_LIMIT;
        }

        @Override
        protected boolean isRf() {
            return true;
        }
    }
}
