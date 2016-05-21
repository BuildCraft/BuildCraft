package buildcraft.transport.pipes.bc8.behaviour;

import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

public class BehaviourFactoryPolishedStone extends BehaviourFactoryBasic {
    @Override
    public BehaviourPolishedStone createNew(IPipe_BC8 pipe) {
        return new BehaviourPolishedStone(definition, pipe, connectionList, blacklist);
    }
}
