package ct.buildcraft.api.facades;

import javax.annotation.Nullable;

import net.minecraft.world.item.DyeColor;

public interface IFacadePhasedState {
    IFacadeState getState();

    @Nullable
    DyeColor getActiveColor();
}
