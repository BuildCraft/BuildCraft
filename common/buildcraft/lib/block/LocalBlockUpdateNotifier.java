/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.block;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Listens for BlockUpdates in a given world and notifies all registered ILocalBlockUpdateSubscribers of the update
 * provided it was within the update range of the ILocalBlockUpdateSubscriber.
 *
 * STUB(R.Chen): IWorldEventListener does not exist in Fabric 1.20.1. Block update notifications are deferred until
 * a Fabric equivalent (e.g. WorldChunk event or mixin) is wired up. The subscriber registration/removal is kept
 * intact; the notifySubscribersInRange method must be called externally for now.
 */
public class LocalBlockUpdateNotifier {

    private static final Map<World, LocalBlockUpdateNotifier> instanceMap = new WeakHashMap<>();
    private final Set<ILocalBlockUpdateSubscriber> subscriberSet = new HashSet<>();

    private LocalBlockUpdateNotifier(World world) {
        // STUB(R.Chen): In Forge this registered an IWorldEventListener via world.addEventListener().
        // Fabric 1.20.1 has no direct equivalent. A mixin into WorldChunk.setBlockState or a
        // Fabric lifecycle callback would be needed to replicate the block-update notification.
        // world.addEventListener(worldEventListener); // NOT AVAILABLE in Fabric 1.20.1
    }

    /**
     * Gets the LocalBlockUpdateNotifier for the given world.
     *
     * @param world the World where BlockUpdate events will be listened for
     * @return the instance of LocalBlockUpdateNotifier for the given world
     */
    public static LocalBlockUpdateNotifier instance(World world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new LocalBlockUpdateNotifier(world));
        }
        return instanceMap.get(world);
    }

    /**
     * Register an ILocalBlockUpdateSubscriber to receive notifications about block updates.
     *
     * @param subscriber the subscriber to receive notifications about local block updates
     */
    public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.add(subscriber);
    }

    /**
     * Stop an ILocalBlockUpdateSubscriber from receiving notifications about block updates.
     *
     * @param subscriber the subscriber to no longer receive notifications about local block updates
     */
    public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.remove(subscriber);
    }

    /**
     * Notifies all subscribers near the given position that a world update took place. The distance used to determine
     * if a subscriber is close enough to notify is determined by a call to the subscriber's implementation of
     * getUpdateRange.
     *
     * STUB(R.Chen): This method is kept for future wiring. It is not called automatically in Fabric 1.20.1 because
     * IWorldEventListener was removed. Needs a mixin or Fabric event to call this.
     *
     * @param world    from the Block Update
     * @param eventPos from the Block Update
     * @param oldState from the Block Update
     * @param newState from the Block Update
     * @param flags    from the Block Update
     */
    public void notifySubscribersInRange(World world, BlockPos eventPos, BlockState oldState, BlockState newState,
                                         int flags) {
        for (ILocalBlockUpdateSubscriber subscriber : subscriberSet) {
            BlockPos keyPos = subscriber.getSubscriberPos();
            int updateRange = subscriber.getUpdateRange();
            if (Math.abs(keyPos.getX() - eventPos.getX()) <= updateRange &&
                    Math.abs(keyPos.getY() - eventPos.getY()) <= updateRange &&
                    Math.abs(keyPos.getZ() - eventPos.getZ()) <= updateRange) {
                subscriber.setWorldUpdated(world, eventPos, oldState, newState, flags);
            }
        }
    }
}
