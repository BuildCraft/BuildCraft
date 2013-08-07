/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.network;

import buildcraft.core.ByteBuffer;
import buildcraft.core.network.ClassMapping.Indexes;
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

	public PacketPayload toPayload(TileEntity tile) {
		int sizeF = 0, sizeS = 0;

		for (int i = 0; i < rootMappings.length; ++i) {
			int[] size = rootMappings[i].getSize();

			sizeF += size[1];
			sizeS += size[2];
		}

		PacketPayloadArrays payload = new PacketPayloadArrays(0, sizeF, sizeS);

		ByteBuffer buf = new ByteBuffer();

		buf.writeInt(tile.xCoord);
		buf.writeInt(tile.yCoord);
		buf.writeInt(tile.zCoord);

		try {
			rootMappings[0].setData(tile, buf, payload.floatPayload, payload.stringPayload, new Indexes(0, 0));

			payload.intPayload = buf.readIntArray();

			return payload;

		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	public PacketPayload toPayload(Object obj) {
		return toPayload(0, 0, 0, new Object[] { obj });
	}

	public PacketPayload toPayload(int x, int y, int z, Object obj) {
		return toPayload(x, y, z, new Object[] { obj });
	}

	public PacketPayload toPayload(int x, int y, int z, Object[] obj) {

		int sizeF = 0, sizeS = 0;

		for (int i = 0; i < rootMappings.length; ++i) {
			int[] size = rootMappings[i].getSize();

			sizeF += size[1];
			sizeS += size[2];
		}

		PacketPayloadArrays payload = new PacketPayloadArrays(0, sizeF, sizeS);

		ByteBuffer buf = new ByteBuffer();

		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);

		try {
			Indexes ind = new Indexes(0, 0);

			for (int i = 0; i < rootMappings.length; ++i) {
				rootMappings[i].setData(obj[i], buf, payload.floatPayload, payload.stringPayload, ind);
			}

			payload.intPayload = buf.readIntArray();

			return payload;

		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	public void fromPayload(TileEntity tile, PacketPayloadArrays packet) {
		try {
			ByteBuffer buf = new ByteBuffer();
			buf.writeIntArray(packet.intPayload);
			buf.readInt();
			buf.readInt();
			buf.readInt();

			rootMappings[0].updateFromData(tile, buf, packet.floatPayload, packet.stringPayload, new Indexes(0, 0));

			packet.intPayload = buf.readIntArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fromPayload(Object obj, PacketPayloadArrays packet) {
		fromPayload(new Object[] { obj }, packet);
	}

	public void fromPayload(Object[] obj, PacketPayloadArrays packet) {
		try {
			ByteBuffer buf = new ByteBuffer();
			buf.writeIntArray(packet.intPayload);
			buf.readInt();
			buf.readInt();
			buf.readInt();

			Indexes ind = new Indexes(0, 0);

			for (int i = 0; i < rootMappings.length; ++i) {
				rootMappings[i].updateFromData(obj[i], buf, packet.floatPayload, packet.stringPayload, ind);
			}

			packet.intPayload = buf.readIntArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
