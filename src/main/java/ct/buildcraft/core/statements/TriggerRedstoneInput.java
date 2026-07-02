/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.statements.containers.IRedstoneStatementContainer;
import ct.buildcraft.api.statements.containers.ISidedStatementContainer;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.core.BCCoreStatements;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import net.minecraft.network.chat.Component;

public class TriggerRedstoneInput extends BCStatement implements ITriggerInternal {
    public final boolean active;

    public TriggerRedstoneInput(boolean active) {
        super("buildcraft:redstone.input." + (active ? "active" : "inactive"), //
            "buildcraft.redstone.input." + (active ? "active" : "inactive"));
        this.active = active;
    }

    @Override
    public SpriteHolder getSprite() {
        return active ? BCCoreSprites.TRIGGER_REDSTONE_ACTIVE : BCCoreSprites.TRIGGER_REDSTONE_INACTIVE;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
    }

    @Override
    public IStatementParameter createParameter(int index) {
        switch (index) {
            case 0:
                return StatementParamGateSideOnly.ANY;
            default:
                return null;
        }
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (container instanceof IRedstoneStatementContainer) {
            int level = ((IRedstoneStatementContainer) container).getRedstoneInput(null);
            if (parameters.length > 0 && parameters[0] instanceof StatementParamGateSideOnly
                && ((StatementParamGateSideOnly) parameters[0]).isSpecific
                && container instanceof ISidedStatementContainer) {
                level = ((IRedstoneStatementContainer) container)
                    .getRedstoneInput(((ISidedStatementContainer) container).getSide());
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

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T obj = super.convertTo(clazz);
        if (obj != null) {
            return obj;
        }

        if (active) {
            if (clazz.isInstance(BCCoreStatements.ACTION_REDSTONE)) {
                return clazz.cast(BCCoreStatements.ACTION_REDSTONE);
            }
        }

        return null;
    }
}
