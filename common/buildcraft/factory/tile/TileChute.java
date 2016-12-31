package buildcraft.factory.tile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.factory.block.BlockChute;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileChute extends TileBC_Neptune implements ITickable, IDebuggable {
    public final ItemHandlerSimple inv;
    protected final MjBattery battery = new MjBattery(1000_000);
    protected int progress = 0;

    public TileChute() {
        inv = itemManager.addInvHandler("inv", 4, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    }

    public static boolean hasInventoryAtPosition(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        return ItemTransactorHelper.getTransactor(tile, side.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    private void putInInventories(EnumFacing currentSide) {
        List<EnumFacing> sides = Arrays.asList(EnumFacing.values());
        Collections.shuffle(sides, new Random());

        for (EnumFacing side : sides) {
            if (side == currentSide) {
                return;
            }

            TileEntity tile = world.getTileEntity(pos.offset(side));
            IItemTransactor transactor = ItemTransactorHelper.getTransactor(tile, side.getOpposite());

            if (transactor == NoSpaceTransactor.INSTANCE) {
                continue;
            }

            IStackFilter filter = (stack) -> {
                if (stack.isEmpty()) {
                    return false;
                }
                ItemStack leftOver = transactor.insert(stack.copy(), false, true);
                if (leftOver.isEmpty()) {
                    return true;
                }
                return leftOver.getCount() < stack.getCount();
            };

            ItemStack extracted = inv.extract(filter, 1, 1, false);

            transactor.insert(extracted, false, false);
        }
    }

    private void putItemsFromGround(EnumFacing currentSide) {
        int radius = 3;
        BlockPos offset = new BlockPos(currentSide.getDirectionVec());
        offset = new BlockPos(offset.getX() * radius, offset.getY() * radius, offset.getZ() * radius);
        AxisAlignedBB aabb = new AxisAlignedBB(this.pos, this.pos).expandXyz(radius).offset(offset);
        List<EntityItem> entityItems = world.getEntitiesWithinAABB(EntityItem.class, aabb);
        int index = 0, max = 3;
        for (EntityItem entityItem : entityItems) {
            ItemStack stack = entityItem.getEntityItem();
            stack = inv.insert(stack, false, false);
            if (stack.isEmpty()) {
                entityItem.setDead();
            } else {
                entityItem.setEntityItemStack(stack);
            }
            if (index++ >= max) {
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
        if (world.isRemote) {
            return;
        }

        if (!(world.getBlockState(pos).getBlock() instanceof BlockChute)) {
            return;
        }

        battery.tick(getWorld(), getPos());

        // test with the output of a stone engine
        battery.addPower(1000); // remove this

        EnumFacing currentSide = world.getBlockState(pos).getValue(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if (currentSide == EnumFacing.UP.getOpposite()) {
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
