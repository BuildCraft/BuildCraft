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
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BitSetUtils;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class PacketFluidUpdate extends PacketCoordinates {

	public static int FLUID_ID_BIT = 0;
	public static int FLUID_AMOUNT_BIT = 1;
	public static int FLUID_DATA_NUM = 2;

	public FluidStack[] renderCache = new FluidStack[EnumFacing.values().length];
	public int[] colorRenderCache = new int[EnumFacing.values().length];
	public BitSet delta;

	public PacketFluidUpdate(BlockPos pos) {
		super(PacketIds.PIPE_LIQUID, pos);
	}

	public PacketFluidUpdate(BlockPos pos, boolean chunkPacket) {
		super(PacketIds.PIPE_LIQUID, pos);
		this.isChunkDataPacket = chunkPacket;
	}

	public PacketFluidUpdate() {
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		World world = CoreProxy.proxy.getClientWorld();
		if (!world.isBlockLoaded(pos, false)) {
			return;
		}

		TileEntity entity = world.getTileEntity(pos);
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
		delta = BitSetUtils.fromByteArray(dBytes);

		// System.out.printf("read %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);

		for (EnumFacing dir : EnumFacing.values()) {
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

		byte[] dBytes = BitSetUtils.toByteArray(delta, 2);
		// System.out.printf("write %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);
		data.writeBytes(dBytes);

		for (EnumFacing dir : EnumFacing.values()) {
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

	@Override
	public int getID() {
		return PacketIds.PIPE_LIQUID;
	}
}
