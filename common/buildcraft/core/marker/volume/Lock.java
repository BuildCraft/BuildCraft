/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Yarn 1.20.1 renames:
//   NbtCompound → NbtCompound, NBTUtil → NbtHelper, PacketByteBuf → PacketByteBuf,
//   Identifier → Identifier, Block.REGISTRY → Registries.BLOCK
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.client.render.laser.LaserData_BC8;
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

    public NbtCompound writeToNBT() {
        NbtCompound nbt = new NbtCompound();
        NbtCompound causeTag = new NbtCompound();
        causeTag.put("type", NBTUtilBC.writeEnum(Cause.EnumCause.getForClass(cause.getClass())));
        causeTag.put("data", cause.writeToNBT(new NbtCompound()));
        nbt.put("cause", causeTag);
        nbt.put("targets", NBTUtilBC.writeCompoundList(targets.stream().map(target -> {
            NbtCompound targetTag = new NbtCompound();
            targetTag.put("type", NBTUtilBC.writeEnum(Target.EnumTarget.getForClass(target.getClass())));
            targetTag.put("data", target.writeToNBT(new NbtCompound()));
            return targetTag;
        })));
        return nbt;
    }

    public void readFromNBT(NbtCompound nbt) {
        NbtCompound causeTag = nbt.getCompound("cause");
        cause = NBTUtilBC.readEnum(causeTag.get("type"), Cause.EnumCause.class).supplier.get();
        cause.readFromNBT(causeTag.getCompound("data"));
        NBTUtilBC.readCompoundList(nbt.get("targets")).map(targetTag -> {
            Target target;
            target = NBTUtilBC.readEnum(targetTag.get("type"), Target.EnumTarget.class).supplier.get();
            target.readFromNBT(targetTag.getCompound("data"));
            return target;
        }).forEach(targets::add);
    }

    public void toBytes(PacketByteBuf buf) {
        new PacketBufferBC(buf).writeEnumValue(Cause.EnumCause.getForClass(cause.getClass()));
        cause.toBytes(buf);
        buf.writeInt(targets.size());
        targets.forEach(target -> {
            new PacketBufferBC(buf).writeEnumValue(Target.EnumTarget.getForClass(target.getClass()));
            target.toBytes(buf);
        });
    }

    public void fromBytes(PacketByteBuf buf) {
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
        public abstract NbtCompound writeToNBT(NbtCompound nbt);

        public abstract void readFromNBT(NbtCompound nbt);

        public abstract void toBytes(PacketByteBuf buf);

        public abstract void fromBytes(PacketByteBuf buf);

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
            public NbtCompound writeToNBT(NbtCompound nbt) {
                nbt.put("pos", NbtHelper.fromBlockPos(pos));
                nbt.putString("block", Registries.BLOCK.getId(block).toString());
                return nbt;
            }

            @Override
            public void readFromNBT(NbtCompound nbt) {
                pos = NbtHelper.toBlockPos(nbt.getCompound("pos"));
                block = Registries.BLOCK.get(new Identifier(nbt.getString("block")));
            }

            @Override
            public void toBytes(PacketByteBuf buf) {
                buf.writeBlockPos(pos);
                buf.writeString(Registries.BLOCK.getId(block).toString());
            }

            @Override
            public void fromBytes(PacketByteBuf buf) {
                pos = buf.readBlockPos();
                block = Registries.BLOCK.get(new Identifier(buf.readString(1024)));
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
        public abstract NbtCompound writeToNBT(NbtCompound nbt);

        public abstract void readFromNBT(NbtCompound nbt);

        public abstract void toBytes(PacketByteBuf buf);

        public abstract void fromBytes(PacketByteBuf buf);

        public static class TargetRemove extends Target {
            @Override
            public NbtCompound writeToNBT(NbtCompound nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(NbtCompound nbt) {
            }

            @Override
            public void toBytes(PacketByteBuf buf) {
            }

            @Override
            public void fromBytes(PacketByteBuf buf) {
            }
        }

        public static class TargetResize extends Target {
            @Override
            public NbtCompound writeToNBT(NbtCompound nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(NbtCompound nbt) {
            }

            @Override
            public void toBytes(PacketByteBuf buf) {
            }

            @Override
            public void fromBytes(PacketByteBuf buf) {
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
            public NbtCompound writeToNBT(NbtCompound nbt) {
                nbt.put("slot", NBTUtilBC.writeEnum(slot));
                return nbt;
            }

            @Override
            public void readFromNBT(NbtCompound nbt) {
                slot = NBTUtilBC.readEnum(nbt.get("slot"), EnumAddonSlot.class);
            }

            @Override
            public void toBytes(PacketByteBuf buf) {
                new PacketBufferBC(buf).writeEnumValue(slot);
            }

            @Override
            public void fromBytes(PacketByteBuf buf) {
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
            public NbtCompound writeToNBT(NbtCompound nbt) {
                nbt.put("type", NBTUtilBC.writeEnum(type));
                return nbt;
            }

            @Override
            public void readFromNBT(NbtCompound nbt) {
                type = NBTUtilBC.readEnum(nbt.get("type"), EnumType.class);
            }

            @Override
            public void toBytes(PacketByteBuf buf) {
                new PacketBufferBC(buf).writeEnumValue(type);
            }

            @Override
            public void fromBytes(PacketByteBuf buf) {
                type = new PacketBufferBC(buf).readEnumValue(EnumType.class);
            }

            public enum EnumType {
                STRIPES_WRITE {
                    @Environment(EnvType.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_WRITE;
                    }
                },
                STRIPES_READ {
                    @Environment(EnvType.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_READ;
                    }
                };

                @Environment(EnvType.CLIENT)
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
