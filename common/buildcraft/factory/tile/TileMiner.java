/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()        → tick() + static ticker() wired in the owning Block.getTicker()
//   Side / @SideOnly          → NetSide / @Environment(EnvType.*)
//   NbtCompound            → NbtCompound (Yarn rename)
//   NBTUtil.createPosTag      → NbtHelper.fromBlockPos
//   NBTUtil.getPosFromTag     → NbtHelper.toBlockPos
//   world.getTotalWorldTime() → world.getTime()
//   world.isClient            → world.isClient
//   world.rand                → world.random
//   world.setBlockToAir       → world.removeBlock(pos, false)
//   Box / INFINITE  → Fabric render bounds are handled via BuiltinModelItemRenderer / BER
//   BCCoreConfig.miningMaxDepth→ MINING_MAX_DEPTH stub constant
//   BCFactoryBlocks.tube      → isTubeBlock() stub (returns false until BCFactoryBlocks is in libLeaf)
//   TilesAPI.CAP_HAS_WORK     → Forge capability — commented out (deferred to Phase 4F)
public abstract class TileMiner extends TileBC_Neptune implements IDebuggable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("miner");
    public static final int NET_LED_STATUS = IDS.allocId("LED_STATUS");
    public static final int NET_WANTED_Y = IDS.allocId("WANTED_Y");

    // STUB(R.Chen): BCCoreConfig.miningMaxDepth — inline constant until BCCoreConfig lands in libLeaf.
    protected static final int MINING_MAX_DEPTH = 256;

    protected int progress = 0;
    protected BlockPos currentPos = null;

    private int wantedLength = 0;
    private double currentLength = 0;
    private double lastLength = 0;
    private int offset;

    protected boolean isComplete = false;
    protected final MjBattery battery = new MjBattery(getBatteryCapacity());

    public TileMiner(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addProvider(new MjCapabilityHelper(createMjReceiver()));
        // STUB(R.Chen): TilesAPI.CAP_HAS_WORK — Forge capability; deferred to Phase 4F.
        //   caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> !isComplete, EnumPipePart.VALUES);
    }

    protected abstract void mine();

    protected abstract IMjReceiver createMjReceiver();

    /** BlockEntityTicker wired by the owning Block.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileMiner> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    /** Ticking logic. Replaces {@code ITickable.update()}. */
    public void tick() {
        if (world.isClient) {
            lastLength = currentLength;
            if (Math.abs(wantedLength - currentLength) <= 0.01) {
                currentLength = wantedLength;
            } else {
                currentLength = currentLength + (wantedLength - currentLength) / 7D;
            }
            return;
        }

        battery.tick(getWorld(), getPos());

        if (world.getTime() % 10 == offset) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        mine();
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        offset = world.random.nextInt(10);
    }

    @Override
    public void onRemove() {
        super.onRemove();
        for (int y = pos.getY() - 1; y > pos.getY() - MINING_MAX_DEPTH; y--) {
            BlockPos blockPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isTubeBlock(world, blockPos)) {
                world.removeBlock(blockPos, false);
            } else {
                break;
            }
        }
    }

    protected void updateLength() {
        int newY = getTargetPos() != null ? getTargetPos().getY() : pos.getY();
        int newLength = pos.getY() - newY;
        if (newLength != wantedLength) {
            for (int y = pos.getY() - 1; y > pos.getY() - MINING_MAX_DEPTH; y--) {
                BlockPos blockPos = new BlockPos(pos.getX(), y, pos.getZ());
                if (isTubeBlock(world, blockPos)) {
                    world.removeBlock(blockPos, false);
                } else {
                    break;
                }
            }
            for (int y = pos.getY() - 1; y > newY; y--) {
                BlockPos blockPos = new BlockPos(pos.getX(), y, pos.getZ());
                // STUB(R.Chen): BCFactoryBlocks.tube.getDefaultState() — tube block not yet in libLeaf.
                //               Tube placement deferred until BCFactoryBlocks is migrated.
            }
            currentLength = wantedLength = newLength;
            sendNetworkUpdate(NET_WANTED_Y);
        }
    }

    /** STUB(R.Chen): BCFactoryBlocks.tube — returns false until BCFactoryBlocks is in libLeaf. */
    @SuppressWarnings("unused")
    protected boolean isTubeBlock(net.minecraft.world.World w, BlockPos blockPos) {
        return false;
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
        return world.isClient ? isComplete : currentPos == null;
    }

    @Override
    protected void migrateOldNBT(int version, NbtCompound nbt) {
        super.migrateOldNBT(version, nbt);
        if (version == BCVersion.BEFORE_RECORDS.dataVersion || version == BCVersion.v7_2_0_pre_12.dataVersion) {
            NbtCompound oldBattery = nbt.getCompound("battery");
            int energy = oldBattery.getInt("energy");
            battery.extractPower(0, Integer.MAX_VALUE);
            battery.addPower(energy * 100, false);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (currentPos != null) {
            nbt.put("currentPos", NbtHelper.fromBlockPos(currentPos));
        }
        nbt.putInt("wantedLength", wantedLength);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.writeToNbt());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("currentPos")) {
            currentPos = NbtHelper.toBlockPos(nbt.getCompound("currentPos"));
        }
        wantedLength = nbt.getInt("wantedLength");
        progress = nbt.getInt("progress");
        // TODO(R.Chen): remove legacy mj_battery migration once all worlds are updated
        if (nbt.contains("mj_battery")) {
            nbt.put("battery", nbt.get("mj_battery"));
        }
        battery.readFromNbt(nbt.getCompound("battery"));
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER) {
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
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, /* STUB(R.Chen): MessageContext */ Object ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT) {
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

    // Rendering — FRAPI/BER-based; the Forge TESR helpers (getRenderBoundingBox,
    // getMaxRenderDistanceSquared, hasFastRenderer) have no direct Fabric equivalent.

    @Environment(EnvType.CLIENT)
    public float getPercentFilledForRender() {
        float val = battery.getStored() / (float) battery.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    protected long getBatteryCapacity() {
        return 500 * MjAPI.MJ;
    }
}
