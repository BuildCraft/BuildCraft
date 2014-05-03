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

import net.minecraft.tileentity.TileEntity;

import buildcraft.core.network.serializers.ClassMapping;
import buildcraft.core.network.serializers.ClassSerializer;
import buildcraft.core.network.serializers.SerializationContext;

public class TilePacketWrapper {
	ClassSerializer[] rootMappings;

	@SuppressWarnings("rawtypes")
	public TilePacketWrapper(Class c) {
		this(new Class[] {c});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TilePacketWrapper(Class[] c) {
		rootMappings = new ClassSerializer [c.length];

		for (int i = 0; i < c.length; ++i) {
			rootMappings[i] = ClassMapping.get (c[i]);
		}
	}

	public PacketPayload toPayload(final TileEntity tile) {
		return new PacketPayload(new PacketPayload.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
				data.writeInt(tile.xCoord);
				data.writeInt(tile.yCoord);
				data.writeInt(tile.zCoord);

				try {
					SerializationContext context = new SerializationContext();
					rootMappings[0].write(data, tile, context);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
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
		return new PacketPayload(new PacketPayload.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
					data.writeInt(x);
					data.writeInt(y);
					data.writeInt(z);

					for (int i = 0; i < rootMappings.length; ++i) {
						try {
							SerializationContext context = new SerializationContext();
							rootMappings[0].write(data, obj [i], context);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}

			}
		});
	}

	public void fromPayload(TileEntity tile, PacketPayload packet) {
		try {
			ByteBuf data = packet.stream;

			data.readInt();
			data.readInt();
			data.readInt();

			SerializationContext context = new SerializationContext();
			rootMappings[0].read(data, tile, context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fromPayload(Object obj, PacketPayload packet) {
		fromPayload(new Object[] { obj }, packet);
	}

	public void fromPayload(Object[] obj, PacketPayload packet) {
		try {
			ByteBuf data = packet.stream;

			data.readInt();
			data.readInt();
			data.readInt();

			for (int i = 0; i < rootMappings.length; ++i) {
				SerializationContext context = new SerializationContext();
				rootMappings[i].read(data, obj[i], context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
