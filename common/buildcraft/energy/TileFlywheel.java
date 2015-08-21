package buildcraft.energy;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.utils.AverageInt;

public class TileFlywheel extends TileBuildCraft implements IEnergyHandler {
	private static final int[] ROTATE_RIGHT = {0, 1, 4, 5, 3, 2, 6, 7};
	private static final float MAX_ROTATION = 360.0f;
	private long lastReceivedTick = -1;
	private float tickInvDelta = 0.0f;
	private float tickInvDeltaAvg = 0.0f;
	private float tickClientRot = 0.0f;
	private float tickClientRotDelta = 0.0f;
	private float tickClientRotPow = 0.0f;
	private AverageInt tickPower = new AverageInt(3);

	public TileFlywheel() {
		super();
		setBattery(new RFBattery(20000, 5000, 5000));
	}

	@SideOnly(Side.CLIENT)
	public float getClientRotation(float partialTicks) {
		return tickClientRot + (tickClientRotDelta * partialTicks);
	}

	private ForgeDirection getRightSide() {
		return ForgeDirection.getOrientation(ROTATE_RIGHT[((BlockBuildCraft) getBlockType()).getFrontSide(getBlockMetadata()) & 7]);
	}

	private Object getOutTile() {
		ForgeDirection rightDir = getRightSide();
		TileEntity teRight = worldObj.getTileEntity(xCoord + rightDir.offsetX, yCoord + rightDir.offsetY, zCoord + rightDir.offsetZ);
		if (teRight instanceof IEnergyReceiver || teRight instanceof IEnergyHandler) {
			return teRight;
		}
		return null;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (tickInvDelta > 0) {
			tickInvDeltaAvg = (tickInvDeltaAvg * 39 + tickInvDelta * 1) / 40;
		}

		if (worldObj.isRemote) {
			// update client rotation
			float rotationAmount = tickInvDeltaAvg * MAX_ROTATION * (Math.min(60.0f, tickClientRotPow) / 40.0f);
			if (tickInvDelta == 0.0f && tickClientRot != 0.0f) {
				tickClientRotDelta = 0.0f;
				if (tickClientRot >= 359.0f) {
					tickClientRot = 0.0f;
				} else {
					tickClientRot = tickClientRot + Math.max(1f, (MAX_ROTATION - tickClientRot) / 2);
				}
			} else {
				tickClientRotDelta = rotationAmount;
				tickClientRot = (tickClientRot + tickClientRotDelta) % MAX_ROTATION;
			}
			return;
		}

		Object outTile = getOutTile();
		if (outTile != null) {
			float tickRate = tickInvDeltaAvg > 0.0f ? 1.0f / tickInvDeltaAvg : 200.0f;
			int minPow = Math.max(Math.max(2, getBattery().getEnergyStored() / 200), (int) Math.round(tickPower.getAverage() / tickRate));
			int power = getBattery().extractEnergy(minPow, true);
			if (outTile instanceof IEnergyReceiver) {
				power = ((IEnergyReceiver) outTile).receiveEnergy(getRightSide().getOpposite(), power, false);
				getBattery().extractEnergy(power, false);
			} else if (outTile instanceof IEnergyHandler) {
				power = ((IEnergyHandler) outTile).receiveEnergy(getRightSide().getOpposite(), power, false);
				getBattery().extractEnergy(power, false);
			}
		} else {
			getBattery().useEnergy(0, Math.max(10, getBattery().getEnergyStored() / 200), false);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound cpd) {
		super.writeToNBT(cpd);
		cpd.setFloat("tid", tickInvDelta);
		cpd.setFloat("tida", tickInvDeltaAvg);
		cpd.setLong("lrt", lastReceivedTick);
		cpd.setInteger("tp", (int) Math.round(tickPower.getAverage()));
	}

	@Override
	public void readFromNBT(NBTTagCompound cpd) {
		super.readFromNBT(cpd);
		tickInvDelta = cpd.getFloat("tid");
		tickInvDeltaAvg = cpd.getFloat("tida");
		lastReceivedTick = cpd.getLong("lrt");
		for (int i = 0; i < 3; i++) {
			tickPower.push(cpd.getInteger("tp"));
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeFloat(tickInvDelta);
		buf.writeFloat((float) tickPower.getAverage() * tickInvDelta);
	}

	@Override
	public void readData(ByteBuf buf) {
		tickInvDelta = buf.readFloat();
		tickClientRotPow = buf.readFloat();
	}

	@Override
	public int receiveEnergy(ForgeDirection side, int maxReceive, boolean simulate) {
		if (side != getRightSide().getOpposite()) {
			return 0;
		}

		int out = super.receiveEnergy(side, maxReceive, simulate);
		if (out > 0 && !simulate) {
			float oldAvgPower = (float) tickPower.getAverage();
			tickPower.tick(out);
			float avgPower = (float) tickPower.getAverage();

			float oldDelta = tickInvDelta;
			if (lastReceivedTick >= 0) {
				int newDelta = (int) Math.min(200, worldObj.getTotalWorldTime() - lastReceivedTick);
				tickInvDelta = 1.0f / (float) newDelta;
			} else {
				tickInvDelta = 0.0f;
			}
			lastReceivedTick = worldObj.getTotalWorldTime();
			if (oldDelta != tickInvDelta || oldAvgPower != avgPower) {
				sendNetworkUpdate();
			}
		}
		return out;
	}

	@Override
	public boolean emitsEnergy(ForgeDirection side) {
		return side == getRightSide();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection side) {
		int sideO = side.ordinal();
		int frontO = ((BlockBuildCraft) getBlockType()).getFrontSide(getBlockMetadata());
		if ((frontO & 6) == 2) { // X
			return sideO == 4 || sideO == 5;
		} else if ((frontO & 6) == 4) { // Z
			return sideO == 2 || sideO == 3;
		} else {
			return false;
		}
	}
}
