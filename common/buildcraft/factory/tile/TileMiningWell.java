package buildcraft.factory.tile;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReciver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class TileMiningWell extends TileMiner {
    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            long target = BlockUtil.computeBlockBreakPower(world, currentPos);
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                EntityPlayer fakePlayer = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, getPos(), getOwner());
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, currentPos, world.getBlockState(currentPos), fakePlayer);
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (!breakEvent.isCanceled()) {
                    NonNullList<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) world, currentPos, getOwner());
                    if (stacks != null) {
                        for (ItemStack stack : stacks) {
                            InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, stack);
                        }
                    }
                    world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                    world.destroyBlock(currentPos, false);
                }
                nextPos();
                updateLength();
            } else {
                if (!world.isAirBlock(currentPos)) {
                    world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else {
            nextPos();
            updateLength();
        }
    }

    private boolean canBreak() {
        return !world.isAirBlock(currentPos) && !BlockUtil.isUnbreakableBlock(getWorld(), currentPos, getOwner());
    }

    private void nextPos() {
        for (currentPos = pos.down(); currentPos.getY() >= 0; currentPos = currentPos.down()) {
            if (canBreak()) {
                updateLength();
                return;
            } else if (!world.isAirBlock(currentPos) && world.getBlockState(currentPos).getBlock() != BCFactoryBlocks.tube) {
                break;
            }
        }
        updateLength();
        currentPos = null;
    }

    @Override
    protected void initCurrentPos() {
        if (currentPos == null) {
            nextPos();
        }
    }

    @Override
    public void invalidate() {
        if (currentPos != null) {
            world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
        }
        super.invalidate();
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReciver(battery);
    }
}
