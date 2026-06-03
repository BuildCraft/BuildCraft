/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// STUB(R.Chen): TileBuilder logic deferred — snapshot/builder API migration pending
package buildcraft.builders.tile;

import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;

public class TileBuilder extends TileBC_Neptune
        implements IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder {

    public static BlockEntityType<TileBuilder> TYPE;

    public TileBuilder(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        // STUB
    }

    @Override
    public World getWorldBC() {
        return getWorld();
    }

    @Override
    public MjBattery getBattery() {
        return null;
    }

    @Override
    public BlockPos getBuilderPos() {
        return getPos();
    }

    @Override
    public boolean canExcavate() {
        return false;
    }

    @Override
    public SnapshotBuilder<?> getBuilder() {
        return null;
    }

    @Override
    public GameProfile getOwner() {
        return null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return null;
    }

    @Override
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return null;
    }

    @Override
    public IItemTransactor getInvResources() {
        return null;
    }

    @Override
    public TankManager getTankManager() {
        return null;
    }
}
