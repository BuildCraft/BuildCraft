package buildcraft.transport.network;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class PacketFluidUpdate extends PacketCoordinates {

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
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		World world = CoreProxy.proxy.getClientWorld();
		if (!world.blockExists(posX, posY, posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(posX, posY, posZ);
		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeTransportFluids))
			return;

		PipeTransportFluids transLiq = ((PipeTransportFluids) pipe.pipe.transport);

		renderCache = transLiq.renderCache;
		colorRenderCache = transLiq.colorRenderCache;

		byte[] dBytes = new byte[3];
		data.read(dBytes);
		delta = fromByteArray(dBytes);

		// System.out.printf("read %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);

		for (ForgeDirection dir : ForgeDirection.values()) {
			if (delta.get(dir.ordinal() * 3 + 0)) {
			    int amt = renderCache[dir.ordinal()] != null ? renderCache[dir.ordinal()].amount : 0;
				renderCache[dir.ordinal()] = new FluidStack(data.readInt(), amt);
				colorRenderCache[dir.ordinal()] = data.readInt();
			}
			if (delta.get(dir.ordinal() * 3 + 2)) {
			    if (renderCache[dir.ordinal()] == null) {
			        renderCache[dir.ordinal()] = new FluidStack(0,0);
			    }
		        renderCache[dir.ordinal()].amount = Math.min(transLiq.getCapacity(), data.readInt());
			}
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		byte[] dBytes = toByteArray(delta);
		// System.out.printf("write %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);
		data.write(dBytes);

		for (ForgeDirection dir : ForgeDirection.values()) {
			FluidStack liquid = renderCache[dir.ordinal()];

			if (delta.get(dir.ordinal() * 3 + 0)) {
				if (liquid != null) {
					data.writeInt(liquid.fluidID);
					data.writeInt(colorRenderCache[dir.ordinal()]);
				} else {
					data.writeInt(0);
					data.writeInt(0xFFFFFF);
				}
			}
			if (delta.get(dir.ordinal() * 3 + 2)) {
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
		byte[] bytes = new byte[3];
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
