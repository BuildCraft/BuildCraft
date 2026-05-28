/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.mj;

import team.reborn.energy.api.EnergyStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/**
 * MJ (MinecraftJoule) energy buffer for BuildCraft.
 *
 * <h2>Unit choice</h2>
 * The legacy BuildCraft codebase exposes MJ as {@code double} values where
 * {@code 1.0 MJ} is the canonical step. To bridge into Team Reborn's
 * {@link EnergyStorage} (which is {@code long}-based) without losing precision,
 * this class stores power internally as <b>microJoules</b>:
 * <pre>
 *     1 MJ  = 1_000_000 µJ
 * </pre>
 * Team Reborn callers see micro-joules directly (1 TR unit == 1 µJ). MJ callers
 * use the {@code double} API and pay one multiplication per call.
 *
 * <h2>Why not RF/FE bridging?</h2>
 * Forge's IEnergyStorage uses RF/FE. BuildCraft MJ is <i>not</i> RF — it is a
 * physically-motivated unit (joules) with its own balance. Cross-mod compat is
 * an explicit follow-up (Step 7 in the migration plan); this class deliberately
 * does NOT implement {@code IEnergyStorage} or a fixed MJ↔RF conversion.
 *
 * TODO(R.Chen): once {@code buildcraft.api.mj} is ported, fold the legacy
 *               {@code IMjReadable} / {@code IMjStorage} interfaces in here too.
 * TODO(R.Chen): expose an MJ↔E (Team Reborn) conversion ratio knob via config
 *               so server admins can tune cross-mod balance. Default 1:1 µJ↔E.
 */
public class MJEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {

    /** Conversion: 1 MJ == this many internal/Team-Reborn units. */
    public static final long MJ_TO_INTERNAL = 1_000_000L;

    protected final long capacity;       // in µJ
    protected final long maxInsert;      // in µJ per insert call
    protected final long maxExtract;     // in µJ per extract call
    protected long stored;               // in µJ

    public MJEnergyStorage(long capacityMicroJoules, long maxInsertMicroJoules, long maxExtractMicroJoules) {
        this.capacity = capacityMicroJoules;
        this.maxInsert = maxInsertMicroJoules;
        this.maxExtract = maxExtractMicroJoules;
    }

    /** Convenience constructor with limits expressed in whole MJ. */
    public static MJEnergyStorage ofMJ(double capacityMJ, double maxInsertMJ, double maxExtractMJ) {
        return new MJEnergyStorage(
            (long) (capacityMJ   * MJ_TO_INTERNAL),
            (long) (maxInsertMJ  * MJ_TO_INTERNAL),
            (long) (maxExtractMJ * MJ_TO_INTERNAL)
        );
    }

    // -----------------------------------------------------------------------
    // MJ-native API (matches legacy method names)
    // -----------------------------------------------------------------------

    // TODO(R.Chen): the legacy API exposed {@code double getStored()} / {@code double getCapacity()}.
    // Renamed to *MJ() because Team Reborn's EnergyStorage already defines
    // {@code long getCapacity()} and Java forbids return-type-only overloads.
    // Call sites should be updated when the legacy code is ported.

    /** @return current stored energy in MJ (may be fractional). */
    public double getStoredMJ() {
        return stored / (double) MJ_TO_INTERNAL;
    }

    /** @return total capacity in MJ. */
    public double getCapacityMJ() {
        return capacity / (double) MJ_TO_INTERNAL;
    }

    /** Raw micro-joule accessor (preferred for internal accounting). */
    public long getStoredMicroJoules() {
        return stored;
    }

    /**
     * Try to add {@code microJoules} of energy. Mirrors legacy
     * {@code addPowerChecking(double, boolean)}.
     *
     * @return micro-joules actually added (0 if full or simulating-and-checking)
     */
    public long addPower(long microJoules, boolean simulate) {
        long space = capacity - stored;
        long accepted = Math.min(microJoules, Math.min(space, maxInsert));
        if (!simulate) {
            stored += accepted;
        }
        return accepted;
    }

    /**
     * Try to draw between {@code min} and {@code max} micro-joules. Mirrors
     * legacy {@code extractPower(double, double, boolean)} — if at least
     * {@code min} cannot be drawn, return 0 (atomic semantics).
     */
    public long extractPower(long min, long max, boolean simulate) {
        long available = Math.min(stored, maxExtract);
        if (available < min) return 0;
        long extracted = Math.min(available, max);
        if (!simulate) {
            stored -= extracted;
        }
        return extracted;
    }

    // -----------------------------------------------------------------------
    // Team Reborn EnergyStorage (transactional)
    // -----------------------------------------------------------------------

    @Override public boolean supportsInsertion()  { return maxInsert > 0; }
    @Override public boolean supportsExtraction() { return maxExtract > 0; }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long space = capacity - stored;
        long accepted = Math.min(maxAmount, Math.min(space, maxInsert));
        if (accepted > 0) {
            updateSnapshots(transaction);
            stored += accepted;
        }
        return accepted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long available = Math.min(stored, maxExtract);
        long extracted = Math.min(maxAmount, available);
        if (extracted > 0) {
            updateSnapshots(transaction);
            stored -= extracted;
        }
        return extracted;
    }

    @Override public long getAmount()   { return stored; }
    @Override public long getCapacity() { return capacity; }

    // SnapshotParticipant — restore previous {@code stored} value on transaction abort.
    @Override protected Long createSnapshot()                  { return stored; }
    @Override protected void readSnapshot(Long snapshot)       { this.stored = snapshot; }
}
