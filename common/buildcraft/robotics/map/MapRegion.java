package buildcraft.robotics.map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IntHashMap;

import buildcraft.api.core.INBTStoreable;

public class MapRegion implements INBTStoreable {
	private final IntHashMap chunks = new IntHashMap();
	private final int x, z;

	public MapRegion(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public boolean hasChunk(int x, int z) {
		return chunks.containsItem((z << 4) | x);
	}

	public MapChunk getChunk(int x, int z) {
		int id = (z << 4) | x;
		MapChunk chunk = (MapChunk) chunks.lookup(id);
		if (chunk == null) {
			chunk = new MapChunk(x, z);
			chunks.addKey(id, chunk);
		}
		return chunk;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		chunks.clearMap();

		if (tag != null) {
			for (int i = 0; i < 256; i++) {
				if (tag.hasKey("r" + i)) {
					MapChunk chunk = new MapChunk(tag.getCompoundTag("r" + i));
					chunks.addKey(i, chunk);
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		for (int i = 0; i < 256; i++) {
			MapChunk chunk = (MapChunk) chunks.lookup(i);
			if (chunk != null) {
				NBTTagCompound chunkNBT = new NBTTagCompound();
				synchronized (chunk) {
					chunk.writeToNBT(chunkNBT);
				}
				tag.setTag("r" + i, chunkNBT);
			}
		}
	}
}
