package ct.buildcraft.transport.statements;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.gates.IGate;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.statements.StatementParameterItemStack;
import ct.buildcraft.api.transport.pipe.PipeFlow;

import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.pipe.flow.PipeFlowItems;

public class TriggerItemsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerItemsTraversing() {
        super("buildcraft:pipe_contains_items");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.pipe.containsItems");
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
