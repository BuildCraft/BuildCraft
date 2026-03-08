package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

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
        if (f instanceof PipeFlowPower) {
            return ((PipeFlowPower) f).getPowerRequested(null) > 0;
        } else if (f instanceof PipeFlowRedstoneFlux) {
            return ((PipeFlowRedstoneFlux) f).getPowerRequested(null) > 0;
        } else {
            return false;
        }
    }

    @Override
    public Component getDescription() {
//        return LocaleUtil.localize("gate.trigger.pipe.requestsEnergy");
        return new TranslatableComponent("gate.trigger.pipe.requestsEnergy");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.pipe.requestsEnergy";
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_POWER_REQUESTED;
    }

}
