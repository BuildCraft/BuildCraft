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

import buildcraft.core.MapArea;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public class ContainerMap extends BuildCraftContainer {

	private TileMap map;
	public MapArea currentAreaSelection;
	public GuiMap gui;

	public ContainerMap(TileMap iMap) {
		super(0);

		map = iMap;
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
		RPCHandler.rpcPlayer(info.sender, this, "rpcAreaLoaded", map.getArea(index));
	}

	@RPC(RPCSide.SERVER)
	private void rpcSaveArea(int index, MapArea area) {
		map.setArea(index, area);
	}

	@RPC(RPCSide.CLIENT)
	private void rpcAreaLoaded(MapArea areaSelection) {
		currentAreaSelection = areaSelection;
		gui.refreshSelectedArea();
	}

}
