package buildcraft.transport.pipes.bc8.behaviour;

import buildcraft.api.transport.pipe_bc8.IPipeBehaviourFactory;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeBehaviour_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;

public class BehaviourFactoryWooden implements IPipeBehaviourFactory {
    private PipeDefinition_BC8 definition;

    public void setDefinition(PipeDefinition_BC8 definition) {
        if (this.definition == null) {
            this.definition = definition;
        }
    }

    @Override
    public PipeBehaviour_BC8 createNew(IPipe_BC8 pipe) {
        return new BehaviourWood(definition, pipe);
    }
}
