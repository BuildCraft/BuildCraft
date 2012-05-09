/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftSilicon extends NetworkMod {

	public static mod_BuildCraftSilicon instance;

	public mod_BuildCraftSilicon() {
		instance = this;
	}

	@Override
	public void modsLoaded () {
		super.modsLoaded();

		BuildCraftTransport.initialize();
		BuildCraftSilicon.initialize();

		BuildCraftSilicon.initializeModel(this);
	}

	/*
	@Override
	public void handlePacket(Packet230ModLoader packet, EntityPlayerMP entityplayermp) {
		int x = packet.dataInt[0];
		int y = packet.dataInt[1];
		int z = packet.dataInt[2];

		TileEntity tile = entityplayermp.worldObj.getBlockTileEntity(x, y,
				z);

		switch (PacketIds.values()[packet.packetType]) {
		case AssemblyTableSelect:
			if (tile instanceof TileAssemblyTable) {
				TileAssemblyTable assemblyTable = (TileAssemblyTable) tile;
				TileAssemblyTable.SelectionMessage message = new TileAssemblyTable.SelectionMessage();

				TileAssemblyTable.selectionMessageWrapper.updateFromPacket(message, packet);

				assemblyTable.handleSelectionMessage (message);
			}

			break;
		case AssemblyTableGetSelection:
			if (tile instanceof TileAssemblyTable) {
				TileAssemblyTable assemblyTable = (TileAssemblyTable) tile;
				assemblyTable.sendSelectionTo(entityplayermp);
			}
		}
	}*/

	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}

	@Override
	public void load() {
		BuildCraftSilicon.load();
	}

	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return true; }
}
