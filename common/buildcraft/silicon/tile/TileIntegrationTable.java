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
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

/**
 * STUB(R.Chen): IntegrationRecipe/IntegrationRecipeRegistry not in libLeaf — recipe logic deferred.
 * inv fields kept for UI/serialisation compatibility.
 */
public class TileIntegrationTable extends TileLaserTableBase {
    public final ItemHandlerSimple invTarget = itemManager.addInvHandler(
        "target",
        1,
        ItemHandlerManager.EnumAccess.BOTH
    );
    public final ItemHandlerSimple invToIntegrate = itemManager.addInvHandler(
        "toIntegrate",
        3 * 3 - 1,
        ItemHandlerManager.EnumAccess.BOTH
    );
    public final ItemHandlerSimple invResult = itemManager.addInvHandler(
        "result",
        1,
        ItemHandlerManager.EnumAccess.INSERT
    );

    // STUB(R.Chen): IntegrationRecipe recipe field deferred.
    // public IntegrationRecipe recipe;

    public TileIntegrationTable(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public long getTarget() {
        // STUB(R.Chen): recipe.getRequiredMicroJoules() deferred.
        return 0L;
    }

    @Override
    public void tick() {
        super.tick();
        // STUB(R.Chen): updateRecipe() and recipe completion deferred.
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // STUB(R.Chen): recipe serialisation deferred.
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        // STUB(R.Chen): recipe deserialisation deferred.
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): NET_GUI_DATA recipe payload deferred.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): NET_GUI_DATA recipe reading deferred.
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipe - (stubbed)");
        left.add("target - " + getTarget());
    }
}
