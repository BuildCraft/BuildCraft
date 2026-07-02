/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.addon;

import java.io.IOException;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.IBox;
import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.containers.IFillerStatementContainer;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.builders.filler.FillerType;
import ct.buildcraft.builders.filler.FillerUtil;
import ct.buildcraft.builders.snapshot.Template;
import ct.buildcraft.core.marker.volume.Addon;
import ct.buildcraft.core.marker.volume.AddonDefaultRenderer;
import ct.buildcraft.core.marker.volume.IFastAddonRenderer;
import ct.buildcraft.core.marker.volume.ISingleAddon;
import ct.buildcraft.lib.statement.FullStatement;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AddonFillerPlanner extends Addon implements ISingleAddon, IFillerStatementContainer {
    public final FullStatement<IFillerPattern> patternStatement = new FullStatement<>(
        FillerType.INSTANCE,
        4,
        null
    );
    public boolean inverted;
    @Nullable
    public Template.BuildingInfo buildingInfo;

    public void updateBuildingInfo() {
        buildingInfo = FillerUtil.createBuildingInfo(
            this,
            patternStatement,
            IntStream.range(0, patternStatement.maxParams)
                .mapToObj(patternStatement::get)
                .toArray(IStatementParameter[]::new),
            inverted
        );
    }

    @Override
    public void onVolumeBoxSizeChange() {
        updateBuildingInfo();
    }

    @Override
    public IFastAddonRenderer<AddonFillerPlanner> getRenderer() {
        return new AddonDefaultRenderer<AddonFillerPlanner>(BCBuildersSprites.FILLER_PLANNER.getSprite())
            .then(new AddonRendererFillerPlanner());
    }

    @Override
    public void onAdded() {
        super.onAdded();
        updateBuildingInfo();
    }

    @Override
    public void postReadFromNbt() {
        super.postReadFromNbt();
        updateBuildingInfo();
    }

    @Override
    public void onPlayerRightClick(Player player) {
        super.onPlayerRightClick(player);
//        BCBuildersGuis.FILLER_PLANNER.openGUI(player);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.put("patternStatement", patternStatement.writeToNbt());
        nbt.putBoolean("inverted", inverted);
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        patternStatement.readFromNbt(nbt.getCompound("patternStatement"));
        inverted = nbt.getBoolean("inverted");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        patternStatement.writeToBuffer(buf);
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) throws IOException {
        patternStatement.readFromBuffer(buf);
        inverted = buf.readBoolean();
        updateBuildingInfo();
    }

    // IFillerStatementContainer

    @Override
    public BlockEntity getNeighbourTile(Direction side) {
        return null;
    }

    @Override
    public BlockEntity getTile() {
        return null;
    }

    @Override
    public Level getFillerWorld() {
        return volumeBox.world;
    }

    @Override
    public boolean hasBox() {
        return true;
    }

    @Override
    public IBox getBox() {
        return volumeBox.box;
    }

    @Override
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
        patternStatement.set(pattern, params);
        updateBuildingInfo();
    }
}
