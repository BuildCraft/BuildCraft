/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {
    private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
            FillerType.INSTANCE,
            4,
            (statement, paramIndex) -> onStatementChange()
    );

    public ContainerFiller(ContainerType menuType, int id, PlayerEntity player, TileFiller tile) {
        super(menuType, id, player, tile);

        addFullPlayerInventory(153);

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
//                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, sx * 18 + 8, sy * 18 + 40));
                addSlot(new SlotBase(tile.invResources, sx + sy * 9, sx * 18 + 8, sy * 18 + 40));
            }
        }

        init();
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatementClient() {
        return patternStatementClient;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatement() {
        return tile.addon != null ? tile.addon.patternStatement : tile.patternStatement;
    }

    @Override
    public boolean isInverted() {
        return tile.addon != null ? tile.addon.inverted : tile.inverted;
    }

    @Override
    public void setInverted(boolean value) {
        if (tile.addon != null) {
            tile.addon.inverted = value;
        } else {
            tile.inverted = value;
        }
    }

    @Override
    public void valuesChanged() {
        if (tile.addon != null) {
            tile.addon.updateBuildingInfo();
            if (!player.level.isClientSide) {
                WorldSavedDataVolumeBoxes.get(getPlayer().level).setDirty();
            }
        }
        if (!player.level.isClientSide) {
            tile.onStatementChange();
        }
    }

    @Override
//    public void readMessage(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side, ctx);
    }
}
