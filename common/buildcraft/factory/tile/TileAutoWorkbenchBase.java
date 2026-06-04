/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.io.IOException;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerFiltered;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()       → tick() + static ticker()
//   WorkbenchCrafting        → STUB (buildcraft.lib.tile.craft not yet in libLeaf)
//   IAutoCraft               → STUB (blocked by WorkbenchCrafting)
//   IMjRedstoneReceiver      → MjCapabilityHelper (MjBatteryReceiver) used instead
//   IHasWork                 → STUB (TilesAPI.CAP_HAS_WORK deferred, Phase 4F)
//   NbtCompound           → NbtCompound
//   Side                     → NetSide
//   Identifier         → Identifier
//   crafting/power logic     → STUB (WorkbenchCrafting not in libLeaf)
public abstract class TileAutoWorkbenchBase extends TileBC_Neptune {

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("autoWorkbench");

    private static final long POWER_GEN_PASSIVE = MjAPI.MJ / 5;
    private static final long POWER_REQUIRED = POWER_GEN_PASSIVE * 20 * 10;
    private static final long POWER_LOST = POWER_GEN_PASSIVE * 10;

    @SuppressWarnings("unused")
    private static final Identifier ADVANCEMENT_AUTOCRAFT = new Identifier("buildcraftfactory", "lazy_crafting");

    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterialFilter;
    public final ItemHandlerFiltered invMaterials;
    public final ItemHandlerSimple invResult;

    // STUB(R.Chen): WorkbenchCrafting + IAutoCraft deferred — lib.tile.craft not in libLeaf.
    // private final WorkbenchCrafting crafting;

    private long powerStored;
    @SuppressWarnings("unused")
    private long powerStoredLast;

    public TileAutoWorkbenchBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int width, int height) {
        super(type, pos, state);
        int slots = width * height;
        invBlueprint = itemManager.addInvHandler("blueprint", slots, EnumAccess.PHANTOM);
        invMaterialFilter = itemManager.addInvHandler("material_filter", slots, EnumAccess.PHANTOM);
        invMaterials = new ItemHandlerFiltered(invMaterialFilter, true);
        // STUB(R.Chen): invMaterials.setCallback(itemManager.callback) — callback API removed in migration.
        //               ItemHandlerFiltered checker is set up in its constructor instead.
        itemManager.addInvHandler("materials", invMaterials, EnumAccess.INSERT);
        invResult = itemManager.addInvHandler("result", 1, EnumAccess.EXTRACT);
        // STUB(R.Chen): crafting = new WorkbenchCrafting(width, height, ...) deferred.
        // STUB(R.Chen): TilesAPI.CAP_HAS_WORK / IMjRedstoneReceiver cap deferred to Phase 4F.
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(new MjBattery(POWER_REQUIRED * 2))));
    }

    /** BlockEntityTicker wired in BlockAutoWorkbench*.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileAutoWorkbenchBase> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        if (world.isClient) {
            return;
        }
        // STUB(R.Chen): full crafting tick (WorkbenchCrafting + MJ drain) deferred to Phase 4E.
        powerStored -= Math.min(powerStored, POWER_LOST);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // NBT

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("powerStored", powerStored);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        powerStored = nbt.getLong("powerStored");
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): resultClient / recipe sync deferred — WorkbenchCrafting not in libLeaf.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): resultClient / recipe sync deferred.
    }
}
