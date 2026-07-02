package ct.buildcraft.api.transport.pipe;

import net.minecraft.nbt.CompoundTag;

public final class PipeFlowType {
    public final IFlowCreator creator;
    public final IFlowLoader loader;

    /** The default colour type, if none is given in {@link PipeDefinition}. if this is also null then the final
     * fallback type is {@link EnumPipeColourType#TRANSLUCENT}. */
    public EnumPipeColourType fallbackColourType;

    public PipeFlowType(IFlowCreator creator, IFlowLoader loader) {
        this(creator, loader, null);
    }

    public PipeFlowType(IFlowCreator creator, IFlowLoader loader, EnumPipeColourType colourType) {
        this.creator = creator;
        this.loader = loader;
        this.fallbackColourType = colourType;
    }

    @FunctionalInterface
    public interface IFlowCreator {
        PipeFlow createFlow(IPipe t);
    }

    @FunctionalInterface
    public interface IFlowLoader {
        PipeFlow loadFlow(IPipe t, CompoundTag u);
    }
}
