/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TravelerSet implements Iterable<TravelingItem> {

	private final BiMap<Integer, TravelingItem> items = HashBiMap.create();
	private final Set<TravelingItem> toLoad = new HashSet<TravelingItem>();
	private final Set<TravelingItem> toAdd = new HashSet<TravelingItem>();
	private final Set<TravelingItem> toRemove = new HashSet<TravelingItem>();
	private int delay = 0;
	private final PipeTransportItems transport;

	public TravelerSet(PipeTransportItems transport) {
		this.transport = transport;
	}

	private boolean add(TravelingItem item) {
		if (items.containsValue(item))
			return false;
		item.setContainer(transport.container);
		items.put(item.id, item);
		return true;
	}

	private boolean addAll(Collection<? extends TravelingItem> collection) {
		boolean changed = false;
		for (TravelingItem item : collection) {
			changed |= add(item);
		}
		return changed;
	}

	public TravelingItem get(int id) {
		return items.get(id);
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

	public void scheduleAdd(TravelingItem item) {
		toAdd.add(item);
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
		items.values().removeAll(toRemove);
		toRemove.clear();
	}

	void purgeBadItems() {
		Iterator<TravelingItem> it = items.values().iterator();
		while (it.hasNext()) {
			TravelingItem item = it.next();
			if (item.isCorrupted()) {
				it.remove();
				continue;
			}

			if (item.getContainer() != transport.container) {
				it.remove();
				continue;
			}
		}
	}

	void flush() {
		loadScheduledItems();
		addScheduledItems();
		removeScheduledItems();
		purgeBadItems();
	}

	@Override
	public Iterator<TravelingItem> iterator() {
		return items.values().iterator();
	}

	public int size() {
		return items.values().size();
	}

	void clear() {
		toRemove.addAll(items.values());
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
}
