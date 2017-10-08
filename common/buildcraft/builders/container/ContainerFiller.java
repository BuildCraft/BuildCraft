/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.filler.FillerType;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {
    private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
        FillerType.INSTANCE,
        4,
        (statement, paramIndex) -> onStatementChange()
    );

    public ContainerFiller(EntityPlayer player, TileFiller tile) {
        super(player, tile);

        addFullPlayerInventory(153);

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, sx * 18 + 8, sy * 18 + 40));
            }
        }

        init();
    }

    @Override
    public EntityPlayer getPlayer() {
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
            if (!player.world.isRemote) {
                WorldSavedDataVolumeBoxes.get(getPlayer().world).markDirty();
            }
        }
        if (!player.world.isRemote) {
            tile.onStatementChange();
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side, ctx);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
