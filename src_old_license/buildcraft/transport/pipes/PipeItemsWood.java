/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import cofh.api.energy.IEnergyReceiver;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.statements.ActionSingleEnergyPulse;

public class PipeItemsWood extends Pipe<PipeTransportItems> implements IEnergyReceiver {
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
                extractItems(maxExtractable());
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
                IItemHandler handler = InvUtils.getItemHandler(tile, side.getOpposite());

                if (handler != null) {
                    int stackSize = 0;
                    int maxItems = maxExtractable();
                    int[] extracted = getExtractionTargets(handler, maxItems);
                    if (extracted != null) {
                        for (int s : extracted) {
                            stackSize += handler.getStackInSlot(s).stackSize;
                        }
                    }

                    stackSize = Math.min(maxItems, stackSize);

                    if (battery.getEnergyStored() >= stackSize * 10) {
                        return true;
                    }
                }
            }

        }

        return ticksSincePull >= 16 && battery.getEnergyStored() >= 10;
    }

    @Override
    protected void actionsActivated(Collection<StatementSlot> actions) {
        super.actionsActivated(actions);
        for (StatementSlot slot : actions) {
            if (slot.statement instanceof ActionSingleEnergyPulse) {
                extractItems(1);
            }
        }
    }

    private int maxExtractable() {
        return battery.getEnergyStored() / 10;
    }

    private void extractItems(int maxItems) {
        int meta = container.getBlockMetadata();

        if (meta > 5) {
            return;
        }

        EnumFacing side = EnumFacing.getFront(meta);
        TileEntity tile = container.getTile(side);
        IItemHandler handler = InvUtils.getItemHandler(tile, side.getOpposite());

        if (handler != null) {
            int[] extracted = getExtractionTargets(handler, maxItems);
            if (extracted == null) {
                return;
            }

            tile.markDirty();

            for (int slotId : extracted) {
                ItemStack slot = handler.extractItem(slotId, maxItems, true);
                if (slot == null || slot.stackSize == 0) {
                    continue;
                }

                int stackSize = Math.min(slot.stackSize, maxItems);
                // TODO: Look into the Speed Multiplier again someday.
                // speedMultiplier = Math.min(4.0F, battery.getEnergyStored() * 10 / stackSize);
                int energyUsed = (int) (stackSize * 10 * speedMultiplier);
                if (battery.useEnergy(energyUsed, energyUsed, false) < energyUsed) {
                    continue;
                }

                TravelingItem entity = makeItem(-0.1f, slot);
                entity.setSpeed(TransportConstants.PIPE_DEFAULT_SPEED);

                if (transport.injectItem(entity, side.getOpposite(), false)) {
                    slot = handler.extractItem(slotId, maxItems, false);
                    if (slot == null || slot.stackSize == 0) {
                        continue;
                    }

                    entity = makeItem(-0.1f, slot);
                    entity.setSpeed(TransportConstants.PIPE_DEFAULT_SPEED);

                    if (!transport.injectItem(entity, side.getOpposite(), true)) {
                        dropItem(slot);
                    }
                }
            }
        }
    }

    protected TravelingItem makeItem(float pos, ItemStack stack) {
        return TravelingItem.make(pos, stack);
    }

    /** Return the itemstack that can be if something can be extracted from this inventory, null if none. On certain
     * cases, the extractable slot depends on the position of the pipe. */
    public int[] getExtractionTargets(IItemHandler handler, int maxItems) {
        int result = getExtractionTargetsGeneric(handler, maxItems);

        if (result >= 0) {
            return new int[] { result };
        }

        return null;
    }

    public int  getExtractionTargetsGeneric(IItemHandler handler, int maxItems) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);

            if (slot != null && slot.stackSize > 0) {
                ItemStack stack = handler.extractItem(i, maxItems, true);
                if (stack != null) {
                    return i;
                }
            }
        }

        return -1;
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
    public int getEnergyStored(EnumFacing from) {
        return battery.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return battery.getMaxEnergyStored();
    }
}
