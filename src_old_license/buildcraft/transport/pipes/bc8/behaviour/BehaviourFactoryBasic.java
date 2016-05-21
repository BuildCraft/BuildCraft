package buildcraft.transport.pipes.bc8.behaviour;

import com.google.common.collect.ImmutableList;

import buildcraft.api.transport.pipe_bc8.IPipeBehaviourFactory;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeBehaviour_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;

public class BehaviourFactoryBasic implements IPipeBehaviourFactory {
    public static enum EnumListStatus {
        WHITELIST,
        BLACKLIST
    }

    protected PipeDefinition_BC8 definition;
    protected ImmutableList<PipeDefinition_BC8> connectionList = ImmutableList.of();
    protected EnumListStatus blacklist = EnumListStatus.BLACKLIST;

    public void setDefinition(PipeDefinition_BC8 definition, EnumListStatus blacklist, PipeDefinition_BC8... connectionList) {
        if (this.definition == null) {
            this.definition = definition;
            this.connectionList = ImmutableList.copyOf(connectionList);
            this.blacklist = blacklist;
        }
    }

    @Override
    public PipeBehaviour_BC8 createNew(IPipe_BC8 tile) {
        return new BehaviourBasic(definition, tile, connectionList, blacklist);
    }
}
