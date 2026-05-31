/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class TileLaserTableBase extends TileBC_Neptune implements ILaserTarget, IDebuggable {
    private static final long MJ_FLOW_ROUND = MjAPI.MJ / 10;
    private final AverageLong avgPower = new AverageLong(120);
    public long avgPowerClient;
    public long power;

    protected TileLaserTableBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): TilesAPI.CAP_HAS_WORK — Forge capability; deferred to Phase 4F.
        // caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> getTarget() > 0, EnumPipePart.VALUES);
    }

    public abstract long getTarget();

    public static <T extends TileLaserTableBase> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        avgPower.tick();
        if (world.isClient) {
            return;
        }

        if (getTarget() <= 0) {
            power = 0;
            avgPower.clear();
        }
    }

    @Override
    public long getRequiredLaserPower() {
        return getTarget() - power;
    }

    @Override
    public long receiveLaserPower(long microJoules) {
        long received = Math.min(microJoules, getRequiredLaserPower());
        power += received;
        avgPower.push(received);
        return microJoules - received;
    }

    @Override
    public boolean isInvalidTarget() {
        return isRemoved();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("power", power);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        power = nbt.getLong("power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == TileBC_Neptune.NetEnvType.SERVER) {
            if (id == NET_GUI_TICK) {
                buffer.writeLong(power);
                double avg = avgPower.getAverage();
                long pwrAvg = Math.round(avg);
                long div = pwrAvg / MJ_FLOW_ROUND;
                long mod = pwrAvg % MJ_FLOW_ROUND;
                int mj = (int) (div) + ((mod > MJ_FLOW_ROUND / 2) ? 1 : 0);
                buffer.writeInt(mj);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == TileBC_Neptune.NetEnvType.CLIENT) {
            if (id == NET_GUI_TICK) {
                power = buffer.readLong();
                avgPowerClient = buffer.readInt() * MJ_FLOW_ROUND;
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("power - " + LocaleUtil.localizeMj(power));
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }

    protected boolean extract(buildcraft.lib.tile.item.ItemHandlerSimple inv,
                              java.util.Collection<?> items, boolean simulate, boolean precise) {
        // STUB(R.Chen): IngredientStack not in libLeaf — extraction logic deferred.
        // Full implementation pending IngredientStack migration in Phase 4E.
        return false;
    }
}
