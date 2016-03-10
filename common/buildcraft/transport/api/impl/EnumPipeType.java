package buildcraft.transport.api.impl;

import java.util.List;

import com.google.common.collect.Lists;

import buildcraft.api.transport.pipe_bc8.IPipeListener;
import buildcraft.api.transport.pipe_bc8.IPipeListenerFactory;
import buildcraft.api.transport.pipe_bc8.IPipeType;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.transport.pipes.bc8.PipeTransportItem_BC8;
import buildcraft.transport.pipes.bc8.PipeTransportPower_BC8;

public enum EnumPipeType implements IPipeType {
    ITEM(PipeTransportItem_BC8::new),
    POWER(PipeTransportPower_BC8::new),
    FLUID(null),
    STRUCTURE(null);

    /** An array of pipe types that carry something */
    public static final EnumPipeType[] CONTENTS = { ITEM, POWER, FLUID };

    private final IPipeListenerFactory factory;

    private EnumPipeType(IPipeListenerFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<IPipeListener> createDefaultListeners(IPipe_BC8 pipe) {
        return Lists.newArrayList(factory.createNewListener(pipe));
    }
}
