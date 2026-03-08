/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.filler.FillerUtil;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.ISingleAddon;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.stream.IntStream;

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
        MessageUtil.serverOpenItemGui(player, BCBuildersItems.addonFillerPlanner.get());
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
    public void toBytes(PacketBufferBC buf) {
        patternStatement.writeToBuffer(buf);
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(PacketBufferBC buf) throws IOException {
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
