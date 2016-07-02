package buildcraft.factory.tile;

import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.misc.FakePlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

public class TileMiningWell extends TileMiner {
    @Override
    protected void mine() {
        IBlockState state = worldObj.getBlockState(currentPos);
        if (BlockUtils.isUnbreakableBlock(getWorld(), currentPos) || state.getBlock() == Blocks.BEDROCK) {
            setComplete(true);
            return;
        }

        int target = BlockUtils.computeBlockBreakPower(worldObj, currentPos);
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            if (!worldObj.isAirBlock(currentPos)) {
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(worldObj, currentPos, state, FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) worldObj).get());
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (breakEvent.isCanceled()) {
                    setComplete(true);
                    return;
                }
                List<ItemStack> stacks = BlockUtils.getItemStackFromBlock((WorldServer) worldObj, currentPos, pos);
                if (stacks != null) {
                    for (ItemStack stack : stacks) {
                        stack.stackSize -= Utils.addToRandomInventoryAround(worldObj, pos, stack);
                        if (stack.stackSize > 0) {
                            stack.stackSize -= Utils.addToRandomInjectableAround(worldObj, pos, null, stack);
                        }
                        InvUtils.dropItemUp(getWorld(), stack, getPos());
                    }
                }
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                worldObj.destroyBlock(currentPos, false);
            }
            worldObj.setBlockState(currentPos, getBlockForDown().getDefaultState());
            worldObj.scheduleUpdate(currentPos, getBlockForDown(), 100);
            currentPos = currentPos.down();
            if (currentPos.getY() < 0) {
                setComplete(true);
            }
        } else {
            if (!worldObj.isAirBlock(currentPos)) {
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (progress * 9) / target);
            }
        }
    }

    @Override
    protected Block getBlockForDown() {
        return BCFactoryBlocks.plainPipe;
    }

    @Override
    public void onRemove() {
        super.onRemove();
        worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
    }
}
