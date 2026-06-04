/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): TileFiller logic deferred
package buildcraft.builders.tile;

import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;

public class TileFiller extends TileBC_Neptune
        implements IDebuggable, ITileForTemplateBuilder, IFillerStatementContainer, IControllable {

    public static BlockEntityType<TileFiller> TYPE;

    private Mode controlMode = Mode.ON;

    public TileFiller() {
        super(TYPE, BlockPos.ORIGIN, null);
    }

    public TileFiller(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {}

    @Override
    public World getWorldBC() { return getWorld(); }

    @Override
    public MjBattery getBattery() { return null; }

    @Override
    public BlockPos getBuilderPos() { return getPos(); }

    @Override
    public boolean canExcavate() { return false; }

    @Override
    public SnapshotBuilder<?> getBuilder() { return null; }

    @Override
    public GameProfile getOwner() { return null; }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() { return null; }

    @Override
    public IItemTransactor getInvResources() { return null; }

    @Override
    public boolean hasBox() { return false; }

    @Override
    public IBox getBox() { throw new IllegalStateException("no box"); }

    @Override
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {}

    @Override
    public Mode getControlMode() { return controlMode; }

    @Override
    public void setControlMode(Mode mode) { this.controlMode = mode; }
}
