package buildcraft.factory.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReciver;

public class TileMiningWell extends TileMiner {
    @Override
    protected void mine() {
        IBlockState state = world.getBlockState(currentPos);
        if (BlockUtil.isUnbreakableBlock(getWorld(), currentPos, getOwner()) || state.getBlock() == Blocks.BEDROCK) {
            setComplete(true);
            return;
        }

        long target = BlockUtil.computeBlockBreakPower(world, currentPos);
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            if (!world.isAirBlock(currentPos)) {
                EntityPlayer fakePlayer = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, getPos(), getOwner());
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, currentPos, state, fakePlayer);
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (breakEvent.isCanceled()) {
                    setComplete(true);
                    return;
                }
                NonNullList<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) world, currentPos, getOwner());
                if (stacks != null) {
                    for (ItemStack stack : stacks) {
                        InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, stack);
                    }
                }
                world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                world.destroyBlock(currentPos, false);
            }
            currentPos = currentPos.down();
            if (currentPos.getY() < 0) {
                setComplete(true);
            } else {
                goToYLevel(currentPos.getY());
            }
        } else {
            if (!world.isAirBlock(currentPos)) {
                world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
            }
        }
    }

    // @Override
    // public double getTubeOffset() {
    // return -0.8;
    // }

    @Override
    public void onRemove() {
        super.onRemove();
        world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReciver(battery);
    }
}
