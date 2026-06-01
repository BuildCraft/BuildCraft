/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.engine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.mj.MjToRfAutoConvertor;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.mj.MJEnergyStorage;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Step 3 migration scope (R.Chen): only the Forge IEnergyStorage capability, the ITickable tick, and the
// energy-field NBT have been ported. All engine-specific game logic (heat, piston, burn, MJ chaining) is
// preserved verbatim and marked with TODO(R.Chen) where it still depends on un-migrated APIs
// (buildcraft.api.mj, IDebuggable, the Forge MjToRfAutoConvertor RF bridge). Do NOT treat the game logic as
// verified for Fabric.
public abstract class TileEngineBase_BC8 extends TileBC_Neptune implements IDebuggable, IEngineLikeForLedger {

    /** Heat per {@link MjAPI#MJ}. */
    public static final double HEAT_PER_MJ = 0.0023;

    public static final double MIN_HEAT = 20;
    public static final double IDEAL_HEAT = 100;
    public static final double MAX_HEAT = 250;

    @Nonnull
    public final IMjConnector mjConnector = createConnector();
    // TODO(R.Chen): MjCapabilityHelper is part of the un-migrated buildcraft.api.mj package (Forge
    // Capability model). It must be replaced by a Transfer API EnergyStorage.SIDED lookup registration.
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(mjConnector);

    protected double heat = MIN_HEAT;// TODO: sync gui data
    protected long power = 0;// TODO: sync gui data
    private long lastPower = 0;
    /** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
    private float progress, lastProgress;
    private int progressPart = 0;

    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    protected Direction currentDirection = Direction.UP;

    public long currentOutput;// TODO: sync gui data
    public boolean isRedstonePowered = false;
    protected boolean isPumping = false;

    /** The model variables, used to keep track of the various state-based variables. */
    public final ModelVariableData clientModelData = new ModelVariableData();

    // Needed: Power stored

    // Step 3: Forge IEnergyStorage capability → Team Reborn EnergyStorage (MJ, µJ-based). Created lazily
    // because the capacity / transfer limits come from subclass-defined abstract methods that cannot be
    // called from the constructor.
    // TODO(R.Chen): the manual `long power` accumulator below should be folded onto this storage, and the
    // storage exposed via EnergyStorage.SIDED in the owning ModInitializer (replacing getCapability).
    private MJEnergyStorage mjEnergyStorage;

