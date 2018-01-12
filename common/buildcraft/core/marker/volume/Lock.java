/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.core.client.BuildCraftLaserManager;

public class Lock {
    public Cause cause;
    public List<Target> targets = new ArrayList<>();

    public Lock() {
    }

    public Lock(Cause cause, Target... targets) {
        this.cause = cause;
        this.targets.addAll(Arrays.asList(targets));
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound causeTag = new NBTTagCompound();
        causeTag.setTag("type", NBTUtilBC.writeEnum(Cause.EnumCause.getForClass(cause.getClass())));
        causeTag.setTag("data", cause.writeToNBT(new NBTTagCompound()));
        nbt.setTag("cause", causeTag);
        nbt.setTag("targets", NBTUtilBC.writeCompoundList(targets.stream().map(target -> {
            NBTTagCompound targetTag = new NBTTagCompound();
            targetTag.setTag("type", NBTUtilBC.writeEnum(Target.EnumTarget.getForClass(target.getClass())));
            targetTag.setTag("data", target.writeToNBT(new NBTTagCompound()));
            return targetTag;
        })));
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagCompound causeTag = nbt.getCompoundTag("cause");
        cause = NBTUtilBC.readEnum(causeTag.getTag("type"), Cause.EnumCause.class).supplier.get();
        cause.readFromNBT(causeTag.getCompoundTag("data"));
        NBTUtilBC.readCompoundList(nbt.getTag("targets")).map(targetTag -> {
            Target target;
            target = NBTUtilBC.readEnum(targetTag.getTag("type"), Target.EnumTarget.class).supplier.get();
            target.readFromNBT(targetTag.getCompoundTag("data"));
            return target;
        }).forEach(targets::add);
    }

    public void toBytes(PacketBuffer buf) {
        new PacketBufferBC(buf).writeEnumValue(Cause.EnumCause.getForClass(cause.getClass()));
        cause.toBytes(buf);
        buf.writeInt(targets.size());
        targets.forEach(target -> {
            new PacketBuffer(buf).writeEnumValue(Target.EnumTarget.getForClass(target.getClass()));
            target.toBytes(buf);
        });
    }

    public void fromBytes(PacketBuffer buf) {
        cause = new PacketBufferBC(buf).readEnumValue(Cause.EnumCause.class).supplier.get();
        cause.fromBytes(buf);
        targets.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> {
            Target target;
            target = new PacketBufferBC(buf).readEnumValue(Target.EnumTarget.class).supplier.get();
            target.fromBytes(buf);
            return target;
        }).forEach(targets::add);
    }

    public static abstract class Cause {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void readFromNBT(NBTTagCompound nbt);

        public abstract void toBytes(PacketBuffer buf);

        public abstract void fromBytes(PacketBuffer buf);

        public abstract boolean stillWorks(World world);

        public static class CauseBlock extends Cause {
            public BlockPos pos;
            public Block block;

            public CauseBlock() {
            }

            public CauseBlock(BlockPos pos, Block block) {
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
                MessageUtil.writeBlockPos(buf, pos);
                buf.writeString(Block.REGISTRY.getNameForObject(block).toString());
            }

            @Override
            public void fromBytes(PacketBuffer buf) {
                pos = MessageUtil.readBlockPos(buf);
                block = Block.REGISTRY.getObject(new ResourceLocation(buf.readString(1024)));
            }

            @Override
            public boolean stillWorks(World world) {
                return world.getBlockState(pos).getBlock() == block;
            }
        }

        enum EnumCause {
            BLOCK(CauseBlock::new);

            public final Supplier<? extends Cause> supplier;

            EnumCause(Supplier<? extends Cause> supplier) {
                this.supplier = supplier;
            }

            public static EnumCause getForClass(Class<? extends Cause> clazz) {
                return Arrays.stream(values())
                    .filter(enumCause -> enumCause.supplier.get().getClass() == clazz)
                    .findFirst()
                    .orElse(null);
            }
        }
    }

    public static abstract class Target {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void readFromNBT(NBTTagCompound nbt);

        public abstract void toBytes(PacketBuffer buf);

        public abstract void fromBytes(PacketBuffer buf);

        public static class TargetRemove extends Target {
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

        public static class TargetResize extends Target {
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

        public static class TargetAddon extends Target {
            public EnumAddonSlot slot;

            public TargetAddon() {
            }

            public TargetAddon(EnumAddonSlot slot) {
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

        public static class TargetUsedByMachine extends Target {
            public EnumType type;

            public TargetUsedByMachine() {
            }

            public TargetUsedByMachine(EnumType type) {
                this.type = type;
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("type", NBTUtilBC.writeEnum(type));
                return nbt;
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
                type = NBTUtilBC.readEnum(nbt.getTag("type"), EnumType.class);
            }

            @Override
            public void toBytes(PacketBuffer buf) {
                new PacketBufferBC(buf).writeEnumValue(type);
            }

            @Override
            public void fromBytes(PacketBuffer buf) {
                type = new PacketBufferBC(buf).readEnumValue(EnumType.class);
            }

            public enum EnumType {
                STRIPES_WRITE {
                    @SideOnly(Side.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_WRITE;
                    }
                },
                STRIPES_READ {
                    @SideOnly(Side.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_READ;
                    }
                };

                @SideOnly(Side.CLIENT)
                public abstract LaserData_BC8.LaserType getLaserType();
            }
        }

        enum EnumTarget {
            REMOVE(TargetRemove::new),
            RESIZE(TargetResize::new),
            ADDON(TargetAddon::new),
            USED_BY_MACHINE(TargetUsedByMachine::new);

            public final Supplier<? extends Target> supplier;

            EnumTarget(Supplier<? extends Target> supplier) {
                this.supplier = supplier;
            }

            public static EnumTarget getForClass(Class<? extends Target> clazz) {
                return Arrays.stream(values())
                    .filter(enumTarget -> enumTarget.supplier.get().getClass() == clazz)
                    .findFirst()
                    .orElse(null);
            }
        }
    }
}
