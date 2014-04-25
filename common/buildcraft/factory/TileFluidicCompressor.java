package buildcraft.factory;

import buildcraft.api.mj.MjBattery;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.Tank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IGuiReturnHandler;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketGuiReturn;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.utils.Utils;
import buildcraft.transport.ItemCanister;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.ItemFluidContainer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TileFluidicCompressor extends TileBuildCraft implements
		ISidedInventory, IFluidHandler, IGuiReturnHandler {

	private final SimpleInventory _inventory = new SimpleInventory(3, "Canner", 1);
	public final int maxLiquid = FluidContainerRegistry.BUCKET_VOLUME * 10;
	@MjBattery(maxCapacity = 5000.0, maxReceivedPerCycle = 25.0)
	public double energyStored = 0;
	public Tank tank = new Tank("tank", maxLiquid, this);
	private TankManager tankManager = new TankManager();
	public @NetworkData
	boolean fill;

	public TileFluidicCompressor() {
		tankManager.add(tank);
	}

	@Override
	public void updateEntity() {
		ItemStack itemstack = _inventory.getStackInSlot(0);
		if (itemstack != null) {
			ItemFluidContainer item = null;
			Item itemInSlot = itemstack.getItem();
			if (itemInSlot instanceof ItemCanister) {
				item = (ItemCanister) itemstack.getItem();
			}
			if (item != null) {
				int amount = 50;
				if (fill && !tank.isEmpty()) {
					if (tank.getFluid().amount < 25)
						amount = tank.getFluid().amount;
					if (energyStored >= amount) {
						tank.drain(item.fill(itemstack, new FluidStack(tank.getFluid(), amount), true), true);
						energyStored = energyStored - amount;
						FluidStack fluid = FluidUtils.getFluidStackFromItemStack(itemstack);
						if (fluid != null) {
							if (getProgress() == 16 && _inventory.getStackInSlot(1) == null) {
								_inventory.setInventorySlotContents(1, itemstack);
								_inventory.setInventorySlotContents(0, null);
							}
						}
					}
				} else {
					amount = 50;
					if (!fill && !tank.isFull() && FluidUtils.getFluidStackFromItemStack(itemstack) != null) {
						if (!tank.isEmpty()) {
							if ((tank.getCapacity() - tank.getFluid().amount) < 50) {
								amount = tank.getCapacity() - tank.getFluid().amount;
							}
						}
						if (amount > FluidUtils.getFluidStackFromItemStack(itemstack).amount) {
							amount = FluidUtils.getFluidStackFromItemStack(itemstack).amount;
						}
						tank.fill(item.drain(itemstack, amount, true), true);
						if (getProgress() == 16&& _inventory.getStackInSlot(1) == null) {
							itemstack.getTagCompound().removeTag("Fluid");
							_inventory.setInventorySlotContents(1, itemstack);
							_inventory.setInventorySlotContents(0, null);
						}
					}
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
		fill = nbtTagCompound.getBoolean("fill");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);
		NBTTagCompound inventoryTag = new NBTTagCompound();
		_inventory.writeToNBT(inventoryTag);
		nbtTagCompound.setTag("inventory", inventoryTag);
		tankManager.writeToNBT(nbtTagCompound);
		nbtTagCompound.setBoolean("fill", fill);
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
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D,
						zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slotid, ItemStack itemStack) {
		if (itemStack == null)
			return false;
		Item item = itemStack.getItem();
		if (slotid == 0) {
			if (item instanceof ItemCanister) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
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

	public FluidStack getFluid() {
		return tank.getFluid();
	}

	public int getScaledLiquid(int i) {
		return tank.getFluid() != null ? (int) (((float) this.tank.getFluid().amount / (float) (maxLiquid)) * i)
				: 0;
	}

	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(
				new PacketPayload.StreamWriter() {
					@Override
					public void writeData(ByteBuf data) {
						tankManager.writeData(data);
						data.writeBoolean(fill);
					}
				});
		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		ByteBuf stream = packet.payload.stream;
		tankManager.readData(stream);
		fill = stream.readBoolean();
	}

	@Override
	public void writeGuiData(ByteBuf data) {
	}

	@Override
	public void readGuiData(ByteBuf data, EntityPlayer player) {
		fill = data.readBoolean();
	}

	public void sendModeUpdatePacket() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeBoolean(fill);
			PacketGuiReturn pkt = new PacketGuiReturn(this, bytes.toByteArray());
			pkt.sendPacket();
		} catch (Exception e) {
		}
	}

	public int getProgress() {
		ItemStack itemstack = _inventory.getStackInSlot(0);
		if (itemstack != null) {
			Item item = itemstack.getItem();
			if (item instanceof ItemCanister) {
				FluidStack fluidstack = FluidUtils
						.getFluidStackFromItemStack(itemstack);
				ItemCanister canister = (ItemCanister) itemstack.getItem();
				if (fluidstack != null) {
					int capacity = canister.getCapacity(itemstack);
					if (fill)
						return (fluidstack.amount * 16)/ capacity;
					return ((capacity - fluidstack.amount) * 16) / capacity;
				}
			}
			if (!fill) {
				return 16;
			}
		}
		return 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return Utils.createSlotArray(0, 1);
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return side != 0 && slot != 2 && isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return side != 1 && slot == 1;
	}

	public double getEnergyStored() {
		return energyStored;
	}

	public int getFluidStored() {
		if (tank.getFluid() != null) {
			return tank.getFluid().amount;
		}
		return 0;
	}
}
