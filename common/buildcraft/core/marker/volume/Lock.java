/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;


import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Lock {
    public Cause cause;
    public List<Target> targets = new ArrayList<>();

    public Lock() {
    }

    public Lock(Cause cause, Target... targets) {
        this.cause = cause;
        this.targets.addAll(Arrays.asList(targets));
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag causeTag = new CompoundTag();
        causeTag.put("type", NBTUtilBC.writeEnum(Cause.EnumCause.getForClass(cause.getClass())));
        causeTag.put("data", cause.writeToNBT(new CompoundTag()));
        nbt.put("cause", causeTag);
        nbt.put("targets", NBTUtilBC.writeCompoundList(targets.stream().map(target ->
        {
            CompoundTag targetTag = new CompoundTag();
            targetTag.put("type", NBTUtilBC.writeEnum(Target.EnumTarget.getForClass(target.getClass())));
            targetTag.put("data", target.writeToNBT(new CompoundTag()));
            return targetTag;
        })));
        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        CompoundTag causeTag = nbt.getCompound("cause");
        cause = NBTUtilBC.readEnum(causeTag.get("type"), Cause.EnumCause.class).supplier.get();
        cause.readFromNBT(causeTag.getCompound("data"));
        NBTUtilBC.readCompoundList(nbt.get("targets")).map(targetTag ->
        {
            Target target;
            target = NBTUtilBC.readEnum(targetTag.get("type"), Target.EnumTarget.class).supplier.get();
            target.readFromNBT(targetTag.getCompound("data"));
            return target;
        }).forEach(targets::add);
    }

    public void toBytes(FriendlyByteBuf buf) {
        new PacketBufferBC(buf).writeEnum(Cause.EnumCause.getForClass(cause.getClass()));
        cause.toBytes(buf);
        buf.writeInt(targets.size());
        targets.forEach(target ->
        {
            new FriendlyByteBuf(buf).writeEnum(Target.EnumTarget.getForClass(target.getClass()));
            target.toBytes(buf);
        });
    }

    public void fromBytes(FriendlyByteBuf buf) {
        cause = new PacketBufferBC(buf).readEnum(Cause.EnumCause.class).supplier.get();
        cause.fromBytes(buf);
        targets.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i ->
        {
            Target target;
            target = new PacketBufferBC(buf).readEnum(Target.EnumTarget.class).supplier.get();
            target.fromBytes(buf);
            return target;
        }).forEach(targets::add);
    }

    public static abstract class Cause {
        public abstract CompoundTag writeToNBT(CompoundTag nbt);

        public abstract void readFromNBT(CompoundTag nbt);

        public abstract void toBytes(FriendlyByteBuf buf);

        public abstract void fromBytes(FriendlyByteBuf buf);

        public abstract boolean stillWorks(Level world);

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
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.put("pos", NbtUtils.writeBlockPos(pos));
//                nbt.setString("block", Block.REGISTRY.getNameForObject(block).toString());
                nbt.putString("block", ForgeRegistries.BLOCKS.getKey(block).toString());
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                pos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
//                block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
                MessageUtil.writeBlockPos(buf, pos);
//                buf.writeUtf(Block.REGISTRY.getNameForObject(block).toString());
                buf.writeUtf(ForgeRegistries.BLOCKS.getKey(block).toString());
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
                pos = MessageUtil.readBlockPos(buf);
//                block = Block.REGISTRY.getObject(new ResourceLocation(buf.readString(1024)));
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(buf.readUtf(1024)));
            }

            @Override
            public boolean stillWorks(Level world) {
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
        public abstract CompoundTag writeToNBT(CompoundTag nbt);

        public abstract void readFromNBT(CompoundTag nbt);

        public abstract void toBytes(FriendlyByteBuf buf);

        public abstract void fromBytes(FriendlyByteBuf buf);

        public static class TargetRemove extends Target {
            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
            }
        }

        public static class TargetResize extends Target {
            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
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
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.put("slot", NBTUtilBC.writeEnum(slot));
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                slot = NBTUtilBC.readEnum(nbt.get("slot"), EnumAddonSlot.class);
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
                new PacketBufferBC(buf).writeEnum(slot);
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
                slot = new PacketBufferBC(buf).readEnum(EnumAddonSlot.class);
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
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.put("type", NBTUtilBC.writeEnum(type));
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                type = NBTUtilBC.readEnum(nbt.get("type"), EnumType.class);
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
                new PacketBufferBC(buf).writeEnum(type);
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
                type = new PacketBufferBC(buf).readEnum(EnumType.class);
            }

            public enum EnumType {
                STRIPES_WRITE {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_WRITE;
                    }
                },
                STRIPES_READ {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_READ;
                    }
                };

                @OnlyIn(Dist.CLIENT)
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
