package buildcraft.core.marker.volume;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
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

    public void toBytes(PacketBuffer buf) {
        new PacketBufferBC(buf).writeEnumValue(LockCause.EnumLockCause.getForClass(cause.getClass()));
        cause.toBytes(buf);
        buf.writeInt(targets.size());
        targets.forEach(target -> {
            new PacketBuffer(buf).writeEnumValue(LockTarget.EnumLockTarget.getForClass(target.getClass()));
            target.toBytes(buf);
        });
    }

    public void fromBytes(PacketBuffer buf) {
        try {
            cause = new PacketBufferBC(buf).readEnumValue(LockCause.EnumLockCause.class).clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        cause.fromBytes(buf);
        targets.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> {
            LockTarget target;
            try {
                target = new PacketBufferBC(buf).readEnumValue(LockTarget.EnumLockTarget.class).clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            target.fromBytes(buf);
            return target;
        }).forEach(targets::add);
    }

    public static abstract class LockCause {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void readFromNBT(NBTTagCompound nbt);

        public abstract void toBytes(PacketBuffer buf);

        public abstract void fromBytes(PacketBuffer buf);

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
            public void toBytes(PacketBuffer buf) {
                buf.writeBlockPos(pos);
                buf.writeString(Block.REGISTRY.getNameForObject(block).toString());
            }

            @Override
            public void fromBytes(PacketBuffer buf) {
                pos = buf.readBlockPos();
                block = Block.REGISTRY.getObject(new ResourceLocation(buf.readString(1024)));
            }

            @Override
            public boolean stillWorks(World world) {
                return world.getBlockState(pos).getBlock() == block;
            }
        }

        enum EnumLockCause {
            BLOCK(LockCauseBlock.class);

            public final Class<? extends LockCause> clazz;

            EnumLockCause(Class<? extends LockCause> clazz) {
                this.clazz = clazz;
            }

            public static Enum<EnumLockCause> getForClass(Class<? extends LockCause> clazz) {
                return Arrays.stream(values()).filter(enumCause -> enumCause.clazz == clazz).findFirst().orElse(null);
            }
        }
    }

    public static abstract class LockTarget {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void readFromNBT(NBTTagCompound nbt);

        public abstract void toBytes(PacketBuffer buf);

        public abstract void fromBytes(PacketBuffer buf);

        public static class LockTargetResize extends LockTarget {
            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
            }

            @Override
            public void toBytes(PacketBuffer buf) {
            }

            @Override
            public void fromBytes(PacketBuffer buf) {
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

            @Override
            public void toBytes(PacketBuffer buf) {
                new PacketBufferBC(buf).writeEnumValue(slot);
            }

            @Override
            public void fromBytes(PacketBuffer buf) {
                slot = new PacketBufferBC(buf).readEnumValue(EnumAddonSlot.class);
            }
        }

        public static class LockTargetUsedByMachine extends LockTarget {
            public EnumLockTargetUsedByMachineType type;

            public LockTargetUsedByMachine() {
            }

            public LockTargetUsedByMachine(EnumLockTargetUsedByMachineType type) {
                this.type = type;
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("type", NBTUtilBC.writeEnum(type));
                return nbt;
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
                type = NBTUtilBC.readEnum(nbt.getTag("type"), EnumLockTargetUsedByMachineType.class);
            }

            @Override
            public void toBytes(PacketBuffer buf) {
                new PacketBufferBC(buf).writeEnumValue(type);
            }

            @Override
            public void fromBytes(PacketBuffer buf) {
                type = new PacketBufferBC(buf).readEnumValue(EnumLockTargetUsedByMachineType.class);
            }

            public enum EnumLockTargetUsedByMachineType {
                STRIPES_WRITE(BuildCraftLaserManager.STRIPES_WRITE),
                STRIPES_READ(BuildCraftLaserManager.STRIPES_READ);

                public final LaserData_BC8.LaserType laserType;

                EnumLockTargetUsedByMachineType(LaserData_BC8.LaserType laserType) {
                    this.laserType = laserType;
                }
            }
        }

        enum EnumLockTarget {
            RESIZE(LockTargetResize.class),
            ADDON(LockTargetAddon.class),
            USED_BY_MACHINE(LockTargetUsedByMachine.class);

            public final Class<? extends LockTarget> clazz;

            EnumLockTarget(Class<? extends LockTarget> clazz) {
                this.clazz = clazz;
            }

            public static Enum<EnumLockTarget> getForClass(Class<? extends LockTarget> clazz) {
                return Arrays.stream(values()).filter(enumTarget -> enumTarget.clazz == clazz).findFirst().orElse(null);
            }
        }
    }
}
