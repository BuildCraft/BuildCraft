package buildcraft.transport.api_move;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public final class PluggableDefinition {
    public final ResourceLocation identifier;
    public final IPluggableCreator pluggableConstructor;
    public final IPluggableLoader pluggableLoader;

    public PluggableDefinition(ResourceLocation identifier, IPluggableCreator pluggableConstructor, IPluggableLoader pluggableLoader) {
        this.identifier = identifier;
        this.pluggableConstructor = pluggableConstructor;
        this.pluggableLoader = pluggableLoader;
    }

    @FunctionalInterface
    public interface IPluggableCreator {
        PipePluggable createPluggable(IPipeHolder holder, EnumFacing side);
    }

    @FunctionalInterface
    public interface IPluggableLoader {
        PipePluggable loadPluggable(IPipeHolder holder, EnumFacing side, NBTTagCompound u);
    }
}
