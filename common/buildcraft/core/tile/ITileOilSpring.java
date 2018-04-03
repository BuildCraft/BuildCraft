package buildcraft.core.tile;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.math.BlockPos;

import buildcraft.energy.tile.TileSpringOil;

/** Implemented by {@link TileSpringOil} in the energy module. */
public interface ITileOilSpring {

    /** Pumps should call this when they pump oil from this spring. */
    void onPumpOil(GameProfile pumpOwner, BlockPos oilPos);

}