    // TODO(R.Chen): Yarn BlockEntity requires (BlockEntityType<?>, BlockPos, BlockState). Subclasses must
    // pass their registered BlockEntityType. This replaces the Forge no-arg constructor.
    public TileEngineBase_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected MJEnergyStorage getMjEnergyStorage() {
        if (mjEnergyStorage == null) {
            mjEnergyStorage = new MJEnergyStorage(getMaxPower(), maxPowerReceived(), maxPowerExtracted());
        }
        return mjEnergyStorage;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        currentDirection = NBTUtilBC.readEnum(nbt.get("currentDirection"), Direction.class);
        if (currentDirection == null) {
            currentDirection = Direction.UP;
        }
        isRedstonePowered = nbt.getBoolean("isRedstonePowered");
        heat = nbt.getDouble("heat");
        power = nbt.getLong("power");
        progress = nbt.getFloat("progress");
        progressPart = nbt.getInt("progressPart");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("currentDirection", NBTUtilBC.writeEnum(currentDirection));
        nbt.putBoolean("isRedstonePowered", isRedstonePowered);
        nbt.putDouble("heat", heat);
        nbt.putLong("power", power);
        nbt.putFloat("progress", progress);
        nbt.putInt("progressPart", progressPart);
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                isPumping = buffer.readBoolean();
                currentDirection = buffer.readEnumValue(Direction.class);
                powerStage = buffer.readEnumValue(EnumPowerStage.class);
                progress = buffer.readFloat();
            } else if (id == NET_GUI_DATA) {
                heat = buffer.readFloat();
                currentOutput = buffer.readLong();
                power = buffer.readLong();
            } else if (id == NET_GUI_TICK) {
                heat = buffer.readFloat();
                currentOutput = buffer.readLong();
                power = buffer.readLong();

            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(isPumping);
                buffer.writeEnumValue(currentDirection);
                buffer.writeEnumValue(powerStage);
                buffer.writeFloat(progress);
            } else if (id == NET_GUI_DATA) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeLong(power);
            } else if (id == NET_GUI_TICK) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeLong(power);

            }
        }
    }

    // TODO(R.Chen): GAME LOGIC — not verified for Fabric. world.updateNeighborsAlways replaces the Forge
    // notifyNeighborsRespectDebug call; behaviour parity unconfirmed.
    public ActionResult attemptRotation() {
        OrderedEnumMap<Direction> possible = VanillaRotationHandlers.ROTATE_FACING;
        Direction current = currentDirection;
        for (int i = 0; i < 6; i++) {
            current = possible.next(current);
            if (isFacingReceiver(current)) {
                if (currentDirection != current) {
                    currentDirection = current;
                    // makeTileCache();
                    sendNetworkUpdate(NET_RENDER_DATA);
                    redrawBlock();
                    world.updateNeighborsAlways(getPos(), getCachedState().getBlock());
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
        }
        return ActionResult.FAIL;
    }

    private boolean isFacingReceiver(Direction dir) {
        return getReceiverToPower(dir) != null;
    }

    protected final boolean canChain() {
        return getMaxChainLength() > 0;
    }

    /** @return The number of additional engines that this engine can send power through. */
    protected int getMaxChainLength() {
        return 2;
    }

    public void rotateIfInvalid() {
        if (currentDirection != null && isFacingReceiver(currentDirection)) {
            return;
        }
        attemptRotation();
        if (currentDirection == null) {
            currentDirection = Direction.UP;
        }
    }

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        currentDirection = null;// Force rotateIfInvalid to always attempt to rotate
        rotateIfInvalid();
    }

    // TODO(R.Chen): GAME LOGIC — World.getBiome returns a RegistryEntry<Biome> in 1.20.1; unwrapped via
    // .value(). Biome.getTemperature(BlockPos) signature parity unconfirmed.
    protected Biome getBiome() {
        // TODO: Cache this!
        return world.getBiome(getPos()).value();
    }

    /** @return The heat of the current biome, in celsius. */
    protected float getBiomeHeat() {
        Biome biome = getBiome();
        // TODO(R.Chen): getTemperature(BlockPos) is private in 1.20.1; using no-arg overload — positional biome
        // temperature (elevation scaling) is lost. Restore once Biome internals are accessible or a mixin is added.
        float temp = biome.getTemperature();
        return Math.max(0, Math.min(30, temp * 15f));
    }

    public double getPowerLevel() {
        return power / (double) getMaxPower();
    }

    protected EnumPowerStage computePowerStage() {
        double heatLevel = getHeatLevel();
        if (heatLevel < 0.25f) return EnumPowerStage.BLUE;
        else if (heatLevel < 0.5f) return EnumPowerStage.GREEN;
        else if (heatLevel < 0.75f) return EnumPowerStage.YELLOW;
        else if (heatLevel < 0.85f) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }

    @Override
    public final EnumPowerStage getPowerStage() {
        if (!world.isClient) {
            EnumPowerStage newStage = computePowerStage();

            if (powerStage != newStage) {
                powerStage = newStage;
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }

        return powerStage;
    }

    public void updateHeatLevel() {
        heat = ((MAX_HEAT - MIN_HEAT) * getPowerLevel()) + MIN_HEAT;
    }

    public double getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public double getIdealHeatLevel() {
        return heat / IDEAL_HEAT;
    }

    @Override
    public double getHeat() {
        return heat;
    }

    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.02;
            case GREEN:
                return 0.04;
            case YELLOW:
                return 0.08;
            case RED:
                return 0.12;
            default:
                return 0;
        }
    }

    @Nonnull
    protected abstract IMjConnector createConnector();

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos nehighbour) {
        super.onNeighbourBlockChanged(block, nehighbour);
        isRedstonePowered = world.getReceivedRedstonePower(getPos()) > 0;
    }

    // Step 3: ITickable.update() → BlockEntityTicker pattern. The owning Block must return this ticker from
    // Block#getTicker(...). The tick BODY below is preserved verbatim game logic — TODO(R.Chen): unverified.
    public static <T extends TileEngineBase_BC8> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        deltaManager.tick();
        if (cannotUpdate()) return;

        boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;

        if (world.isClient) {
            lastProgress = progress;

            if (isPumping) {
                progress += getPistonSpeed();

                if (progress >= 1) {
                    progress = 0;
                }
            } else if (progress > 0) {
                progress -= 0.01f;
            }
            clientModelData.tick();
            return;
        }

        lastPower = 0;

        if (!isRedstonePowered) {
            if (power > MjAPI.MJ) {
                power -= MjAPI.MJ;
            } else if (power > 0) {
                power = 0;
            }
        }

        updateHeatLevel();
        getPowerStage();
        engineUpdate();

        IMjReceiver receiver = getReceiverToPower(currentDirection);
        boolean pulsedPower = receiver instanceof IMjRedstoneReceiver;

        if (progressPart != 0) {
            progress += getPistonSpeed();

            if (progress > 0.5 && progressPart == 1) {
                progressPart = 2;
                if (pulsedPower) {
                    sendPower(receiver);
                }
            } else if (progress >= 1) {
                progress = 0;
                progressPart = 0;
            }
        } else if (isRedstonePowered && isActive()) {
            if (getPowerToExtract(false) > 0) {
                progressPart = 1;
                setPumping(true);
            } else {
                setPumping(false);
            }
        } else {
            setPumping(false);
        }

        // Comment for constant power
        if (!pulsedPower) {
            if (isRedstonePowered && isActive()) {
                sendPower(receiver);
            } else {
                currentOutput = 0;
            }
        }

        if (!overheat) {
            burn();
        }

        markChunkDirty();
    }

    private long getPowerToExtract(boolean doExtract) {
        IMjReceiver receiver = getReceiverToPower(currentDirection);
        if (receiver == null) {
            return 0;
        }

        // Pulsed power
        return extractPower(0, receiver.getPowerRequested(), doExtract);
        // TODO: Use this:
        // return extractPower(receiver.getMinPowerReceived(), receiver.getMaxPowerReceived(), false);

        // Constant power
        // return extractEnergy(0, getActualOutput(), false); // Uncomment for constant power
    }

    private void sendPower(IMjReceiver receiver) {
        if (receiver != null) {
            long extracted = getPowerToExtract(false);
            if (extracted > 0) {
                long excess = receiver.receivePower(extracted, false);
                extractPower(extracted - excess, extracted - excess, true); // Comment out for constant power
                // currentOutput = extractEnergy(0, needed, true); // Uncomment for constant power
            }
        }
    }

    // Uncomment out for constant power
    // public float getActualOutput() {
    // float heatLevel = getIdealHeatLevel();
    // return getCurrentOutput() * heatLevel;
    // }
    protected void burn() {}

    protected void engineUpdate() {
        if (!isRedstonePowered) {
            if (power >= 1) {
                power -= 1;
            } else if (power < 1) {
                power = 0;
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    protected final void setPumping(boolean isActive) {
        if (this.isPumping == isActive) {
            return;
        }

        this.isPumping = isActive;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    // TEMP
    @FunctionalInterface
    public interface ITileBuffer {
        BlockEntity getTile();
    }

    /** Temp! This should be replaced with a tile buffer! */
    public ITileBuffer getTileBuffer(Direction side) {
        BlockEntity tile = world.getBlockEntity(getPos().offset(side));
        return () -> tile;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        // tileCache = null;
        // checkOrientation = true;
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        // tileCache = null;
        // checkOrientation = true;
    }

    /* STATE INFORMATION */
    public abstract boolean isBurning();

    // IPowerReceptor stuffs -- move!
    // @Override
    // public PowerReceiver getPowerReceiver(ForgeDirection side) {
    // return powerHandler.getPowerReceiver();
    // }
    //
    // @Override
    // public void doWork(PowerHandler workProvider) {
    // if (worldObj.isClient) {
    // return;
    // }
    //
    // addEnergy(powerHandler.useEnergy(1, maxEnergyReceived(), true) * 0.95F);
    // }

    public void addPower(long microJoules) {
        power += microJoules;
        lastPower += microJoules;

        if (getPowerStage() == EnumPowerStage.OVERHEAT) {
            // TODO: turn engine off
            // worldObj.createExplosion(null, xCoord, yCoord, zCoord, explosionRange(), true);
            // worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }

        if (power > getMaxPower()) {
            power = getMaxPower();
        }
    }

    public long extractPower(long min, long max, boolean doExtract) {
        if (power < min) {
            return 0;
        }

        long actualMax;

        if (max > maxPowerExtracted()) {
            actualMax = maxPowerExtracted();
        } else {
            actualMax = max;
        }

        if (actualMax < min) {
            return 0;
        }

        long extracted;

        if (power >= actualMax) {
            extracted = actualMax;

            if (doExtract) {
                power -= actualMax;
            }
        } else {
            extracted = power;

            if (doExtract) {
                power = 0;
            }
        }

        return extracted;
    }

    public final boolean isPoweredTile(BlockEntity tile, Direction side) {
        if (tile == null) return false;
        if (tile.getClass() == getClass()) {
            TileEngineBase_BC8 other = (TileEngineBase_BC8) tile;
            return other.currentDirection == currentDirection;
        }
        return getReceiverToPower(tile, side) != null;
    }

    // TODO(R.Chen): GAME LOGIC / CROSS-MOD — uses the un-migrated buildcraft.api.mj capability
    // (MjAPI.CAP_RECEIVER) and the Forge RF bridge (CapabilityEnergy via MjToRfAutoConvertor). Both depend
    // on the Forge Capability model and must move to Transfer API lookups. The RF fallback branch is stubbed
    // out (returns null) until a Team Reborn EnergyStorage lookup replaces it.
    /** @deprecated Replaced with {@link #getReceiverToPower(Direction)}. */
    @Deprecated
    public IMjReceiver getReceiverToPower(BlockEntity tile, Direction side) {
        if (tile == null) return null;
        // STUB(R.Chen): tile.getCapability(MjAPI.CAP_RECEIVER, side.getOpposite()) — Forge capability lookup.
        IMjReceiver rec = null; // TODO(R.Chen): MjAPI.CAP_RECEIVER lookup via Transfer API.
        if (rec != null && rec.canConnect(mjConnector) && mjConnector.canConnect(rec)) {
            return rec;
        } else if (couldPowerRf()) {
            // STUB(R.Chen): tile.getCapability(CapabilityEnergy.ENERGY, ...) — Forge RF capability.
            return MjToRfAutoConvertor.createReceiver(null); // TODO(R.Chen): Team Reborn EnergyStorage lookup.
        } else {
            return null;
        }
    }

    /** @return True if this engine is allowed to autoconvert output MJ to RF. By default this checks
     *         {@link MjAPI#isRfAutoConversionEnabled()} */
    protected boolean couldPowerRf() {
        return MjAPI.isRfAutoConversionEnabled();
    }

    public IMjReceiver getReceiverToPower(Direction side) {
        TileEngineBase_BC8 engine = this;
        BlockEntity next = null;

        for (int len = 0; len <= getMaxChainLength(); len++) {
            next = engine.getTileBuffer(side).getTile();

            if (next == null) {
                return null;
            }

            if (next.getClass() == getClass()) {
                if (side != ((TileEngineBase_BC8) next).currentDirection) {
                    return null;
                }
            }

            if (next instanceof TileEngineBase_BC8) {
                if (next.getClass() != getClass()) {
                    return null;
                }
                engine = (TileEngineBase_BC8) next;
            } else {
                break;
            }
        }

        if (next == null || next instanceof TileEngineBase_BC8) {
            return null;
        }

        return getReceiverToPower(next, side);
    }

    // STUB(R.Chen): Forge getCapability(Capability<T>, Direction) override removed. The MJ receiver
    // capability (mjCaps) must be exposed via an EnergyStorage.SIDED Transfer API lookup, restricted to
    // currentDirection, registered against the owning BlockEntityType in the ModInitializer.

    public abstract long getMaxPower();

    public long minPowerReceived() {
        return 2 * MjAPI.MJ;
    }

    public abstract long maxPowerReceived();

    public abstract long maxPowerExtracted();

    public abstract float explosionRange();

    public long getEnergyStored() {
        return power;
    }

    public abstract long getCurrentOutput();

    @Override
    public boolean isEngineOn() {
        return isPumping;
    }

    // IEngineLikeForLedger

    @Override
    public final long getCurrentMjOutput() {
        return getCurrentOutput();
    }

    @Override
    public final long getMjStored() {
        return getEnergyStored();
    }

    @Environment(EnvType.CLIENT)
    public float getProgressClient(float partialTicks) {
        float last = lastProgress;
        float now = progress;
        if (last > 0.5 && now < 0.5) {
            // we just returned
            now += 1;
        }
        float interp = last * (1 - partialTicks) + now * partialTicks;
        return interp % 1;
    }

    public Direction getCurrentFacing() {
        return currentDirection;
    }

    // TODO(R.Chen): IDebuggable (buildcraft.api.tiles) is un-migrated and still declares Direction; this
    // override signature uses Direction and will only match once that API is ported.
    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("facing = " + currentDirection);
        left.add("heat = " + LocaleUtil.localizeHeat(heat) + " -- " + StringUtilBC.formatSafe("%.2f %%", getHeatLevel()));
        left.add("power = " + LocaleUtil.localizeMj(power));
        left.add("stage = " + powerStage);
        left.add("progress = " + progress);
        left.add("last = " + LocaleUtil.localizeMjFlow(lastPower));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Current Model Variables:");
        clientModelData.addDebugInfo(left);
    }

    // STUB(R.Chen): hasFastRenderer() was a Forge BlockEntityRenderer hint; removed @Override since
    // BlockEntity has no such method in 1.20.1. TODO(R.Chen): port to Fabric rendering API if needed.
    @Environment(EnvType.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }
}
