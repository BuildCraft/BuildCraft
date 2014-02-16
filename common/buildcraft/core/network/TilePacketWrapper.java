/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.tileentity.TileEntity;

public class TilePacketWrapper {
	ClassMapping rootMappings[];

	@SuppressWarnings("rawtypes")
	public TilePacketWrapper(Class c) {
		this(new Class[] { c });
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TilePacketWrapper(Class c[]) {
		rootMappings = new ClassMapping[c.length];

		for (int i = 0; i < c.length; ++i) {
			rootMappings[i] = new ClassMapping(c[i]);
		}
	}

	public PacketPayload toPayload(final TileEntity tile) {
		return new PacketPayloadStream(new PacketPayloadStream.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
				data.writeInt(tile.xCoord);
				data.writeInt(tile.yCoord);
				data.writeInt(tile.zCoord);

				try {
					rootMappings[0].setData(tile, data);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public PacketPayload toPayload(Object obj) {
		return toPayload(0, 0, 0, new Object[] { obj });
	}

	public PacketPayload toPayload(int x, int y, int z, Object obj) {
		return toPayload(x, y, z, new Object[] { obj });
	}

	public PacketPayload toPayload(final int x, final int y, final int z, final Object[] obj) {
		return new PacketPayloadStream(new PacketPayloadStream.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
					data.writeInt(x);
					data.writeInt(y);
					data.writeInt(z);

					for (int i = 0; i < rootMappings.length; ++i) {
						try {
							rootMappings[i].setData(obj[i], data);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

			}
		});
	}

	public void fromPayload(TileEntity tile, PacketPayloadStream packet) {
		try {
			ByteBuf data = packet.stream;

			data.readInt();
			data.readInt();
			data.readInt();

			rootMappings[0].updateFromData(tile, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fromPayload(Object obj, PacketPayloadStream packet) {
		fromPayload(new Object[] { obj }, packet);
	}

	public void fromPayload(Object[] obj, PacketPayloadStream packet) {
		try {
			ByteBuf data = packet.stream;

			data.readInt();
			data.readInt();
			data.readInt();

			for (int i = 0; i < rootMappings.length; ++i) {
				rootMappings[i].updateFromData(obj[i], data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
