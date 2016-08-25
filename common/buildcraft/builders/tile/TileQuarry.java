package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.core.Box;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.BoxIterator;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

public class TileQuarry extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    private final Box box = new Box();
    private BlockPos min;
    private BlockPos max;
    private BoxIterator boxIterator;
    public final IItemHandlerModifiable invFrames = addInventory("frames", 9, ItemHandlerManager.EnumAccess.NONE, EnumPipePart.VALUES);

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.worldObj.isRemote) {
            return;
        }
        EnumFacing facing = worldObj.getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = worldObj.getTileEntity(areaPos);
        if(tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            min = provider.min();
            max = provider.max();
            box.setMin(new BlockPos(min.getX(), 0, min.getZ()));
            box.setMax(new BlockPos(max.getX(), min.getY() - 1, max.getZ()));
            provider.removeFromWorld();
        }
    }

    @Override
    public void update() {
        if(worldObj.isRemote) {
            return;
        }

        if(min == null || max == null) {
            return;
        }

        for(int x = min.getX(); x <= max.getX(); x++) {
            for(int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos pos = new BlockPos(x, min.getY(), z);
                boolean shouldBeFrame = x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ();
                Block block = worldObj.getBlockState(pos).getBlock();
                if((block != Blocks.AIR && !shouldBeFrame) || (block != BCBuildersBlocks.frame && block != Blocks.AIR && shouldBeFrame)) {
                    if(worldObj.destroyBlock(pos, true)) {
                        return;
                    }
                }
                if(shouldBeFrame && block == Blocks.AIR) {
                    boolean found = false;
                    for(int i = 8; i >= 0; i--) {
                        ItemStack stackInSlot = invFrames.getStackInSlot(i);
                        if(stackInSlot != null) {
                            worldObj.setBlockState(pos, BCBuildersBlocks.frame.getDefaultState());
                            invFrames.setStackInSlot(i, stackInSlot.stackSize > 0 ? new ItemStack(stackInSlot.getItem(), stackInSlot.stackSize - 1) : null);
                            found = true;
                            break;
                        }
                    }
                    if(found) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("box", box.writeToNBT());
        nbt.setTag("min", NBTUtils.writeBlockPos(min));
        nbt.setTag("max", NBTUtils.writeBlockPos(max));
        if(boxIterator != null) {
            nbt.setTag("box_iterator", boxIterator.writeToNBT());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        box.initialize(nbt.getCompoundTag("box"));
        min = NBTUtils.readBlockPos(nbt.getTag("min"));
        max = NBTUtils.readBlockPos(nbt.getTag("max"));
        boxIterator = new BoxIterator().readFromNBT(nbt.getCompoundTag("box_iterator"));
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("min = " + min);
        left.add("max = " + max);
        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
    }
}
