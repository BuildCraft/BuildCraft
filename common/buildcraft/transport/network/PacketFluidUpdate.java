/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import java.util.BitSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class PacketFluidUpdate extends PacketCoordinates {

	public static int FLUID_ID_BIT = 0;
	public static int FLUID_AMOUNT_BIT = 1;
	public static int FLUID_DATA_NUM = 2;

	public FluidStack[] renderCache = new FluidStack[ForgeDirection.values().length];
	public int[] colorRenderCache = new int[ForgeDirection.values().length];
	public BitSet delta;

	public PacketFluidUpdate(int xCoord, int yCoord, int zCoord) {
		super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
	}

	public PacketFluidUpdate(int xCoord, int yCoord, int zCoord, boolean chunkPacket) {
		super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
		this.isChunkDataPacket = chunkPacket;
	}

	public PacketFluidUpdate() {
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		World world = CoreProxy.proxy.getClientWorld();
		if (!world.blockExists(posX, posY, posZ)) {
			return;
		}

		TileEntity entity = world.getTileEntity(posX, posY, posZ);
		if (!(entity instanceof TileGenericPipe)) {
			return;
		}

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null) {
			return;
		}

		if (!(pipe.pipe.transport instanceof PipeTransportFluids)) {
			return;
		}

		PipeTransportFluids transLiq = (PipeTransportFluids) pipe.pipe.transport;

		renderCache = transLiq.renderCache;
		colorRenderCache = transLiq.colorRenderCache;

		byte[] dBytes = new byte[2];
		data.readBytes(dBytes);
		delta = fromByteArray(dBytes);

		// System.out.printf("read %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);

		for (ForgeDirection dir : ForgeDirection.values()) {
			if (delta.get(dir.ordinal() * FLUID_DATA_NUM + FLUID_ID_BIT)) {
			    int amt = renderCache[dir.ordinal()] != null ? renderCache[dir.ordinal()].amount : 0;
				renderCache[dir.ordinal()] = new FluidStack(data.readShort(), amt);
				colorRenderCache[dir.ordinal()] = data.readInt();
			}
			if (delta.get(dir.ordinal() * FLUID_DATA_NUM + FLUID_AMOUNT_BIT)) {
			    if (renderCache[dir.ordinal()] == null) {
					renderCache[dir.ordinal()] = new FluidStack(0, 0);
			    }
		        renderCache[dir.ordinal()].amount = Math.min(transLiq.getCapacity(), data.readInt());
			}
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		byte[] dBytes = toByteArray(delta);
		// System.out.printf("write %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);
		data.writeBytes(dBytes);

		for (ForgeDirection dir : ForgeDirection.values()) {
			FluidStack liquid = renderCache[dir.ordinal()];

			if (delta.get(dir.ordinal() * FLUID_DATA_NUM + FLUID_ID_BIT)) {
				if (liquid != null) {
					data.writeShort(liquid.fluidID);
					data.writeInt(colorRenderCache[dir.ordinal()]);
				} else {
					data.writeShort(0);
					data.writeInt(0xFFFFFF);
				}
			}
			if (delta.get(dir.ordinal() * FLUID_DATA_NUM + FLUID_AMOUNT_BIT)) {
				if (liquid != null) {
					data.writeInt(liquid.amount);
				} else {
					data.writeInt(0);
				}
			}
		}
	}

	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[2];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}
		return bytes;
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_LIQUID;
	}
}
