package buildcraft.factory;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.ItemFluidContainer;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.ItemDiamondCanister;
import buildcraft.core.ItemGoldCanister;
import buildcraft.core.ItemIronCannister;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;

public class TileCanner extends TileBuildCraft implements IInventory, IFluidHandler{
	
	private final SimpleInventory _inventory = new SimpleInventory(3, "Canner", 1);
	public final int maxLiquid = FluidContainerRegistry.BUCKET_VOLUME * 10;
	@MjBattery (maxCapacity = 5000.0, maxReceivedPerCycle = 25.0)
	public double energyStored = 0;
	public SingleUseTank tank = new SingleUseTank("tank", maxLiquid, this);
	private TankManager tankManager = new TankManager();
	
	public TileCanner(){
		tankManager.add(tank);
	}
	
	@Override
	public void updateEntity() {
		if (_inventory.getStackInSlot(0) != null && tank.getFluid() != null){
			ItemFluidContainer item = null;
			if (_inventory.getStackInSlot(0).getItem() == BuildCraftCore.ironCannister){
				item = (ItemIronCannister) _inventory.getStackInSlot(0).getItem();
				ItemIronCannister item2 = (ItemIronCannister) item;
			}
			if (_inventory.getStackInSlot(0).getItem() == BuildCraftCore.goldCanister){
				item = (ItemGoldCanister) _inventory.getStackInSlot(0).getItem();
			}
			if (_inventory.getStackInSlot(0).getItem() == BuildCraftCore.diamondCanister){
				item = (ItemDiamondCanister) _inventory.getStackInSlot(0).getItem();
			}
			if (item != null){
				int amount = 10;
				if (tank.getFluid().amount <50)
					amount = tank.getFluid().amount;
				if (energyStored >= amount){
					FluidStack before = tank.getFluid();
					tank.drain(item.fill(_inventory.getStackInSlot(0), new FluidStack(tank.getFluid(), amount), true), true);
					energyStored = energyStored - amount;
					if (tank.getFluid() != null){
						if (item.fill(_inventory.getStackInSlot(0), new FluidStack(tank.getFluid(), 1), false) == 0){
							_inventory.setInventorySlotContents(1, _inventory.getStackInSlot(0));
							_inventory.setInventorySlotContents(0, null);
						}
					}
					sendNetworkUpdate();
					}
				}
			}
		}
		
	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);
		NBTTagCompound p = (NBTTagCompound) nbtTagCompound.getTag("inventory");
		_inventory.readFromNBT(p);
		tankManager.readFromNBT(nbtTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);
		NBTTagCompound inventoryTag = new NBTTagCompound();
		_inventory.writeToNBT(inventoryTag);
		nbtTagCompound.setTag("inventory", inventoryTag);
		tankManager.writeToNBT(nbtTagCompound);
	}

	@Override
	public int getSizeInventory() {
		return _inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return _inventory.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return _inventory.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return _inventory.getStackInSlotOnClosing(var1);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		_inventory.setInventorySlotContents(slotId, itemstack);		
	}

	@Override
	public String getInventoryName() {
		return _inventory.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return _inventory.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return _inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && entityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int slotid, ItemStack itemStack) {
		return _inventory.isItemValidForSlot(slotid, itemStack);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		int temp = tank.fill(resource, doFill);
		this.sendNetworkUpdate();
		return temp;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		FluidStack temp = tank.drain(maxDrain, doDrain);
		this.sendNetworkUpdate();
		return temp;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return tank.getFluidType() == fluid;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return tankManager.getTankInfo(from);
	}
	public FluidStack getFluid(){
		return tank.getFluid();
	}
	
	public int getScaledLiquid(int i) {
		return tank.getFluid() != null ? (int) (((float) this.tank.getFluid().amount / (float) (maxLiquid)) * i) : 0;
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
