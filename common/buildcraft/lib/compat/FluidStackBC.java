/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat;

import java.util.Objects;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;

/**
 * Compatibility shim replacing {@code net.minecraftforge.fluids.FluidStack}.
 *
 * Forge FluidStack paired a {@code Fluid} + integer millibucket amount.
 * Fabric's {@link FluidVariant} is the typed fluid token; amounts are tracked
 * in <b>droplets</b> (1 bucket = {@link BCFluidStorage#BUCKET} = 81000 droplets).
 *
 * This class mirrors the Forge API surface (getFluid, getAmount, isEmpty,
 * copy, NBT round-trip) so that unmigrated call sites compile without change.
 *
 * TODO(R.Chen): audit all call sites and migrate to FluidVariant + long natively.
 */
public final class FluidStackBC {

    public static final FluidStackBC EMPTY = new FluidStackBC(FluidVariant.blank(), 0L);

    private final FluidVariant fluid;
    /** Amount in droplets. Public for Forge API compat (was {@code public int amount} in ForgeFluidStack). */
    public long amount;

    private FluidStackBC(FluidVariant fluid, long amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    // ── Forge compat constructors (amount in millibuckets) ────────────────────

    /** Forge compat: {@code new FluidStack(fluid, amountMB)}. Amount is in mB (1 bucket = 1000 mB). */
    public FluidStackBC(Fluid fluid, int amountMB) {
        this(FluidVariant.of(fluid), BCFluidStorage.mBToDroplets(amountMB));
    }

    /** Forge compat: {@code new FluidStack(stack, amountMB)}. Copy with new mB amount. */
    public FluidStackBC(FluidStackBC stack, int amountMB) {
        this(stack.fluid, BCFluidStorage.mBToDroplets(amountMB));
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /** Primary factory: fluid variant + amount in droplets. */
    public static FluidStackBC of(FluidVariant fluid, long amount) {
        if (fluid.isBlank() || amount <= 0) return EMPTY;
        return new FluidStackBC(fluid, amount);
    }

    /** Convenience factory from a plain Fluid (no extra NBT on the variant). */
    public static FluidStackBC of(Fluid fluid, long amount) {
        return of(FluidVariant.of(fluid), amount);
    }

    /** Factory from Forge-style millibucket amount. */
    public static FluidStackBC ofMilliBuckets(FluidVariant fluid, long mB) {
        return of(fluid, BCFluidStorage.mBToDroplets(mB));
    }

    public FluidVariant getFluidVariant() {
        return fluid;
    }

    /** Forge compat: returns the unwrapped {@link Fluid}. */
    public Fluid getFluid() {
        return fluid.getFluid();
    }

    /** Amount in droplets. */
    public long getAmount() {
        return amount;
    }

    /** Amount in Forge-style millibuckets (truncates sub-mB remainder). */
    public long getAmountMB() {
        return BCFluidStorage.dropletsToMB(amount);
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isEmpty() {
        return fluid.isBlank() || amount <= 0;
    }

    public FluidStackBC copy() {
        if (isEmpty()) return EMPTY;
        return new FluidStackBC(fluid, amount);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    public NbtCompound toNbt(NbtCompound tag) {
        fluid.toNbt(tag);
        tag.putLong("Amount", amount);
        return tag;
    }

    public NbtCompound toNbt() {
        return toNbt(new NbtCompound());
    }

    /** Forge compat alias for {@link #toNbt(NbtCompound)}. */
    public NbtCompound writeToNBT(NbtCompound tag) {
        return toNbt(tag);
    }

    @Nullable
    public static FluidStackBC fromNbt(NbtCompound tag) {
        if (tag == null || tag.isEmpty()) return EMPTY;
        FluidVariant fv = FluidVariant.fromNbt(tag);
        if (fv.isBlank()) return EMPTY;
        long amt = tag.getLong("Amount");
        return of(fv, amt);
    }

    /** Forge compat alias for {@link #fromNbt(NbtCompound)}. */
    @Nullable
    public static FluidStackBC loadFluidStackFromNBT(NbtCompound tag) {
        return fromNbt(tag);
    }

    // ── Object ────────────────────────────────────────────────────────────────

    /** True when both variant and amount match (Forge FluidStack equality semantics). */
    public boolean isFluidEqual(FluidStackBC other) {
        return other != null && fluid.equals(other.fluid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidStackBC)) return false;
        FluidStackBC that = (FluidStackBC) o;
        return amount == that.amount && Objects.equals(fluid, that.fluid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, amount);
    }

    @Override
    public String toString() {
        if (isEmpty()) return "FluidStackBC.EMPTY";
        return "FluidStackBC{" + fluid + " x" + amount + "droplets}";
    }
}
