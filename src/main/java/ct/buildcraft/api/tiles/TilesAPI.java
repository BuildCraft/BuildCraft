package ct.buildcraft.api.tiles;

import javax.annotation.Nonnull;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class TilesAPI {
    @Nonnull
    public static final Capability<IControllable> CAP_CONTROLLABLE;

    @Nonnull
    public static final Capability<IHasWork> CAP_HAS_WORK;

    @Nonnull
    public static final Capability<IHeatable> CAP_HEATABLE;

    @Nonnull
    public static final Capability<ITileAreaProvider> CAP_TILE_AREA_PROVIDER;

    static {
        CAP_CONTROLLABLE = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_HAS_WORK = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_HEATABLE = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_TILE_AREA_PROVIDER = CapabilityManager.get(new CapabilityToken<>(){});
    }
}
