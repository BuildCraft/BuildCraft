package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import buildcraft.api.core.SafeTimeTracker;

import buildcraft.lib.world.WorldEventListenerAdapter;

/**
 * Listens for block updates and collects all the updates within a tick to be available at once
 */
public class BlockUpdateCollector {

    private static Map<World, BlockUpdateCollector> instanceMap = new HashMap<>();
    private List<UpdateRecord> updatesSinceLastTick;
    private List<UpdateRecord> updatesInLastTick;
    private final SafeTimeTracker tickDetector = new SafeTimeTracker(1);
    private World world;


    private BlockUpdateCollector(World world) {
        updatesInLastTick = new ArrayList<>();
        updatesSinceLastTick = new ArrayList<>();
        this.world = world;

        IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
            @Override
            public void notifyBlockUpdate(@Nonnull World world, @Nonnull BlockPos eventPos, @Nonnull IBlockState oldState,
                                          @Nonnull IBlockState newState, int flags) {
                updatesSinceLastTick.add(new UpdateRecord(oldState, newState, eventPos));
            }
        };
        this.world.addEventListener(worldEventListener);
    }

    public static BlockUpdateCollector instance(World world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new BlockUpdateCollector(world));
        }
        return instanceMap.get(world);
    }

    /**
     * Returns a list of all the block updates that took place in the previous tick
     *
     * @return List<UpdateRecord></UpdateRecord>
     */
    public List<UpdateRecord> getUpdtaesInLastTick() {
        if (tickDetector.markTimeIfDelay(world)) {
            updatesInLastTick = updatesSinceLastTick;
            updatesSinceLastTick = new ArrayList<>();
        }
        return updatesInLastTick;
    }

    public class UpdateRecord {
        public IBlockState oldState;
        public IBlockState newState;
        public BlockPos position;

        public UpdateRecord(IBlockState oldState, IBlockState newState, BlockPos position) {
            this.oldState = oldState;
            this.newState = newState;
            this.position = position;
        }

    }
}
