/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): TileDynamoMJ logic deferred — ITickable + RFT power system migration pending
package buildcraft.energy.tile;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.enums.EnumPowerStage;

import buildcraft.lib.engine.IEngineLikeForLedger;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileDynamoMJ extends TileBC_Neptune implements IEngineLikeForLedger {

    public static BlockEntityType<TileDynamoMJ> TYPE;

    public TileDynamoMJ(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileDynamoMJ(BlockPos pos, BlockState state) {
        this(TYPE, pos, state);
    }

    @Override
    public EnumPowerStage getPowerStage() {
        return EnumPowerStage.BLUE;
    }

    @Override
    public boolean isEngineOn() {
        return false;
    }

    @Override
    public long getCurrentMjOutput() {
        return 0;
    }

    @Override
    public long getMjStored() {
        return 0;
    }

    @Override
    public double getHeat() {
        return 0;
    }
}
