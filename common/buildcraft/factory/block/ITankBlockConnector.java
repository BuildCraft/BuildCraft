package buildcraft.factory.block;

import buildcraft.factory.tile.TileTank;

/** Marker interface for {@link BlockTank} to determine if the block below should be considered a tank block when
 * visually connecting below. This should only really be implemented when the block creates an instance of
 * {@link TileTank}. */
public interface ITankBlockConnector {
}
