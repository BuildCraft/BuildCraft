package net.minecraft.src.buildcraft.core;

import java.util.Iterator;

import net.minecraft.src.World;

public abstract class WorldIterator {

	protected World world;
	protected int x;
	protected int y;
	protected int z;

	protected Iterator <BlockIndex> iterator;

	public WorldIterator (World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockIndex iterate () {
		if (iterator.hasNext())
			return iterator.next();
		else
			return null;
	}

}
