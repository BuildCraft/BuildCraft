/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.InventoryWrapper;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.*;

public class PipeItemsWood extends Pipe<PipeTransportItems> implements IEnergyHandler {
    protected RFBattery battery = new RFBattery(2560, 80, 0);

    protected int standardIconIndex = PipeIconProvider.TYPE.PipeItemsWood_Standard.ordinal();
    protected int solidIconIndex = PipeIconProvider.TYPE.PipeItemsWood_Solid.ordinal();
    protected float speedMultiplier = 1.0F;

    private int ticksSincePull = 0;

    private PipeLogicWood logic = new PipeLogicWood(this) {
        @Override
        protected boolean isValidConnectingTile(TileEntity tile) {
            if (tile instanceof IPipeTile) {
                return false;
            }
            if (!(tile instanceof IInventory)) {
                return false;
            }
            return true;
        }
    };

    public PipeItemsWood(Item item) {
        super(new PipeTransportItems(), item);
    }

    @Override
    public boolean blockActivated(EntityPlayer entityplayer, EnumFacing side) {
        return logic.blockActivated(entityplayer, EnumPipePart.fromFacing(side));
    }

    @Override
    public void onNeighborBlockChange(int blockId) {
        logic.onNeighborBlockChange();
        super.onNeighborBlockChange(blockId);
    }

    @Override
    public void initialize() {
        logic.initialize();
        super.initialize();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        if (direction == null) {
            return standardIconIndex;
        } else {
            int metadata = container.getBlockMetadata();

            if (metadata == direction.ordinal()) {
                return solidIconIndex;
            } else {
                return standardIconIndex;
            }
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (container.getWorld().isRemote) {
            return;
        }

        ticksSincePull++;

        if (shouldTick()) {
            if (transport.getNumberOfStacks() < PipeTransportItems.MAX_PIPE_STACKS) {
                extractItems();
            }

            battery.setEnergy(0);
            ticksSincePull = 0;
            speedMultiplier = 1.0F;

            onPostTick();
        }
    }

    public void onPostTick() {

    }

    private boolean shouldTick() {
        if (ticksSincePull < 8) {
            return false;
        } else if (ticksSincePull < 16) {
            // Check if we have just enough energy for the next stack.
            int meta = container.getBlockMetadata();

            if (meta <= 5) {
                EnumFacing side = EnumFacing.getFront(meta);
                TileEntity tile = container.getTile(side);

                if (tile instanceof IInventory) {
                    int stackSize = 0;
                    IInventory inventory = (IInventory) tile;
                    ItemStack[] extracted = checkExtract(inventory, false, side.getOpposite());
                    if (extracted != null) {
                        for (ItemStack s : extracted) {
                            stackSize += s.stackSize;
                        }
                    }

                    if (battery.getEnergyStored() >= stackSize * 10) {
                        return true;
                    }
                }
            }

        }

        return ticksSincePull >= 16 && battery.getEnergyStored() >= 10;
    }

    private void extractItems() {
        int meta = container.getBlockMetadata();

        if (meta > 5) {
            return;
        }

        EnumFacing side = EnumFacing.getFront(meta);
        TileEntity tile = container.getTile(side);

        if (tile instanceof IInventory) {
            IInventory inventory = (IInventory) tile;

            ItemStack[] extracted = checkExtract(inventory, true, side.getOpposite());
            if (extracted == null) {
                return;
            }

            tile.markDirty();

            for (ItemStack stack : extracted) {
                if (stack == null || stack.stackSize == 0) {
                    // battery.useEnergy(10, 10, false);

                    continue;
                }

                Vec3 entPos = Utils.convertMiddle(tile.getPos()).add(Utils.convert(side, -0.6));

                TravelingItem entity = makeItem(entPos, stack);
                entity.setSpeed(TransportConstants.PIPE_DEFAULT_SPEED);
                transport.injectItem(entity, side.getOpposite());
            }
        }
    }

    protected TravelingItem makeItem(Vec3 pos, ItemStack stack) {
        return TravelingItem.make(pos, stack);
    }

    /** Return the itemstack that can be if something can be extracted from this inventory, null if none. On certain
     * cases, the extractable slot depends on the position of the pipe. */
    public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, EnumFacing from) {
        IInventory inv = InvUtils.getInventory(inventory);
        ItemStack result = checkExtractGeneric(inv, doRemove, from);

        if (result != null) {
            return new ItemStack[] { result };
        }

        return null;
    }

    public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, EnumFacing from) {
        return checkExtractGeneric(InventoryWrapper.getWrappedInventory(inventory), doRemove, from);
    }

    public ItemStack checkExtractGeneric(ISidedInventory inventory, boolean doRemove, EnumFacing from) {
        if (inventory == null) {
            return null;
        }

        for (int k : inventory.getSlotsForFace(from)) {
            ItemStack slot = inventory.getStackInSlot(k);

            if (slot != null && slot.stackSize > 0 && inventory.canExtractItem(k, slot, from)) {
                if (doRemove) {
                    int maxStackSize = slot.stackSize;
                    int stackSize = Math.min(maxStackSize, battery.getEnergyStored() / 10);
                    // TODO: Look into the Speed Multiplier again someday.
                    // speedMultiplier = Math.min(4.0F, battery.getEnergyStored() * 10 / stackSize);
                    int energyUsed = (int) (stackSize * 10 * speedMultiplier);
                    battery.useEnergy(energyUsed, energyUsed, false);

                    return inventory.decrStackSize(k, stackSize);
                } else {
                    return slot;
                }
            }
        }

        return null;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return battery.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return battery.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return battery.getMaxEnergyStored();
    }
}
