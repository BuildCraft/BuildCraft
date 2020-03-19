package buildcraft.transport.statements;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowItems;

public class TriggerItemsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerItemsTraversing() {
        super("buildcraft:pipe_contains_items");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.containsItems");
    }

    @Override
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
                PipeFlowItems itemFlow = (PipeFlowItems) flow;

                ItemStack filter = getParam(0, parameters, StatementParameterItemStack.EMPTY).getItemStack();
                return itemFlow.containsItemMatching(filter);
            }
        }
        return false;
    }
}
