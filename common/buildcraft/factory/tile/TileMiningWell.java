package buildcraft.factory.tile;

import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

public class TileMiningWell extends TileBC_Neptune implements ITickable {
    private int progress = 0;
    private BlockPos currentPos = null;

    private void updatePos() {
        if(currentPos == null) {
            currentPos = pos;
            currentPos = new BlockPos(currentPos.getX(), currentPos.getY() - 1, currentPos.getZ());
        }
    }

    @Override
    public void update() {
        updatePos();
        if(worldObj.isRemote) {
            return;
        }
        float hardness = worldObj.getBlockState(currentPos).getBlockHardness(worldObj, currentPos);
        if(hardness == 0) {
            hardness = 0.1F;
        }
        progress += 10 / hardness;
        if(progress > 100) {
            progress = 0;
            if(!worldObj.isAirBlock(currentPos)) {
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(worldObj, currentPos, worldObj.getBlockState(currentPos), FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) worldObj).get());
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if(breakEvent.isCanceled()) {
                    return;
                }
                List<ItemStack> stacks = BlockUtils.getItemStackFromBlock((WorldServer) worldObj, currentPos, pos);
                if(stacks != null) {
                    for(ItemStack stack : stacks) {
                        stack.stackSize -= Utils.addToRandomInventoryAround(worldObj, pos, stack);
                        if(stack.stackSize > 0) {
                            stack.stackSize -= Utils.addToRandomInjectableAround(worldObj, pos, null, stack);
                        }
                        if(stack.stackSize > 0) {
                            float y = worldObj.rand.nextFloat() * 1.0F - 0.5F + 0.5F;
                            float x = worldObj.rand.nextFloat() * 1.0F - 0.5F + 0.5F;
                            float z = worldObj.rand.nextFloat() * 1.0F - 0.5F + 0.5F;
                            EntityItem entityItem = new EntityItem(worldObj, pos.getX() + x, pos.getY() + y + 0.5F, pos.getZ() + z, stack);
                            entityItem.setDefaultPickupDelay();
                            entityItem.motionX = entityItem.motionY = entityItem.motionZ = 0;
                            entityItem.motionX = (float) worldObj.rand.nextGaussian() * 0.02F - 0.01F;
                            entityItem.motionY = (float) worldObj.rand.nextGaussian() * 0.02F;
                            entityItem.motionZ = (float) worldObj.rand.nextGaussian() * 0.02F - 0.01F;
                            worldObj.spawnEntityInWorld(entityItem);
                        }
                    }
                }
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                worldObj.destroyBlock(currentPos, false);
            }
            worldObj.setBlockState(currentPos, BCFactoryBlocks.plainPipe.getDefaultState());
            worldObj.scheduleUpdate(currentPos, BCFactoryBlocks.plainPipe, 100);
            currentPos = new BlockPos(currentPos.getX(), currentPos.getY() - 1, currentPos.getZ());
        } else {
            if(!worldObj.isAirBlock(currentPos)) {
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) (progress / 100F * 9));
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        currentPos = new BlockPos(
                nbt.getInteger("currentX"),
                nbt.getInteger("currentY"),
                nbt.getInteger("currentZ")
        );
        progress = nbt.getInteger("progress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        updatePos();
        nbt.setInteger("currentX", currentPos.getX());
        nbt.setInteger("currentY", currentPos.getY());
        nbt.setInteger("currentZ", currentPos.getZ());
        nbt.setInteger("progress", progress);
        return nbt;
    }

    @Override
    public void onRemove() {
        worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
        for(int y = currentPos.getY(); y < pos.getY(); y++) {
            BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
            if(worldObj.getBlockState(p).getBlock() == BCFactoryBlocks.plainPipe) {
                worldObj.destroyBlock(p, false);
            }
        }
    }
}
