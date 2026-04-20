/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.menu;

import java.io.IOException;

import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.builders.BCBuildersGuis;
import ct.buildcraft.builders.filler.FillerType;
import ct.buildcraft.builders.tile.TileFiller;
import ct.buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.statement.FullStatement;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {
    private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
        FillerType.INSTANCE,
        4,
        (statement, paramIndex) -> onStatementChange()
    );
    
	public ContainerFiller(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(27), CreateClientLevelAccess(buf));
	}

    public ContainerFiller(int containerId, Inventory playerInventory, ItemHandlerSimple invResources, ContainerLevelAccess access) {
        super(BCBuildersGuis.MENU_FILLER.get(), playerInventory, containerId, access);

        addFullPlayerInventory(153);

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
            	addSlot(new SlotBase(invResources, sx + sy * 9, sx * 18 + 8, sy * 18 + 85));
            }
        }

        init();
    }

    @Override
    public Player getPlayer() {
        return playerInventory.player;
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
            if (!playerInventory.player.level.isClientSide) {
                WorldSavedDataVolumeBoxes.get(getPlayer().level).setDirty();
            }
        }
        if (!playerInventory.player.level.isClientSide) {
            tile.onStatementChange();
        }
    }

    @Override
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side, ctx);
    }
}
