package ct.buildcraft.transport.statements;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.gates.IGate;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.transport.pipe.PipeFlow;

import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower;

import net.minecraft.network.chat.Component;

public class TriggerPowerRequested extends BCStatement implements ITriggerInternal {

    public TriggerPowerRequested() {
        super("buildcraft:powerRequested");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (!(source instanceof IGate)) {
            return false;
        }
        PipeFlow f = ((IGate) source).getPipeHolder().getPipe().getFlow();
        if (!(f instanceof PipeFlowPower)) {
            return false;
        }
        final PipeFlowPower flow = (PipeFlowPower) f;

        return flow.getPowerRequested(null) > 0;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.pipe.requestsEnergy");
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_POWER_REQUESTED;
    }

}
