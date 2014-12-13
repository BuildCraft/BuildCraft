/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import net.minecraftforge.fml.relauncher.Side;
import buildcraft.BuildCraftCore;
import buildcraft.core.BCDynamicTexture;
import buildcraft.core.ZonePlan;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotOutput;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;

public class ContainerZonePlan extends BuildCraftContainer implements ICommandReceiver {

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

				for (int i = 0; i < size; ++i) {
					mapTexture.colorMap[i] = 0xFF000000 | MapColor.mapColorArray[stream.readUnsignedByte()].colorValue;
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
						stream.readUnsignedByte(), (EntityPlayer) sender);
			}
		}
	}

	private void computeMap(int cx, int cz, int width, int height, int blocksPerPixel, EntityPlayer player) {
		final byte[] textureData = new byte[width * height];

		int startX = cx - width * blocksPerPixel / 2;
		int startZ = cz - height * blocksPerPixel / 2;

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int x = startX + i * blocksPerPixel;
				int z = startZ + j * blocksPerPixel;
				int ix = x - (map.chunkStartX << 4);
				int iz = z - (map.chunkStartZ << 4);

				if (ix >= 0 && iz >= 0 && ix < TileZonePlan.RESOLUTION && iz < TileZonePlan.RESOLUTION) {
					textureData[i + j * width] = map.colors[ix + iz * TileZonePlan.RESOLUTION];
				}
			}
		}

		BuildCraftCore.instance.sendToPlayer(player, new PacketCommand(this, "receiveImage", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeMedium(textureData.length);
				data.writeBytes(textureData);
			}
		}));
	}
}
