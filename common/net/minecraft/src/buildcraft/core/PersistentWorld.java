/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import java.util.HashMap;
import java.util.TreeMap;

import net.minecraft.src.IBlockAccess;

public class PersistentWorld {

	private static HashMap<Long, PersistentWorld> worlds = new HashMap<Long, PersistentWorld>();
	private static Long lastBlockAccess = null;
	private static PersistentWorld lastWorld = null;

	private TreeMap<BlockIndex, PersistentTile> tiles = new TreeMap<BlockIndex, PersistentTile>();

	public PersistentTile createTile(PersistentTile defaultTile, BlockIndex index) {
		PersistentTile result = null;

		if (!tiles.containsKey(index)) {
			tiles.put(index, defaultTile);
			result = defaultTile;
		} else {
			result = tiles.get(index);

			if (result == defaultTile) {

			} else if (!result.getClass().equals(defaultTile.getClass())) {
				tiles.remove(index);
				tiles.put(index, defaultTile);
				result.destroy ();
				result = defaultTile;
			} else
				defaultTile.destroy ();
		}

		return result;
	}

	public void storeTile(PersistentTile tile, BlockIndex index) {
		if (tiles.containsKey(index)) {
			PersistentTile old = tiles.get (index);

			if (old == tile)
				return;

			tiles.remove(index).destroy();
		}

		tiles.put(index, tile);
	}

	public PersistentTile getTile(BlockIndex index) {
		return tiles.get(index);
	}

	public void removeTile(BlockIndex index) {
		if (tiles.containsKey(index))
			tiles.remove(index).destroy ();
	}

	public static PersistentWorld getWorld (IBlockAccess blockAccess) {
		Long hash = CoreProxy.getHash(blockAccess);
		if (!hash.equals(lastBlockAccess)) {
			if (!worlds.containsKey(hash))
				worlds.put(hash, new PersistentWorld());

			lastBlockAccess = hash;
			lastWorld = worlds.get(hash);
		}

		return lastWorld;
	}

}
