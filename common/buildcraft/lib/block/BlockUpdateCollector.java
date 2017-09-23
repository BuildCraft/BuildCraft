package buildcraft.lib.block;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import buildcraft.lib.world.WorldEventListenerAdapter;

import buildcraft.silicon.tile.TileLaser;

/**
 * Listens for BlockUpdates in a given world and notifies all registered TileLasers of the update provided it was near
 * the TileLaser
 */
public class BlockUpdateCollector {

    private static Map<World, BlockUpdateCollector> instanceMap = new WeakHashMap<>();
    private World world;
    private Map<BlockPos, TileLaser> laserMap = new HashMap<>();


    private BlockUpdateCollector(World world) {
        this.world = world;

        IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
            @Override
            public void notifyBlockUpdate(@Nonnull World world, @Nonnull BlockPos eventPos, @Nonnull IBlockState oldState,
                                          @Nonnull IBlockState newState, int flags) {
                //TODO remove profiling code
                long startMillis = System.nanoTime();

                notifyNearbyLasers(eventPos);

                //TODO remove this
                long endMillis = System.nanoTime();
                long duration = endMillis - startMillis;
                TileLaser.totalScanTime += duration;
            }
        };
        this.world.addEventListener(worldEventListener);
    }

    /**
     * Gets the BlockUpdateCollector for the given world
     *
     * @param world the World where BlockUpdate events will be listened for
     * @return the instance of BlockUpdateCollector for the given world
     */
    public static BlockUpdateCollector instance(World world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new BlockUpdateCollector(world));
        }
        return instanceMap.get(world);
    }

    public void registerLaserForUpdateNotifications(TileLaser laser) {
        laserMap.put(laser.getPos(), laser);
    }

    public void removeLaserFromUpdateNotifications(TileLaser laser) {
        if (laserMap.containsKey(laser.getPos())) {
            laserMap.remove(laser.getPos());
        }
    }

    /**
     * Notifies all lasers near the given position that a world update took place. The distance used to determine if a
     * laser is close enough to notify is the laser's targeting range
     *
     * @param eventPos The position of the event that would cause a laser to have to rescan for valid targets
     */
    private void notifyNearbyLasers(BlockPos eventPos) {
        for (BlockPos keyPos : laserMap.keySet()) {
            int targetingRange = laserMap.get(keyPos).getTargetingRange();
            if (Math.abs(keyPos.getX() - eventPos.getX()) <= targetingRange &&
                    Math.abs(keyPos.getY() - eventPos.getY()) <= targetingRange &&
                    Math.abs(keyPos.getZ() - eventPos.getZ()) <= targetingRange) {
                laserMap.get(keyPos).setWorldUpdated();
            }
        }
    }

}
