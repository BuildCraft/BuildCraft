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

import net.minecraft.item.ItemStack;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowItems;

public class TriggerItemsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerItemsTraversing() {
        super("buildcraft:pipe_contains_items");
    }

    @Override
    public String getDescription() {
        return "gate.trigger.pipe.containsItems";
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_ITEMS_TRAVERSING;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return StatementParameterItemStack.EMPTY;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            PipeFlow flow = ((IGate) source).getPipeHolder().getPipe().getFlow();
            if (flow instanceof PipeFlowItems) {
                ItemStack filter = getParam(0, parameters, StatementParameterItemStack.EMPTY).getItemStack();
                return ((PipeFlowItems) flow).containsItemMatching(filter);
            }
        }
        return false;
    }
}
