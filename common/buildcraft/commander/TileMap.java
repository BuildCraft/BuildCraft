/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import buildcraft.core.BCDynamicTexture;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public class TileMap extends TileBuildCraft {

	private static int RESOLUTION = 1024;
	private static int RESOLUTION_CHUNKS = RESOLUTION >> 4;

	public BCDynamicTexture bcTexture;

	private int[][] colors = new int[RESOLUTION][RESOLUTION];

	private boolean scan = false;
	private int chunkStartX, chunkStartZ;
	// private int curChunkX, curChunkZ;
	private int chunkIt = 0;

	@Override
	public void initialize() {
		super.initialize();
		chunkStartX = (xCoord >> 4) - RESOLUTION_CHUNKS / 2;
		chunkStartZ = (zCoord >> 4) - RESOLUTION_CHUNKS / 2;

		if (!scan) {
			chunkIt = 0;
			scan = true;
		} else {
			if (chunkIt > RESOLUTION_CHUNKS * RESOLUTION_CHUNKS) {

				// In this case, there's been a load problem (resolution
				// change?). just reset the scan.

				chunkIt = 0;
			}
		}
	}

	private int[] getCoords() {
		int chunkCenterX = (xCoord >> 4);
		int chunkCenterZ = (zCoord >> 4);

		if (chunkIt == 0) {
			return new int[] {chunkCenterX, chunkCenterZ};
		}

		int radius = 1;
		int left = chunkIt;

		while (radius < RESOLUTION_CHUNKS / 2) {
			int lineLength = radius * 2;
			int perimeter = lineLength * 4;

			if (left <= perimeter) {
				int chunkX = 0, chunkZ = 0;
				int remained = (left - 1) % lineLength;

				if ((left - 1) / lineLength == 0) {
					chunkX = chunkCenterX + radius;
					chunkZ = chunkCenterZ - lineLength / 2 + remained;
				} else if ((left - 1) / lineLength == 1) {
					chunkX = chunkCenterX - radius;
					chunkZ = chunkCenterZ - lineLength / 2 + remained + 1;
				} else if ((left - 1) / lineLength == 2) {
					chunkX = chunkCenterX - lineLength / 2 + remained + 1;
					chunkZ = chunkCenterZ + radius;
				} else {
					chunkX = chunkCenterX - lineLength / 2 + remained;
					chunkZ = chunkCenterZ - radius;
				}

				return new int[] {chunkX, chunkZ};
			} else {
				left -= perimeter;
			}

			radius += 1;
		}

		return new int[] {chunkCenterX, chunkCenterZ};
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (scan) {
			int[] coords = getCoords();
			Chunk chunk = worldObj.getChunkFromChunkCoords(coords[0], coords[1]);
			loadChunk(chunk);

			if (chunkIt > RESOLUTION_CHUNKS * RESOLUTION_CHUNKS) {
				scan = false;
			} else {
				chunkIt++;
			}
		}
	}

	@RPC(RPCSide.SERVER)
	private void computeMap(int cx, int cz, int width, int height, int blocksPerPixel, RPCMessageInfo info) {
		bcTexture = new BCDynamicTexture(width, height);

		int startX = cx - width * blocksPerPixel / 2;
		int startZ = cz - height * blocksPerPixel / 2;

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				double r = 0;
				double g = 0;
				double b = 0;

				for (int stepi = 0; stepi < blocksPerPixel; ++stepi) {
					for (int stepj = 0; stepj < blocksPerPixel; ++stepj) {
						int x = startX + i * blocksPerPixel + stepi;
						int z = startZ + j * blocksPerPixel + stepj;
						int ix = x - (chunkStartX << 4);
						int iz = z - (chunkStartX << 4);

						if (ix > 0 && ix < RESOLUTION && iz > 0 && iz < RESOLUTION) {
							int color = colors[ix][iz];

							r += (color >> 16) & 255;
							g += (color >> 8) & 255;
							b += color & 255;
						}
					}
				}

				r /= (blocksPerPixel * blocksPerPixel);
				g /= (blocksPerPixel * blocksPerPixel);
				b /= (blocksPerPixel * blocksPerPixel);

				r /= 255F;
				g /= 255F;
				b /= 255F;

				bcTexture.setColor(i, j, r, g, b, 1);
			}
		}

		RPCHandler.rpcPlayer(info.sender, this, "receiveImage", bcTexture.colorMap);
	}

	@RPC(RPCSide.CLIENT)
	private void receiveImage(int[] colors) {
		for (int i = 0; i < colors.length; ++i) {
			bcTexture.colorMap[i] = colors[i];
		}
	}

	private void setColor(int[] map, int width, int height, int index, double r, double g, double b, double a) {
		int i = (int) (a * 255.0F);
		int j = (int) (r * 255.0F);
		int k = (int) (g * 255.0F);
		int l = (int) (b * 255.0F);
		map[index] = i << 24 | j << 16 | k << 8 | l;
	}

	private void setColor(int[] map, int width, int height, int x, int y, double r, double g, double b, double a) {
		int i = (int) (a * 255.0F);
		int j = (int) (r * 255.0F);
		int k = (int) (g * 255.0F);
		int l = (int) (b * 255.0F);
		map[x + y * width] = i << 24 | j << 16 | k << 8 | l;
	}

	private void setColor(int[] map, int width, int height, int x, int y, int color) {
		map[x + y * width] = 255 << 24 | color;
	}


	private void loadChunk(Chunk chunk) {
		for (int cx = 0; cx < 16; ++cx) {
			for (int cz = 0; cz < 16; ++cz) {
				int x = (chunk.xPosition << 4) + cx;
				int z = (chunk.zPosition << 4) + cz;

				int color = 0;

				for (int y = getWorld().getHeight() - 1; y >= 0; --y) {
					if (!chunk.getBlock(cx, y, cz).isAir(worldObj, x, y, z)) {
						color = chunk.getBlock(cx, y, cz).getMapColor(0).colorValue;
						break;
					}
				}

				int ix = x - chunkStartX * 16;
				int iz = z - chunkStartZ * 16;

				colors[ix][iz] = 255 << 24 | color;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("scan", scan);
		nbt.setInteger("chunkIt", chunkIt);

		for (int i = 0; i < RESOLUTION; ++i) {
			nbt.setIntArray("colors[" + i + "]", colors[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		scan = nbt.getBoolean("scan");
		chunkIt = nbt.getInteger("chunkIt");

		for (int i = 0; i < RESOLUTION; ++i) {
			int[] loadedArray =
					nbt.getIntArray("colors[" + i + "]");

			if (loadedArray.length == RESOLUTION) {
				colors[i] = loadedArray;
			}
		}
	}

}
