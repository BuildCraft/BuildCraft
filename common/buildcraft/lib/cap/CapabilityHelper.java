package buildcraft.lib.cap;

import buildcraft.api.core.EnumPipePart;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/** Provides a simple way of mapping {@link Capability}'s to instances. Also allows for additional providers */
public class CapabilityHelper implements ICapabilityProvider {
    private final Map<EnumPipePart, Map<Capability<?>, Supplier<?>>> caps = new EnumMap<>(EnumPipePart.class);

    private final List<ICapabilityProvider> additional = new ArrayList<>();

    public CapabilityHelper() {
        for (EnumPipePart face : EnumPipePart.VALUES) {
            caps.put(face, new HashMap<>());
        }
    }

    private Map<Capability<?>, Supplier<?>> getCapMap(Direction facing) {
        return caps.get(EnumPipePart.fromFacing(facing));
    }

    public <T> void addCapabilityInstance(@Nullable Capability<T> cap, T instance, EnumPipePart... parts) {
        Supplier<T> supplier = () -> instance;
        addCapability(cap, supplier, parts);
    }

    public <T> void addCapability(@Nullable Capability<T> cap, Supplier<T> getter, EnumPipePart... parts) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, getter);
        }
    }

    public <T> void addCapability(@Nullable Capability<T> cap, Function<Direction, T> getter, EnumPipePart... parts) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, () -> getter.apply(part.face));
        }
    }

    public <T extends ICapabilityProvider> T addProvider(T provider) {
        if (provider != null) {
            additional.add(provider);
        }
        return provider;
    }

    // 1.18.2: use getCapability().isPresent()
//    @Override
//    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
//        return getCapability(capability, facing) != null;
//    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@javax.annotation.Nonnull Capability<T> capability, Direction facing) {
        Map<Capability<?>, Supplier<?>> capMap = getCapMap(facing);
        Supplier<?> supplier = capMap.get(capability);
        if (supplier != null) {
            Object ret = supplier.get();
            return ret == null ? LazyOptional.empty() : LazyOptional.of(() -> (T) ret);
        }
        for (ICapabilityProvider provider : additional) {
//            if (provider.hasCapability(capability, facing))
            LazyOptional<T> result = provider.getCapability(capability, facing);
            if (result.isPresent()) {
                return result;
            }
        }
        return LazyOptional.empty();
    }
}
