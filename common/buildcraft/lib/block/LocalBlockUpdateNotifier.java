package buildcraft.lib.block;

import buildcraft.lib.BCLib;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Listens for BlockUpdates in a given world and notifies all registered IBlockUpdateSubscribers of the update provided
 * it was within the update range of the ILocalBlockUpdateSubscriber
 */
@Mod.EventBusSubscriber(modid = BCLib.MODID) // Calen
public class LocalBlockUpdateNotifier {

    private static final Map<IWorld, LocalBlockUpdateNotifier> instanceMap = new WeakHashMap<>();
    private final Set<ILocalBlockUpdateSubscriber> subscriberSet = new HashSet<>();

    private LocalBlockUpdateNotifier(IWorld world) {
//        IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
//            @Override
//            public void notifyBlockUpdate(@Nonnull World world, @Nonnull BlockPos eventPos, @Nonnull IBlockState oldState,
//                                          @Nonnull IBlockState newState, int flags) {
//                notifySubscribersInRange(world, eventPos, oldState, newState, flags);
//            }
//        };
//        // Calen: moved to #handleForgeEvent
//        world.addEventListener(worldEventListener);
    }

    // Calen
    @SubscribeEvent
    public static void handleForgeEvent(NeighborNotifyEvent event) {
        instanceMap.forEach(
                (level, notifier) ->
                {
                    if (level == event.getWorld()) {
                        notifier.notifySubscribersInRange(event.getWorld(), event.getPos());
                    }
                }
        );
    }

    /**
     * Gets the LocalBlockUpdateNotifier for the given world
     *
     * @param world the World where BlockUpdate events will be listened for
     * @return the instance of LocalBlockUpdateNotifier for the given world
     */
    public static LocalBlockUpdateNotifier instance(IWorld world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new LocalBlockUpdateNotifier(world));
        }
        return instanceMap.get(world);
    }

    /**
     * Register an @{ILocalBlockUpdateSubscriber} to receive notifications about block updates
     *
     * @param subscriber the subscriber to receive notifications about local block updates
     */
    public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.add(subscriber);
    }

    /**
     * Stop an @{ILocalBlockUpdateSubscriber} from receiving notifications about block updates
     *
     * @param subscriber the subscriber to no longer receive notifications about local block update
     */
    public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.remove(subscriber);
    }

    /**
     * Notifies all subscribers near the given position that a world update took place. The distance used to determine
     * if a subscriber is close enough to notify is determined by a call to the subscriber's implementation of
     * getUpdateRange
     *
     * @param world    from the Block Update
     * @param eventPos from the Block Update
     */
//    private void notifySubscribersInRange(World world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags)
    private void notifySubscribersInRange(IWorld world, BlockPos eventPos) {
        for (ILocalBlockUpdateSubscriber subscriber : subscriberSet) {
            BlockPos keyPos = subscriber.getSubscriberPos();
            int updateRange = subscriber.getUpdateRange();
            if (Math.abs(keyPos.getX() - eventPos.getX()) <= updateRange &&
                    Math.abs(keyPos.getY() - eventPos.getY()) <= updateRange &&
                    Math.abs(keyPos.getZ() - eventPos.getZ()) <= updateRange)
            {
//                subscriber.setWorldUpdated(world, eventPos, oldState, newState, flags);
                subscriber.setWorldUpdated(world, eventPos);
            }
        }
    }
}
