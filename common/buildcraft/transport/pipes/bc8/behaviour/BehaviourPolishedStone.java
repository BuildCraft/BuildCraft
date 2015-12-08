package buildcraft.transport.pipes.bc8.behaviour;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.transport.pipes.bc8.behaviour.BehaviourFactoryBasic.EnumListStatus;

public class BehaviourPolishedStone extends BehaviourBasic {
    public BehaviourPolishedStone(PipeDefinition_BC8 definition, IPipe_BC8 pipe, ImmutableList<PipeDefinition_BC8> connectionList,
            EnumListStatus blacklist) {
        super(definition, pipe, connectionList, blacklist);
    }

    @Subscribe
    public void connectBlockEvent(IPipeEventConnection_BC8.AttemptCreate.Tile connect) {
        connect.disallow();
    }
}
