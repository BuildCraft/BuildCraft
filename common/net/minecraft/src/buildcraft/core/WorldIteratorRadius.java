package net.minecraft.src.buildcraft.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import net.minecraft.src.World;

public class WorldIteratorRadius extends WorldIterator {

	@SuppressWarnings("unchecked")
	private static LinkedList <BlockIndex> [] lists = new LinkedList [65];

	public WorldIteratorRadius(World world, int px, int py, int pz, int radius) {
		super(world, px, py, pz);

		createPrecomputedList(radius);

		iterator = lists [radius].iterator();
	}

	@Override
	public BlockIndex iterate () {
		if (iterator.hasNext()) {
			BlockIndex b = iterator.next();

			return new BlockIndex(b.i + x, b.j + y, b.k + z);
		} else
			return null;
	}

	public static void createPrecomputedList (int radius) {
		if (lists [radius] == null) {
			lists [radius] = new LinkedList <BlockIndex> ();

			for (int i = -radius; i <= radius; ++i)
				for (int j = -radius; j <= radius; ++j)
					for (int k = -radius; k <= radius; ++k)
						lists [radius].add(new BlockIndex(i, j, k));

			Collections.sort(lists [radius], new Comparator <BlockIndex> () {

				@Override
				public int compare(BlockIndex o1, BlockIndex o2) {
					double d1 = o1.i * o1.i + o1.j * o1.j + o1.k * o1.k;
					double d2 = o2.i * o2.i + o2.j * o2.j + o2.k * o2.k;

					if (d1 < d2)
						return -1;
					else if (d1 > d2)
						return 1;
					else
						return 0;
				}});
		}
	}
}
