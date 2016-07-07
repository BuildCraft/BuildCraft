package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.factory.block.BlockChute;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class TileChute extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    public final ItemHandlerSimple inv;
    protected final MjBattery battery = new MjBattery(1000_000);
    protected int progress = 0;

    public TileChute() {
        inv = addInventory("inv", 4, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    }

    private boolean isInventoryFull(IInventory inventory, EnumFacing side) {
        if(inventory instanceof ISidedInventory) {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;
            int[] slots = sidedInventory.getSlotsForFace(side);

            for(int i = 0; i < slots.length; i++) {
                ItemStack stackInSlot = sidedInventory.getStackInSlot(slots[i]);

                if(stackInSlot == null || stackInSlot.stackSize != stackInSlot.getMaxStackSize()) {
                    return false;
                }
            }
        } else {
            for(int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack itemstack = inventory.getStackInSlot(i);

                if(itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
                    return false;
                }
            }
        }

        return true;
    }

    public static IInventory getInventoryAtPosition(IBlockAccess world, BlockPos pos) {
        IInventory inventory = null;
        Block block = world.getBlockState(pos).getBlock();

        if(block.hasTileEntity()) {
            TileEntity tile = world.getTileEntity(pos);

            if(tile instanceof IInventory) {
                inventory = (IInventory) tile;
            }
        }

        return inventory;
    }

    private static ItemStack putStackInInventory(EnumFacing side, IInventory inventory, ItemStack itemStack) {
        int slotIndexes[] = IntStream.rangeClosed(0, inventory.getSizeInventory() - 1).toArray();
        if(inventory instanceof ISidedInventory) {
            slotIndexes = ((ISidedInventory) inventory).getSlotsForFace(side);
        }
        for(int j : slotIndexes) {
            ItemStack stack = inventory.getStackInSlot(j);
            if(StackUtil.canMerge(itemStack, stack) || stack == null) {
                if(stack == null) {
                    stack = itemStack.copy();
                    stack.stackSize = 0;
                }
                int oldSize = stack.stackSize;
                int maxTransfer = 1;
                stack.stackSize = MathUtils.clamp(stack.stackSize + Math.min(itemStack.stackSize, maxTransfer), 0, itemStack.getMaxStackSize());
                int used = stack.stackSize - oldSize;
                itemStack.stackSize -= used;
                if(stack.stackSize != 0) {
                    inventory.setInventorySlotContents(j, stack);
                } else {
                    inventory.setInventorySlotContents(j, null);
                }
                break;
            }
        }
        return itemStack;
    }

    private void putInInventory(EnumFacing side, IInventory inventory) {
        for(int i = 0; i < 4; i++) {
            if(inv.getStackInSlot(i) != null) {
                ItemStack itemStack = inv.getStackInSlot(i).copy();
                itemStack = putStackInInventory(side, inventory, itemStack);
                inv.setStackInSlot(i, itemStack);
            }
        }
    }

    private void putInInventories(EnumFacing currentSide) {
        List<EnumFacing> sides = Arrays.asList(EnumFacing.values());
        Collections.shuffle(sides, new Random());

        for(EnumFacing side : sides) {
            if(side == currentSide) {
                return;
            }

            IInventory inventory = getInventoryAtPosition(worldObj, pos.offset(side));

            if(inventory == null) {
                return;
            }

            if(isInventoryFull(inventory, side)) {
                return;
            }

            putInInventory(side, inventory);
        }
    }

    private void putItemsFromGround(EnumFacing currentSide) {
        int radius = 3;
        BlockPos offset = new BlockPos(currentSide.getDirectionVec());
        offset = new BlockPos(offset.getX() * radius, offset.getY() * radius, offset.getZ() * radius);
        AxisAlignedBB aabb = new AxisAlignedBB(this.pos, this.pos).expandXyz(radius).offset(offset);
        List<EntityItem> entityItems = worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
        int index = 0, max = 3;
        for(EntityItem entityItem : entityItems) {
            ItemStack stack = entityItem.getEntityItem();
            stack = inv.insert(stack, false, false);
            if(stack == null) {
                entityItem.setDead();
            } else {
                entityItem.setEntityItemStack(stack);
            }
            if(index++ >= max) {
                break;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("progress");
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", progress);
        nbt.setTag("mj_battery", battery.serializeNBT());
        return nbt;
    }

    // ITickable

    @Override
    public void update() {
        if(worldObj.isRemote) {
            return;
        }

        if(!(worldObj.getBlockState(pos).getBlock() instanceof BlockChute)) {
            return;
        }

        battery.tick(getWorld(), getPos());

        // test with the output of a stone engine
        battery.addPower(1000); // remove this

        EnumFacing currentSide = worldObj.getBlockState(pos).getValue(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if(currentSide == EnumFacing.UP.getOpposite()) {
            progress += 1000; // can be free because of gravity
        }
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            putItemsFromGround(currentSide);
        }

        putInInventories(currentSide);
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("progress = " + progress);
    }
}
