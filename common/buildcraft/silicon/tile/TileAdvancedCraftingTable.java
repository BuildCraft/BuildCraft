/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.tile;

import java.io.IOException;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

/**
 * STUB(R.Chen): IAutoCraft removed; WorkbenchCrafting not in libLeaf — crafting logic deferred
 * until WorkbenchCrafting is migrated. inv fields kept for UI/serialisation compatibility.
 */
public class TileAdvancedCraftingTable extends TileLaserTableBase {
    private static final long POWER_REQ = 500 * MjAPI.MJ;

    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResults;

    // STUB(R.Chen): resultClient kept for future GUI migration.
    public ItemStack resultClient = ItemStack.EMPTY;

    public TileAdvancedCraftingTable(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        invBlueprint = itemManager.addInvHandler("blueprint", 3 * 3, EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", 5 * 3, EnumAccess.INSERT);
        invResults = itemManager.addInvHandler("result", 3 * 3, EnumAccess.EXTRACT);
        // STUB(R.Chen): WorkbenchCrafting not in libLeaf — crafting not wired.
    }

    @Override
    public long getTarget() {
        // STUB(R.Chen): crafting.canCraft() check deferred.
        return 0L;
    }

    @Override
    public void tick() {
        super.tick();
        // STUB(R.Chen): WorkbenchCrafting.tick() and craft() deferred.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): NET_GUI_DATA resultClient reading deferred.
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, TileBC_Neptune.NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): NET_GUI_DATA crafting result payload deferred.
    }
}
