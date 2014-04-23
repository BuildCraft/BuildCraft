package buildcraft.factory;

import buildcraft.BuildCraftTransport;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IGuiReturnHandler;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketGuiReturn;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.transport.ItemDiamondCanister;
import buildcraft.transport.ItemGoldCanister;
import buildcraft.transport.ItemIronCannister;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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

public class TileCanner extends TileBuildCraft implements IInventory, IFluidHandler, IGuiReturnHandler {

    private final SimpleInventory _inventory = new SimpleInventory(3, "Canner", 1);
    public final int maxLiquid = FluidContainerRegistry.BUCKET_VOLUME * 10;
    @MjBattery (maxCapacity = 5000.0, maxReceivedPerCycle = 25.0)
    public double energyStored = 0;
    public SingleUseTank tank = new SingleUseTank("tank", maxLiquid, this);
    private TankManager tankManager = new TankManager();
    public @NetworkData
    boolean fill;

    public TileCanner() {
        tankManager.add(tank);
    }

    @Override
    public void updateEntity() {
        sendNetworkUpdate();

        if (_inventory.getStackInSlot(0) != null && !tank.isEmpty()) {
            ItemFluidContainer item = null;
            if (_inventory.getStackInSlot(0).getItem() == BuildCraftTransport.ironCannister) {
                item = (ItemIronCannister) _inventory.getStackInSlot(0).getItem();
            }
            if (_inventory.getStackInSlot(0).getItem() == BuildCraftTransport.goldCanister) {
                item = (ItemGoldCanister) _inventory.getStackInSlot(0).getItem();
            }
            if (_inventory.getStackInSlot(0).getItem() == BuildCraftTransport.diamondCanister) {
                item = (ItemDiamondCanister) _inventory.getStackInSlot(0).getItem();
            }
            if (item != null) {
                int amount = 10;
                if (fill){
                    if (tank.getFluid().amount < 50)
                        amount = tank.getFluid().amount;
                    if (energyStored >= amount) {
                        tank.drain(item.fill(_inventory.getStackInSlot(0), new FluidStack(tank.getFluid(), amount), true), true);
                        energyStored = energyStored - amount;
                        FluidStack fluid = FluidUtils.getFluidStackFromItemStack(_inventory.getStackInSlot(0));
                        if (fluid != null) {
                            if ((item instanceof ItemIronCannister && fluid.amount == 1000)
                                    || (item instanceof ItemGoldCanister && fluid.amount == 3000)
                                    || (item instanceof ItemDiamondCanister && fluid.amount == 9000)){
                                _inventory.setInventorySlotContents(1, _inventory.getStackInSlot(0));
                                _inventory.setInventorySlotContents(0, null);
                            }
                        }
                    }
                } else {
                    if (_inventory.getStackInSlot(0) != null && !tank.isFull()){

                    }
                }
            }
        }

        System.out.println(fill);
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
        sendNetworkUpdate();
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
    public void writeGuiData(ByteBuf data) {}

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
        } catch (Exception e) {}
    }
}
