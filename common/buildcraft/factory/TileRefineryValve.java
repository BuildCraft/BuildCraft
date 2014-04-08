package buildcraft.factory;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;

public class TileRefineryValve extends TileBuildCraft implements IFluidHandler{
	
	public int type = 0;
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 10;
	public SingleUseTank tank = new SingleUseTank("tank", MAX_LIQUID, this);
	private TankManager tankManager = new TankManager();
	
	public TileRefineryValve(){
		tankManager.add(tank);
	}
	
	public void markNeutral(){
		type = 0;
	}
	
	public void markAsInput(){
		type = 1;
	}
	
	public void markAsOutput(){
		type = 2;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (type == 1 && resource.getFluid() == BuildCraftEnergy.fluidOil){
			int t = tank.fill(resource, doFill);
			sendNetworkUpdate();
			
			return t;
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (type == 2){
			FluidStack t = tank.drain(maxDrain, doDrain);
			sendNetworkUpdate();
			return t;
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (type == 1 && fluid == BuildCraftEnergy.fluidOil){
			return true;
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if (type == 2){
			return true;
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return tankManager.getTankInfo(from);
	}
	
	public int getScaledFluid(int i) {
		return this.tank.getFluid() != null ? (int) (((float) this.tank.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}
	
	public int getAmountOfLiquid(){
		if (tank.isEmpty()){
			return 0;
		}
		return tank.getFluidAmount();
	}
	
	public FluidStack getLiquid(){
		return tank.getFluid();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tankManager.readFromNBT(data);
		type = data.getInteger("type");

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tankManager.writeToNBT(data);
		data.setInteger("type", type);
	}
	
	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(new PacketPayload.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
				tankManager.writeData(data);
			}
		});
		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		ByteBuf stream = packet.payload.stream;
		tankManager.readData(stream);
	}

}
