/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.core.BCDynamicTexture;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public class TileMap extends TileBuildCraft {

	public BCDynamicTexture bcTexture;

	private boolean currentComputation = false;
	private int curI, curJ;
	private int itPerCycles;
	private int blocksPerPixel;
	private EntityPlayer playerRequesting;
	private int lastIndexUpdate = 0;

	@Override
	public void updateEntity() {
		if (currentComputation) {
			for (int k = 0; k < itPerCycles; ++k) {
				curI++;

				if (curI >= bcTexture.width) {
					curI = 0;
					curJ++;
				}

				if (curJ >= bcTexture.height) {
					curJ = 0;
					currentComputation = false;

					sendPixels(lastIndexUpdate, bcTexture.colorMap.length - lastIndexUpdate);
					return;
				}

				double r = 0;
				double g = 0;
				double b = 0;

				for (int stepi = 0; stepi < blocksPerPixel; ++stepi) {
					for (int stepj = 0; stepj < blocksPerPixel; ++stepj) {
						int x = xCoord - bcTexture.width * blocksPerPixel / 2 + curI * blocksPerPixel + stepi;
						int z = zCoord - bcTexture.height * blocksPerPixel / 2 + curJ * blocksPerPixel + stepj;

						for (int y = getWorld().getHeight() - 1; y >= 0; --y) {
							if (!getWorld().isAirBlock(x, y, z)) {
								int color = worldObj.getBlock(x, y, z).getMapColor(0).colorValue;

								r += (color >> 16) & 255;
								g += (color >> 8) & 255;
								b += color & 255;

								break;
							}
						}
					}
				}

				r /= (blocksPerPixel * blocksPerPixel);
				g /= (blocksPerPixel * blocksPerPixel);
				b /= (blocksPerPixel * blocksPerPixel);

				r /= 255F;
				g /= 255F;
				b /= 255F;

				bcTexture.setColor(curI, curJ, r, g, b, 1);
			}

			if (currentComputation && worldObj.getTotalWorldTime() % 5 == 0) {
				int curIndexUpdate = curI + curJ * bcTexture.width;
				sendPixels(lastIndexUpdate, curIndexUpdate - lastIndexUpdate);
				lastIndexUpdate = curIndexUpdate;
			}
		}
	}


	@RPC(RPCSide.SERVER)
	private void computeMap(int width, int height, int iBlocksPerPixel, RPCMessageInfo info) {
		currentComputation = true;
		curI = 0;
		curJ = 0;
		bcTexture = new BCDynamicTexture(width, height);
		blocksPerPixel = iBlocksPerPixel;
		itPerCycles = 1000 / blocksPerPixel;
		playerRequesting = info.sender;
		lastIndexUpdate = 0;
	}

	private void sendPixels(int from, int number) {
		int[] pixels = new int[number];

		for (int i = 0; i < pixels.length; ++i) {
			pixels[i] = bcTexture.colorMap[i + from];
		}

		RPCHandler.rpcPlayer(playerRequesting, this, "receivePixels", pixels, from);
	}

	@RPC(RPCSide.CLIENT)
	private void receivePixels(int[] pixels, int from) {
		// TODO: add safety in these functions in case of one player calling
		// several times in a row
		for (int i = 0; i < pixels.length; ++i) {
			bcTexture.colorMap[i + from] = pixels[i];
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

}
