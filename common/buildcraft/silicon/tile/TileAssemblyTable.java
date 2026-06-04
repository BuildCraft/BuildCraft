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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

/**
 * STUB(R.Chen): AssemblyRecipe/AssemblyRecipeRegistry removed — recipe processing deferred until
 * Fabric RecipeType/RecipeSerializer migration (Phase 4E). inv field is kept for serialization.
 */
public class TileAssemblyTable extends TileLaserTableBase {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("assembly_table");
    public static final int NET_RECIPE_STATE = IDS.allocId("RECIPE_STATE");

    public final ItemHandlerSimple inv = itemManager.addInvHandler(
        "inv",
        3 * 4,
        ItemHandlerManager.EnumAccess.BOTH
        // STUB(R.Chen): EnumPipePart.VALUES pipe access deferred until Transfer-API is wired.
    );

    // STUB(R.Chen): recipesStates map kept for future recipe migration but uses Object as key placeholder.
    public SortedMap<Object, EnumAssemblyRecipeState> recipesStates = new TreeMap<>();

    public TileAssemblyTable(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public long getTarget() {
        // STUB(R.Chen): active recipe lookup deferred until AssemblyRecipe migration.
        return 0L;
    }

    @Override
    public void tick() {
        super.tick();
        // STUB(R.Chen): updateRecipes(), recipe completion, advancement unlocking all deferred.
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // STUB(R.Chen): recipesStates serialisation deferred.
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        // STUB(R.Chen): recipesStates deserialisation deferred.
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): NET_GUI_DATA recipe state payload deferred.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): NET_GUI_DATA and NET_RECIPE_STATE reading deferred.
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipes - " + recipesStates.size());
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }
}
