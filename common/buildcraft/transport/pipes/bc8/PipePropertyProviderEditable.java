package buildcraft.transport.pipes.bc8;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

import io.netty.buffer.ByteBuf;

public class PipePropertyProviderEditable implements IPipePropertyProviderEditable {
    private final Map<IPipePropertyValue<?>, Object> values = Maps.newHashMap();
    // Ignore the generic type mismatch. Its horrible. TODO: Fix this
    private final Set valueSet = Collections.unmodifiableSet(values.keySet());
    private final IPipe_BC8 pipe;

    public PipePropertyProviderEditable() {
        this(null);
    }

    public PipePropertyProviderEditable(IPipe_BC8 pipe) {
        this.pipe = pipe;
    }

    @Override
    public <T> T getValue(IPipeProperty<T> property) {
        if (property instanceof IPipePropertyImplicit) {
            if (pipe != null) return ((IPipePropertyImplicit<T>) property).getValue(pipe);
            return property.getDefault();
        }
        if (values.containsKey(property)) return (T) values.get(property);
        return property.getDefault();
    }

    @Override
    public boolean hasProperty(IPipeProperty<?> property) {
        if (property instanceof IPipePropertyImplicit) return pipe != null;
        return values.containsKey(property);
    }

    @Override
    public Set<IPipeProperty<?>> getPropertySet() {
        return valueSet;
    }

    @Override
    public <T> void addProperty(IPipePropertyValue<T> property) {
        values.put(property, property.getDefault());
    }

    @Override
    public <T> void removeProperty(IPipePropertyValue<T> property) {
        values.remove(property);
    }

    @Override
    public IPipePropertyProvider asReadOnly() {
        return this;// FIXME: REPAIR THIS!
    }

    @Override
    public IPipePropertyProviderEditable readFromNBT(NBTBase nbt) {
        return this;
    }

    @Override
    public NBTBase writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (Entry<IPipePropertyValue<?>, Object> entry : values.entrySet()) {
            IPipePropertyValue<?> property = entry.getKey();
            String name = entry.getKey().getName();
            nbt.setTag(name, property.writeToNBT());
        }
        return nbt;
    }

    @Override
    public IPipePropertyProviderEditable readFromByteBuf(ByteBuf buf) {
        return this;
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {

    }
}
