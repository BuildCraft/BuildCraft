package buildcraft.lib.cap;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import buildcraft.api.core.EnumPipePart;

/** Provides a simple way of mapping {@link Capability}'s to instances. Also allows for additional providers */
public class CapabilityHelper implements ICapabilityProvider {
    private final Map<EnumPipePart, Map<Capability<?>, Object>> caps = new EnumMap<>(EnumPipePart.class);
    private final List<ICapabilityProvider> additional = new ArrayList<>();

    public CapabilityHelper() {
        for (EnumPipePart face : EnumPipePart.VALUES) {
            caps.put(face, new HashMap<>());
        }
    }

    private Map<Capability<?>, Object> getCapMap(EnumFacing facing) {
        return caps.get(EnumPipePart.fromFacing(facing));
    }

    public <T> void addCapability(Capability<T> cap, T instance, EnumFacing[] faces) {
        if (cap == null) {
            return;
        }
        for (EnumFacing face : faces) {
            getCapMap(face).put(cap, instance);
        }
    }

    public <T> void addCapability(Capability<T> cap, Supplier<T> supplier, EnumFacing[] faces) {
        if (cap == null) {
            return;
        }
        addCapability(cap, supplier.get(), faces);
    }

    public void addProvider(ICapabilityProvider provider) {
        if (provider != null) {
            additional.add(provider);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == null) {
            return false;
        }
        if (getCapMap(facing).containsKey(capability)) return true;
        for (ICapabilityProvider provider : additional) {
            if (provider.hasCapability(capability, facing)) return true;
        }
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == null) {
            return null;
        }
        if (getCapMap(facing).containsKey(capability)) {
            return (T) getCapMap(facing).get(capability);
        }
        for (ICapabilityProvider provider : additional) {
            if (provider.hasCapability(capability, facing)) {
                return provider.getCapability(capability, facing);
            }
        }
        return null;
    }
}
