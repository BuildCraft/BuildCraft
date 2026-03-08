package buildcraft.lib.block;

import buildcraft.lib.BCLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;


/**
 * Listens for BlockUpdates in a given world and notifies all registered IBlockUpdateSubscribers of the update provided
 * it was within the update range of the ILocalBlockUpdateSubscriber
 */
@Mod.EventBusSubscriber(modid = BCLib.MODID) // Calen
public class LocalBlockUpdateNotifier {

    private static final Map<LevelAccessor, LocalBlockUpdateNotifier> instanceMap = new WeakHashMap<>();
    private final Set<ILocalBlockUpdateSubscriber> subscriberSet = new HashSet<>();


    private LocalBlockUpdateNotifier(LevelAccessor world) {
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

    private static final List<GameEvent> listenedEvents = List.of(GameEvent.BLOCK_CHANGE, GameEvent.EXPLODE, GameEvent.BLOCK_PLACE, GameEvent.BLOCK_DESTROY);

    // Calen
    @SubscribeEvent
    public static void handleForgeEvent(VanillaGameEvent event) {
        GameEvent gameEvent = event.getVanillaEvent();
        if (listenedEvents.contains(gameEvent)) {
            instanceMap.forEach(
                    (level, notifier) ->
                    {
                        if (level == event.getLevel()) {
                            notifier.notifySubscribersInRange(event.getLevel(), event.getEventPosition());
                        }
                    }
            );
        }
    }

    /**
     * Gets the LocalBlockUpdateNotifier for the given world
     *
     * @param world the Level where BlockUpdate events will be listened for
     * @return the instance of LocalBlockUpdateNotifier for the given world
     */
    public static LocalBlockUpdateNotifier instance(LevelAccessor world) {
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
//    private void notifySubscribersInRange(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags)
    private void notifySubscribersInRange(Level world, BlockPos eventPos) {
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
