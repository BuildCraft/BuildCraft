/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public abstract class TileMiner extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("miner");
    public static final int NET_LED_STATUS = IDS.allocId("LED_STATUS");
    public static final int NET_WANTED_Y = IDS.allocId("WANTED_Y");

    protected int progress = 0;
    protected BlockPos currentPos = null;

    private int wantedLength = 0;
    private double currentLength = 0;
    private double lastLength = 0;
    private int offset;

    protected boolean isComplete = false;
    protected final MjBattery battery = new MjBattery(getBatteryCapacity());

    public TileMiner(TileEntityType<?> blockEntityType) {
        super(blockEntityType);
        caps.addProvider(new MjCapabilityHelper(createMjReceiver()));
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> !isComplete, EnumPipePart.VALUES);
    }

    protected abstract void mine();

    protected abstract IMjReceiver createMjReceiver();

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public void update() {
        ITickable.super.update();
        if (level.isClientSide) {
            lastLength = currentLength;
            if (Math.abs(wantedLength - currentLength) <= 0.01) {
                currentLength = wantedLength;
            } else {
                currentLength = currentLength + (wantedLength - currentLength) / 7D;
            }
            return;
        }

        battery.tick(getLevel(), getBlockPos());

        if (level.getGameTime() % 10 == offset) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        mine();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        offset = level.random.nextInt(10);
    }

    @Override
    public void onRemove() {
        super.onRemove();
        for (int y = worldPosition.getY() - 1; y > worldPosition.getY() - BCCoreConfig.miningMaxDepth; y--) {
            BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
            if (level.getBlockState(blockPos).getBlock() == BCFactoryBlocks.tube.get()) {
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), BlockConstants.UPDATE_ALL);
            } else {
                break;
            }
        }
    }

    protected void updateLength() {
        int newY = getTargetPos() != null ? getTargetPos().getY() : worldPosition.getY();
        int newLength = worldPosition.getY() - newY;
        if (newLength != wantedLength) {
            for (int y = worldPosition.getY() - 1; y > worldPosition.getY() - BCCoreConfig.miningMaxDepth; y--) {
                BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
                if (level.getBlockState(blockPos).getBlock() == BCFactoryBlocks.tube.get()) {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), BlockConstants.UPDATE_ALL);
                } else {
                    break;
                }
            }
            for (int y = worldPosition.getY() - 1; y > newY; y--) {
                BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
                level.setBlock(blockPos, BCFactoryBlocks.tube.get().defaultBlockState(), BlockConstants.UPDATE_ALL);
            }
            currentLength = wantedLength = newLength;
            sendNetworkUpdate(NET_WANTED_Y);
        }
    }

    protected BlockPos getTargetPos() {
        return currentPos;
    }

    public double getLength(float partialTicks) {
        if (partialTicks <= 0) {
            return lastLength;
        } else if (partialTicks >= 1) {
            return currentLength;
        } else {
            return lastLength * (1 - partialTicks) + currentLength * partialTicks;
        }
    }

    public boolean isComplete() {
        return level.isClientSide ? isComplete : currentPos == null;
    }

    @Override
    protected void migrateOldNBT(int version, CompoundNBT nbt) {
        super.migrateOldNBT(version, nbt);
        if (version == BCVersion.BEFORE_RECORDS.dataVersion || version == BCVersion.v7_2_0_pre_12.dataVersion) {
            CompoundNBT oldBattery = nbt.getCompound("battery");
            int energy = oldBattery.getInt("energy");
            battery.extractPower(0, Integer.MAX_VALUE);
            battery.addPower(energy * 100, false);
        }
    }

    @Override
//    public CompoundNBT writeToNBT(CompoundNBT nbt) {
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        if (currentPos != null) {
            nbt.put("currentPos", NBTUtil.writeBlockPos(currentPos));
        }
        nbt.putInt("wantedLength", wantedLength);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.serializeNBT());
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains("currentPos")) {
            currentPos = NBTUtil.readBlockPos(nbt.getCompound("currentPos"));
        }
        wantedLength = nbt.getInt("wantedLength");
        progress = nbt.getInt("progress");
        battery.deserializeNBT(nbt.getCompound("battery"));
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
                buffer.writeInt(wantedLength);
            } else if (id == NET_LED_STATUS) {
                buffer.writeBoolean(isComplete());
                battery.writeToBuffer(buffer);
            } else if (id == NET_WANTED_Y) {
                buffer.writeInt(wantedLength);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
                currentLength = lastLength = wantedLength = buffer.readInt();
            } else if (id == NET_LED_STATUS) {
                isComplete = buffer.readBoolean();
                battery.readFromBuffer(buffer);
            } else if (id == NET_WANTED_Y) {
                wantedLength = buffer.readInt();
            }
        }
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        left.add("battery = " + battery.getDebugString());
//        left.add("current = " + currentPos);
//        left.add("wantedLength = " + wantedLength);
//        left.add("currentLength = " + currentLength);
//        left.add("lastLength = " + lastLength);
//        left.add("isComplete = " + isComplete());
//        left.add("progress = " + LocaleUtil.localizeMj(progress));
        left.add(new StringTextComponent("battery = " + battery.getDebugString()));
        left.add(new StringTextComponent("current = " + currentPos));
        left.add(new StringTextComponent("wantedLength = " + wantedLength));
        left.add(new StringTextComponent("currentLength = " + currentLength));
        left.add(new StringTextComponent("lastLength = " + lastLength));
        left.add(new StringTextComponent("isComplete = " + isComplete()));
        left.add(new StringTextComponent("progress = ").append(LocaleUtil.localizeMjComponent(progress)));
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
//    public double getMaxRenderDistanceSquared()
    public double getViewDistance() {
//        return Double.MAX_VALUE;
        return 512;
    }
//
//    // Rendering
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean hasFastRenderer() {
//        return true;
//    }

    @OnlyIn(Dist.CLIENT)
    public float getPercentFilledForRender() {
        float val = battery.getStored() / (float) battery.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    protected long getBatteryCapacity() {
        return 500 * MjAPI.MJ;
    }
}
