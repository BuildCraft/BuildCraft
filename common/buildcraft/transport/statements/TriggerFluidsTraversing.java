package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TriggerFluidsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerFluidsTraversing() {
        super("buildcraft:pipe_contains_fluids");
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.trigger.pipe.containsFluids");
        return new TranslationTextComponent("gate.trigger.pipe.containsFluids");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.pipe.containsFluids";
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_FLUIDS_TRAVERSING;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            PipeFlow flow = ((IGate) source).getPipeHolder().getPipe().getFlow();
            if (flow instanceof PipeFlowFluids) {
                return ((PipeFlowFluids) flow).doesContainFluid();
            }
        }
        return false;
    }
}
