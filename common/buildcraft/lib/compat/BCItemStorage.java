/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Fabric Transfer API bridge replacing Forge's {@code IItemHandler} capability.
 *
 * Wraps a vanilla {@link Inventory} (which all BuildCraft tiles will implement
 * after the tile-migration pass) and exposes it as a {@link Storage}{@code <ItemVariant>}
 * via Fabric's built-in {@link InventoryStorage} adapter.
 *
 * Why a wrapper class at all (rather than calling InventoryStorage.of directly):
 *   1. The Forge codebase has dozens of {@code IItemHandler} call sites that
 *      will be ported incrementally — this class gives them a stable target.
 *   2. {@link #fromBlockEntity(World, BlockEntity, Direction)} centralises the
 *      side-aware lookup so blocks can later override per-direction inventories
 *      (e.g. quarry's input vs. fuel slots) in one place.
 *
 * TODO(R.Chen): when {@code TileBC_Neptune}'s {@code ItemHandlerManager} is
 *               ported, replace the {@link InventoryStorage} delegate here with
 *               a slot-group-aware adapter so direction filtering survives.
 */
public final class BCItemStorage {

    private BCItemStorage() {}

    /**
     * Side-aware item storage lookup. Mirrors the legacy
     * {@code te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)}
     * call shape.
     *
     * @param world  the world the block entity lives in (must be non-null at call time)
     * @param be     the block entity (may be null — returns {@code null})
     * @param side   the face being queried (use {@code null} for unsided access)
     * @return a {@link Storage} view, or {@code null} if no inventory is exposed on that side
     */
    @Nullable
    public static Storage<ItemVariant> fromBlockEntity(World world, @Nullable BlockEntity be, @Nullable Direction side) {
        if (be == null) return null;
        // Prefer the registered ItemStorage.SIDED lookup so other mods'
        // registrations (e.g. chests with renamed sides) are honoured.
        Storage<ItemVariant> registered = ItemStorage.SIDED.find(world, be.getPos(), be.getCachedState(), be, side);
        if (registered != null) return registered;
        if (be instanceof Inventory inv) {
            return InventoryStorage.of(inv, side);
        }
        return null;
    }

    /** Direct adapter from a vanilla {@link Inventory}. Use from BlockEntity init code. */
    public static Storage<ItemVariant> ofInventory(Inventory inv, @Nullable Direction side) {
        return InventoryStorage.of(inv, side);
    }
}
