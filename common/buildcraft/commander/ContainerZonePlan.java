/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.core.BCDynamicTexture;
import buildcraft.core.ZonePlan;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotOutput;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public class ContainerZonePlan extends BuildCraftContainer {

	public BCDynamicTexture mapTexture;
	public ZonePlan currentAreaSelection;
	public GuiZonePlan gui;

	private TileZonePlan map;

	public ContainerZonePlan(IInventory playerInventory, TileZonePlan iZonePlan) {
		super(0);

		map = iZonePlan;

		addSlotToContainer(new Slot(iZonePlan, 0, 233, 20));
		addSlotToContainer(new SlotOutput(iZonePlan, 1, 233, 68));

		// Player inventory
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 88 + k1 * 18, 138 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 88 + i1 * 18, 196));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return true;
	}

	public void loadArea(int index) {
		RPCHandler.rpcServer(this, "rpcLoadArea", index);
	}

	public void saveArea(int index) {
		RPCHandler.rpcServer(this, "rpcSaveArea", index, currentAreaSelection);
	}

	@RPC(RPCSide.SERVER)
	private void rpcLoadArea(int index, RPCMessageInfo info) {
		RPCHandler.rpcPlayer(info.sender, this, "rpcAreaLoaded", map.selectArea(index));
	}

	@RPC(RPCSide.SERVER)
	private void rpcSaveArea(int index, ZonePlan area) {
		map.setArea(index, area);
	}

	@RPC(RPCSide.CLIENT)
	private void rpcAreaLoaded(ZonePlan areaSelection) {
		currentAreaSelection = areaSelection;
		gui.refreshSelectedArea();
	}

	@RPC(RPCSide.SERVER)
	private void computeMap(int cx, int cz, int width, int height, int blocksPerPixel, RPCMessageInfo info) {
		mapTexture = new BCDynamicTexture(width, height);

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
						int ix = x - (map.chunkStartX << 4);
						int iz = z - (map.chunkStartZ << 4);

						if (ix > 0 && ix < TileZonePlan.RESOLUTION && iz > 0 && iz < TileZonePlan.RESOLUTION) {
							int color = MapColor.mapColorArray[map.colors[ix + iz * TileZonePlan.RESOLUTION]].colorValue;

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

				mapTexture.setColor(i, j, r, g, b, 1);
			}
		}

		RPCHandler.rpcPlayer(info.sender, this, "receiveImage", mapTexture.colorMap);
	}

	@RPC(RPCSide.CLIENT)
	private void receiveImage(int[] colors) {
		for (int i = 0; i < colors.length; ++i) {
			mapTexture.colorMap[i] = colors[i];
		}
	}
}
