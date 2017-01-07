package buildcraft.core.marker.volume;

import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Lock {
    public LockCause cause;
    public List<LockTarget> targets = new ArrayList<>();

    public Lock() {
    }

    public Lock(LockCause cause, LockTarget... targets) {
        this.cause = cause;
        this.targets.addAll(Arrays.asList(targets));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound causeTag = new NBTTagCompound();
        causeTag.setString("class", cause.getClass().getName());
        causeTag.setTag("data", cause.writeToNBT(new NBTTagCompound()));
        nbt.setTag("cause", causeTag);
        NBTTagList targetsTag = new NBTTagList();
        targets.stream().map(target -> {
            NBTTagCompound targetTag = new NBTTagCompound();
            targetTag.setString("class", target.getClass().getName());
            targetTag.setTag("data", target.writeToNBT(new NBTTagCompound()));
            return targetTag;
        }).forEach(targetsTag::appendTag);
        nbt.setTag("targets", targetsTag);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagCompound causeTag = nbt.getCompoundTag("cause");
        try {
            cause = (LockCause) Class.forName(causeTag.getString("class")).newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        cause.readFromNBT(causeTag.getCompoundTag("data"));
        NBTTagList targetsTag = nbt.getTagList("targets", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, targetsTag.tagCount()).mapToObj(targetsTag::getCompoundTagAt).map(targetTag -> {
            LockTarget target;
            try {
                target = (LockTarget) Class.forName(targetTag.getString("class")).newInstance();
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            target.readFromNBT(targetTag.getCompoundTag("data"));
            return target;
        }).forEach(targets::add);
    }

    public static abstract class LockCause {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void readFromNBT(NBTTagCompound nbt);

        public abstract boolean stillWorks(World world);

        public static class LockCauseBlock extends LockCause {
            public BlockPos pos;
            public Block block;

            public LockCauseBlock() {
            }

            public LockCauseBlock(BlockPos pos, Block block) {
                this.pos = pos;
                this.block = block;
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("pos", NBTUtil.createPosTag(pos));
                nbt.setString("block", Block.REGISTRY.getNameForObject(block).toString());
                return nbt;
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
                pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
                block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
            }

            @Override
            public boolean stillWorks(World world) {
                return world.getBlockState(pos).getBlock() == block;
            }
        }
    }

    public static abstract class LockTarget {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void readFromNBT(NBTTagCompound nbt);

        public static class LockTargetResize extends LockTarget {
            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
            }
        }

        public static class LockTargetAddon extends LockTarget {
            public EnumAddonSlot slot;

            public LockTargetAddon() {
            }

            public LockTargetAddon(EnumAddonSlot slot) {
                this.slot = slot;
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("slot", NBTUtilBC.writeEnum(slot));
                return nbt;
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
                slot = NBTUtilBC.readEnum(nbt.getTag("slot"), EnumAddonSlot.class);
            }
        }
    }
}
