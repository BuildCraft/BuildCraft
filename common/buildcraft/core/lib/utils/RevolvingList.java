/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import com.google.common.collect.ForwardingCollection;

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
		if (list.isEmpty()) {
			return;
		} else {
			list.addFirst(list.removeLast());
		}
	}

	public void rotateRight() {
		if (list.isEmpty()) {
			return;
		} else {
			list.addLast(list.removeFirst());
		}
	}

	public T getCurrent() {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.getFirst();
		}
	}

	public void setCurrent(T e) {
		if (!contains(e)) {
			return;
		} else if (e == null) {
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
