/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import com.google.common.collect.ForwardingCollection;
import java.util.*;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class RevolvingList<T> extends ForwardingCollection<T> {

	private Deque<T> list = new LinkedList<T>();

	public RevolvingList() {
	}

	public RevolvingList(Collection<? extends T> collection) {
		list.addAll(collection);
	}

	@Override
	protected Collection<T> delegate() {
		return list;
	}

	public void rotateLeft() {
		if (list.isEmpty())
			return;
		list.addFirst(list.removeLast());
	}

	public void rotateRight() {
		if (list.isEmpty())
			return;
		list.addLast(list.removeFirst());
	}

	public T getCurrent() {
		if (list.isEmpty())
			return null;
		return list.getFirst();
	}

	public void setCurrent(T e) {
		if (!contains(e))
			return;

		if (e == null) {
			while (getCurrent() != null) {
				rotateRight();
			}
		} else {
			while (getCurrent() == null || !getCurrent().equals(e)) {
				rotateRight();
			}
		}
	}
}
