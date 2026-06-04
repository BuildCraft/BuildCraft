/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()         → tick() + static ticker()
//   ItemEntity                 → ItemEntity
//   LivingEntity           → LivingEntity
//   Direction                 → Direction
//   NbtCompound             → NbtCompound
//   Box              → Box
//   BlockView               → BlockView
//   world.getTileEntity        → world.getBlockEntity
//   EntitySelectors.IS_ALIVE   → EntityPredicates.VALID_ENTITY
//   Identifier           → Identifier
//   ICapabilityProvider        → Object
//   pickupItems/putInNearInventories: TransactorEntityItem + BoundingBoxUtil not in libLeaf — STUBbed.
//   TilesAPI.CAP_HAS_WORK      → deferred (Phase 4F)
public class TileChute extends TileBC_Neptune implements IDebuggable {
    private static final Identifier ADVANCEMENT_DID_INSERT = new Identifier("buildcraftfactory", "retired_hopper");

    @SuppressWarnings("unused")
    private static final int PICKUP_MAX = 3;

    // STUB(R.Chen): per-face pipe-part capability registration deferred — use 3-arg form.
    public final ItemHandlerSimple inv = itemManager.addInvHandler("inv", 4, EnumAccess.INSERT);

    private final MjBattery battery = new MjBattery(1 * MjAPI.MJ);
    private int progress = 0;

    public TileChute(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    public static boolean hasInventoryAtPosition(BlockView world, BlockPos pos, Direction side) {
        return ItemTransactorHelper.getTransactor(world.getBlockEntity(pos), side.getOpposite())
            != NoSpaceTransactor.INSTANCE;
    }

    /** STUB(R.Chen): BoundingBoxUtil.extrudeFace + TransactorEntityItem not yet in libLeaf.
     *               Item entity pickup deferred to Phase 4E. */
    @SuppressWarnings("unused")
    private void pickupItems(Direction currentSide) {
        // STUB(R.Chen): deferred — BoundingBoxUtil / TransactorEntityItem not in libLeaf.
    }

    /** STUB(R.Chen): ItemTransactorHelper.move() not in migrated libLeaf. Deferred to Phase 4E. */
    @SuppressWarnings("unused")
    private void putInNearInventories(Direction currentSide) {
        // STUB(R.Chen): item insertion into adjacent inventories deferred to Phase 4E.
        // Full algorithm:
        //   for each Direction != currentSide: getTransactor(world.getBlockEntity(pos.offset(side)))
        //   then ItemTransactorHelper.move(inv, transactor, 1) — move method not yet in libLeaf.
        //   Entity targets (TransactorEntityItem) also deferred.
    }

    /** BlockEntityTicker wired in BlockChute.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileChute> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        if (world.isClient) {
            return;
        }

        // STUB(R.Chen): instanceof BlockChute check removed — BlockChute not in libLeaf.
        battery.tick(getWorld(), getPos());

        Direction currentSide = world.getBlockState(pos).get(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if (currentSide == Direction.UP) {
            progress += 1000;
        }
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            pickupItems(currentSide);
        }

        putInNearInventories(currentSide);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        progress = nbt.getInt("progress");
        battery.readFromNbt(nbt.getCompound("battery"));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.writeToNbt());
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("progress = " + progress);
    }
}
