/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjBattery;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.core.BCCoreConfig;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.lib.migrate.BCVersion;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public abstract class TileMiner extends TileBC_Neptune implements IDebuggable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("miner");
    public static final int NET_LED_STATUS = IDS.allocId("LED_STATUS");
    public static final int NET_WANTED_Y = IDS.allocId("WANTED_Y");

    protected int progress = 0;
    protected BlockPos currentPos = null;

    protected int wantedLength = 0;
    protected double currentLength = 0;
    protected double lastLength = 0;
    protected int offset;

    protected boolean isComplete = false;
    protected final MjBattery battery = new MjBattery(getBatteryCapacity());
    private final AABB blockAABB = new AABB(0,0,0,1,1,1);


    public TileMiner(BlockEntityType<?> bet, BlockPos pos, BlockState state) {
    	super(bet, pos, state);
        caps.addProvider(new MjCapabilityHelper(createMjReceiver()));
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> !isComplete, EnumPipePart.VALUES);
    }

    protected abstract void mine();

    protected abstract IMjReceiver createMjReceiver();

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void update() {
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
    public void onRemove(boolean dropSelf) {
        super.onRemove(dropSelf);
        for (int y = worldPosition.getY() - 1; y > worldPosition.getY() - BCCoreConfig.miningMaxDepth; y--) {
            BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
            if (level.getBlockState(blockPos).getBlock() == BCFactoryBlocks.TUBE_BLOCK.get()) {
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
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
                if (level.getBlockState(blockPos).getBlock() == BCFactoryBlocks.TUBE_BLOCK.get()) {
                    level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                } else {
                    break;
                }
            }
            for (int y = worldPosition.getY() - 1; y > newY; y--) {
                BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
                level.setBlockAndUpdate(blockPos, BCFactoryBlocks.TUBE_BLOCK.get().defaultBlockState());
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
    protected void migrateOldNBT(int version, CompoundTag nbt) {
        super.migrateOldNBT(version, nbt);
        if (version == BCVersion.BEFORE_RECORDS.dataVersion || version == BCVersion.v7_2_0_pre_12.dataVersion) {
            CompoundTag oldBattery = nbt.getCompound("battery");
            int energy = oldBattery.getInt("energy");
            battery.extractPower(0, Integer.MAX_VALUE);
            battery.addPower(energy * 100, FluidAction.EXECUTE);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (currentPos != null) {
            nbt.putLong("currentPos", currentPos.asLong());
        }
        nbt.putInt("wantedLength", wantedLength);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.serializeNBT());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("currentPos")) {
            currentPos = BlockPos.of(nbt.getLong("currentPos"));
        }
        wantedLength = nbt.getInt("wantedLength");
        progress = nbt.getInt("progress");
        // TODO: remove in next version
        if (nbt.contains("mj_battery")) {
            nbt.put("battery", nbt.get("mj_battery"));
        }
        battery.deserializeNBT(nbt.getCompound("battery"));
    }

    // Networking

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
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
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
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
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("current = " + currentPos);
        left.add("wantedLength = " + wantedLength);
        left.add("currentLength = " + currentLength);
        left.add("lastLength = " + lastLength);
        left.add("isComplete = " + isComplete());
        left.add("progress = " + LocaleUtil.localizeMj(progress));
    }

    // Rendering
    
    @Override
	public AABB getRenderBoundingBox() {
		return blockAABB.move(worldPosition).expandTowards(0, 1-currentLength, 0);
	}


    @OnlyIn(Dist.CLIENT)
    public float getPercentFilledForRender() {
        float val = battery.getStored() / (float) battery.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    protected long getBatteryCapacity() {
        return 500 * MjAPI.MJ;
    }
}
