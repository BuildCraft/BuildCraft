/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.gui;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftRobotics;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.render.DynamicTextureBC;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.robotics.TileZonePlan;
import buildcraft.robotics.ZonePlan;
import buildcraft.robotics.map.MapWorld;

public class ContainerZonePlan extends BuildCraftContainer implements ICommandReceiver {
	private static final int MAX_PACKET_LENGTH = 30000;

	public DynamicTextureBC mapTexture;
	public ZonePlan currentAreaSelection;
	public GuiZonePlan gui;

	private TileZonePlan map;

	public ContainerZonePlan(IInventory playerInventory, TileZonePlan iZonePlan) {
		super(0);

		map = iZonePlan;

		addSlotToContainer(new Slot(iZonePlan, 0, 233, 9));
		addSlotToContainer(new SlotOutput(iZonePlan, 1, 233, 57));
		addSlotToContainer(new Slot(iZonePlan, 2, 8, 125));

		// Player inventory
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 88 + k1 * 18, 146 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 88 + i1 * 18, 204));
		}
	}

	public TileZonePlan getTile() {
		return map;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	public void loadArea(final int index) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "loadArea", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeByte(index);
			}
		}));
	}

	public void saveArea(final int index) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "saveArea", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeByte(index);
				currentAreaSelection.writeData(data);
			}
		}));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isClient()) {
			if ("areaLoaded".equals(command)) {
				currentAreaSelection = new ZonePlan();
				currentAreaSelection.readData(stream);
				gui.refreshSelectedArea();
			} else if ("receiveImage".equals(command)) {
				int size = stream.readUnsignedMedium();
				int pos = stream.readUnsignedMedium();

				for (int i = 0; i < Math.min(size - pos, MAX_PACKET_LENGTH); ++i) {
					mapTexture.colorMap[pos + i] = 0xFF000000 | MapColor.mapColorArray[stream.readUnsignedByte()].colorValue;
				}
			}
		} else if (side.isServer()) {
			if ("loadArea".equals(command)) {
				final int index = stream.readUnsignedByte();
				BuildCraftCore.instance.sendToPlayer((EntityPlayer) sender, new PacketCommand(this, "areaLoaded", new CommandWriter() {
					public void write(ByteBuf data) {
						map.selectArea(index).writeData(data);
					}
				}));
			} else if ("saveArea".equals(command)) {
				final int index = stream.readUnsignedByte();
				ZonePlan plan = new ZonePlan();
				plan.readData(stream);
				map.setArea(index, plan);
			} else if ("computeMap".equals(command)) {
				computeMap(stream.readInt(), stream.readInt(),
						stream.readUnsignedShort(), stream.readUnsignedShort(),
						stream.readFloat(), (EntityPlayer) sender);
			} else if ("setName".equals(command)) {
				map.mapName = NetworkUtils.readUTF(stream);
			}
		}
	}

	private void computeMap(int cx, int cz, int width, int height, float blocksPerPixel, EntityPlayer player) {
		final byte[] textureData = new byte[width * height];

		MapWorld w = BuildCraftRobotics.manager.getWorld(map.getWorldObj());
		int startX = Math.round(cx - width * blocksPerPixel / 2);
		int startZ = Math.round(cz - height * blocksPerPixel / 2);
		int mapStartX = map.chunkStartX << 4;
		int mapStartZ = map.chunkStartZ << 4;

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int x = Math.round(startX + i * blocksPerPixel);
				int z = Math.round(startZ + j * blocksPerPixel);
				int ix = x - mapStartX;
				int iz = z - mapStartZ;

				if (ix >= 0 && iz >= 0 && ix < TileZonePlan.RESOLUTION && iz < TileZonePlan.RESOLUTION) {
					textureData[i + j * width] = (byte) w.getColor(x, z);
				}
			}
		}

		final int len = MAX_PACKET_LENGTH;

		for (int i = 0; i < textureData.length; i += len) {
			final int pos = i;
			BuildCraftCore.instance.sendToPlayer(player, new PacketCommand(this, "receiveImage", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeMedium(textureData.length);
					data.writeMedium(pos);
					data.writeBytes(textureData, pos, Math.min(textureData.length - pos, len));
				}
			}));
		}
	}
}
