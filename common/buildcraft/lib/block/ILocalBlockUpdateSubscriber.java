package buildcraft.lib.block;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILocalBlockUpdateSubscriber {

    /**
     * Returns the position of the subscriber. Used with the result of @{getUpdateRange} to determine if a subscriber
     * should be notified about an update
     *
     * @return the @{BlockPos} used to determine if a block update event is in range
     */
    BlockPos getSubscriberPos();

    /**
     * The distance from the @{BlockPos} that subscribers should be notified about updates. This method should be kept
     * lightweight as it can be called multiple times per tick.
     *
     * @return the range from the @{BlockPos} returned by @{getSubscriberPos} where block update events will trigger a 
     * notification
     */
    int getUpdateRange();

    /**
     * Called to indicate an update happened within the listener's update range returned by the @{getUpdateRange} call.
     * This method should be kept lightweight as it can be called multiple times per tick.
     *
     * @param world from the block update event
     * @param eventPos from the block update event
     * @param oldState from the block update event
     * @param newState from the block update event
     * @param flags from the block update event
     */
    void setWorldUpdated(@Nonnull World world, @Nonnull BlockPos eventPos, @Nonnull IBlockState oldState,
                         @Nonnull IBlockState newState, int flags);
}
