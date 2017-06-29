/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;

public class TriggerRedstoneInput extends BCStatement implements ITriggerInternal {

    boolean active;

    public TriggerRedstoneInput(boolean active) {
        super("buildcraft:redstone.input." + (active ? "active" : "inactive"),//
                "buildcraft.redtone.input." + (active ? "active" : "inactive"));
        this.active = active;
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return active ? BCCoreSprites.TRIGGER_REDSTONE_ACTIVE : BCCoreSprites.TRIGGER_REDSTONE_INACTIVE;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
    }

    @Override
    public IStatementParameter createParameter(int index) {
        IStatementParameter param = null;

        if (index == 0) {
            param = new StatementParamGateSideOnly();
        }

        return param;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (container instanceof IRedstoneStatementContainer) {
            int level = ((IRedstoneStatementContainer) container).getRedstoneInput(null);
            if (parameters.length > 0 && parameters[0] instanceof StatementParamGateSideOnly && ((StatementParamGateSideOnly) parameters[0]).isOn && container instanceof ISidedStatementContainer) {
                level = ((IRedstoneStatementContainer) container).getRedstoneInput(((ISidedStatementContainer) container).getSide());
            }

            return active ? level > 0 : level == 0;
        } else {
            return false;
        }
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_REDSTONE;
    }
}
