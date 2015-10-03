/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Sets;

public class TravelerSet extends ForwardingSet<TravelingItem> {

    public boolean iterating;

    private final Set<TravelingItem> items = Sets.newConcurrentHashSet();
    private final Set<TravelingItem> toLoad = Sets.newConcurrentHashSet();
    private final Set<TravelingItem> toAdd = Sets.newConcurrentHashSet();
    private final Set<TravelingItem> toRemove = Sets.newConcurrentHashSet();
    private int delay = 0;
    private final PipeTransportItems transport;

    public TravelerSet(PipeTransportItems transport) {
        this.transport = transport;
    }

    @Override
    protected Set<TravelingItem> delegate() {
        return items;
    }

    @Override
    public boolean add(TravelingItem item) {
        if (iterating) {
            return toAdd.add(item);
        }
        item.setContainer(transport.container);
        items.add(item);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends TravelingItem> collection) {
        if (iterating) {
            return toAdd.addAll(collection);
        }
        return standardAddAll(collection);
    }

    @Override
    public boolean remove(Object object) {
        if (iterating) {
            return toRemove.add((TravelingItem) object);
        }
        return delegate().remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    void scheduleLoad(TravelingItem item) {
        delay = 10;
        toLoad.add(item);
    }

    private void loadScheduledItems() {
        if (delay > 0) {
            delay--;
            return;
        }
        addAll(toLoad);
        toLoad.clear();
    }

    private void addScheduledItems() {
        addAll(toAdd);
        toAdd.clear();
    }

    public boolean scheduleRemoval(TravelingItem item) {
        return toRemove.add(item);
    }

    public boolean unscheduleRemoval(TravelingItem item) {
        return toRemove.remove(item);
    }

    void removeScheduledItems() {
        items.removeAll(toRemove);
        toRemove.clear();
    }

    void flush() {
        loadScheduledItems();
        addScheduledItems();
        removeScheduledItems();
    }

    @Override
    public Iterator<TravelingItem> iterator() {
        return items.iterator();
    }

    @Override
    public void clear() {
        if (iterating) {
            toRemove.addAll(this);
        } else {
            items.clear();
        }
    }
}
