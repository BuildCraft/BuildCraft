/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()         → tick() + static ticker()
//   Tank/FluidSmoother/Fluid/FluidStack → STUB (Transfer-API fluid, Phase 4E)
//   FunctionContext/DefaultContexts/NodeVariable* → STUB (lib.expression not in libLeaf)
//   AverageLong                → STUB (not in libLeaf)
//   IRefineryRecipeManager     → STUB (api.recipes not in libLeaf)
//   BCCoreConfig.networkUpdateRate → STUB inline constant
//   ElementHelpInfo            → STUB (lib.gui.help not fully wired for Tank)
//   CapUtil.CAP_FLUIDS/TilesAPI.CAP_HAS_WORK → STUB (Phase 4F)
//   NbtCompound             → NbtCompound
//   Direction                 → Direction
//   Side                       → NetSide
public class TileDistiller_BC8 extends TileBC_Neptune implements IDebuggable {

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("Distiller");
    public static final int NET_TANK_IN = IDS.allocId("TANK_IN");
    public static final int NET_TANK_GAS_OUT = IDS.allocId("TANK_GAS_OUT");
    public static final int NET_TANK_LIQUID_OUT = IDS.allocId("TANK_LIQUID_OUT");

    // STUB(R.Chen): FunctionContext + NodeVariable* (lib.expression) not in libLeaf.
    // MODEL_FUNC_CTX, MODEL_FACING, MODEL_ACTIVE, MODEL_POWER_AVG, MODEL_POWER_MAX deferred.

    public static final long MAX_MJ_PER_TICK = 6 * MjAPI.MJ;

    // STUB(R.Chen): Tank + FluidSmoother deferred — Transfer-API fluid migration (Phase 4E).
    // public final Tank tankIn, tankGasOut, tankLiquidOut;
    // public final FluidSmoother smoothedTankIn, smoothedTankGasOut, smoothedTankLiquidOut;

    private final MjBattery mjBattery = new MjBattery(1024 * MjAPI.MJ);

    // STUB(R.Chen): AverageLong not in libLeaf — power average tracking deferred.
    // private final AverageLong powerAvg = new AverageLong(100);

    // STUB(R.Chen): BCCoreConfig.networkUpdateRate — inline fallback constant.
    private static final int NETWORK_UPDATE_RATE = 10;

    public final ModelVariableData clientModelData = new ModelVariableData();

    private long distillPower = 0;
    public boolean isActive = false;

    // STUB(R.Chen): SafeTimeTracker needed for network update pacing.
    private final SafeTimeTracker updateTracker = new SafeTimeTracker(NETWORK_UPDATE_RATE, 2);

    public TileDistiller_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(mjBattery)));
        // STUB(R.Chen): tank capability registration deferred to Phase 4E.
        // STUB(R.Chen): TilesAPI.CAP_HAS_WORK deferred to Phase 4F.
    }

    /** BlockEntityTicker wired in BlockDistiller.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileDistiller_BC8> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        if (world.isClient) {
            // STUB(R.Chen): client-side smoothedTank tick + model variable update deferred (Phase 4E).
            return;
        }
        // STUB(R.Chen): entire distillation logic (recipe lookup + MJ drain + fluid transfer)
        //               deferred to Phase 4E (IRefineryRecipeManager + Transfer-API fluids).
        mjBattery.tick(getWorld(), getPos());
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // NBT

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // STUB(R.Chen): tankManager.serializeNBT() deferred — Tank not in libLeaf.
        nbt.put("battery", mjBattery.writeToNbt());
        nbt.putLong("distillPower", distillPower);
        // STUB(R.Chen): powerAvg.writeToNbt deferred.
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        // STUB(R.Chen): tankManager.deserializeNBT deferred.
        mjBattery.readFromNbt(nbt.getCompound("battery"));
        distillPower = nbt.getLong("distillPower");
        // STUB(R.Chen): powerAvg.readFromNbt deferred.
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): smoothedTank payloads deferred — FluidSmoother not in libLeaf.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): smoothedTank payloads deferred — FluidSmoother not in libLeaf.
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + mjBattery.getDebugString());
        left.add("distillPower = " + distillPower);
        left.add("isActive = " + isActive);
        left.add("tanks = STUB(not yet migrated)");
    }
}
