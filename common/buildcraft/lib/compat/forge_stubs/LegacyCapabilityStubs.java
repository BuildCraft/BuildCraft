/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat.forge_stubs;

/**
 * STUB(R.Chen): umbrella tracker for legacy {@code buildcraft.lib} files that
 * still depend on Forge capabilities ({@code IItemHandler}, {@code IFluidHandler},
 * {@code IEnergyStorage}, or the {@code Capability<T>} provider model).
 *
 * The replacements live in:
 *   buildcraft.lib.compat.BCItemStorage     — Storage&lt;ItemVariant&gt;
 *   buildcraft.lib.compat.BCFluidStorage    — Storage&lt;FluidVariant&gt;
 *   buildcraft.lib.mj.MJEnergyStorage       — Team Reborn EnergyStorage (µJ-based)
 *
 * Legacy files (NOT migrated in place — excluded from libLeaf until their dependents are ported):
 *
 *   ── Capability core ──
 *   common/buildcraft/lib/cap/CapabilityHelper.java
 *   common/buildcraft/lib/misc/CapUtil.java
 *
 *   ── Item handlers (Forge IItemHandler / IItemHandlerModifiable) ──
 *   common/buildcraft/lib/tile/TileBC_Neptune.java
 *   common/buildcraft/lib/tile/item/IItemHandlerAdv.java
 *   common/buildcraft/lib/tile/item/ItemHandlerSimple.java
 *   common/buildcraft/lib/tile/item/ItemHandlerFiltered.java
 *   common/buildcraft/lib/tile/item/ItemHandlerManager.java
 *   common/buildcraft/lib/tile/item/StackChangeCallback.java
 *   common/buildcraft/lib/tile/item/DelegateItemHandler.java
 *   common/buildcraft/lib/tile/item/CombinedItemHandlerWrapper.java
 *   common/buildcraft/lib/tile/item/WrappedItemHandlerInsert.java
 *   common/buildcraft/lib/tile/item/WrappedItemHandlerExtract.java
 *   common/buildcraft/lib/tile/craft/WorkbenchCrafting.java
 *   common/buildcraft/lib/inventory/ItemHandlerWrapper.java
 *   common/buildcraft/lib/inventory/ItemTransactorHelper.java
 *   common/buildcraft/lib/inventory/filter/DelegatingItemHandlerFilter.java
 *   common/buildcraft/lib/misc/InventoryUtil.java
 *   common/buildcraft/lib/gui/ContainerBC_Neptune.java
 *   common/buildcraft/lib/gui/json/InventorySlotHolder.java
 *   common/buildcraft/lib/gui/slot/SlotBase.java
 *
 *   ── Fluids (Forge FluidStack / IFluidHandler / Fluid / FluidRegistry / BlockFluid*) ──
 *   common/buildcraft/lib/fluid/Tank.java
 *   common/buildcraft/lib/fluid/TankManager.java
 *   common/buildcraft/lib/fluid/TankProperties.java
 *   common/buildcraft/lib/fluid/SingleUseTank.java
 *   common/buildcraft/lib/fluid/FluidSmoother.java
 *   common/buildcraft/lib/fluid/FluidManager.java
 *   common/buildcraft/lib/fluid/CoolantRegistry.java
 *   common/buildcraft/lib/fluid/FuelRegistry.java
 *   common/buildcraft/lib/fluid/BCFluid.java
 *   common/buildcraft/lib/fluid/BCFluidBlock.java
 *   common/buildcraft/lib/inventory/filter/SimpleFluidFilter.java
 *   common/buildcraft/lib/inventory/filter/PassThroughFluidFilter.java
 *   common/buildcraft/lib/inventory/filter/InvertedFluidFilter.java
 *   common/buildcraft/lib/inventory/filter/ArrayFluidFilter.java
 *   common/buildcraft/lib/list/ListMatchHandlerFluid.java
 *   common/buildcraft/lib/misc/FluidUtilBC.java
 *   common/buildcraft/lib/misc/BlockUtil.java
 *   common/buildcraft/lib/misc/StringUtilBC.java
 *   common/buildcraft/lib/misc/SoundUtil.java
 *   common/buildcraft/lib/misc/GuiUtil.java
 *   common/buildcraft/lib/misc/LocaleUtil.java
 *   common/buildcraft/lib/misc/JsonUtil.java
 *   common/buildcraft/lib/net/cache/NetworkedFluidStackCache.java
 *   common/buildcraft/lib/net/cache/NetworkedObjectCache.java
 *   common/buildcraft/lib/net/cache/BuildCraftObjectCaches.java
 *   common/buildcraft/lib/recipe/RefineryRecipeRegistry.java
 *   common/buildcraft/lib/client/render/fluid/FluidRenderer.java
 *   common/buildcraft/lib/client/render/fluid/FluidSpriteType.java
 *
 *   ── Energy (Forge IEnergyStorage / CapabilityEnergy — NOT MJ) ──
 *   common/buildcraft/lib/engine/TileEngineBase_BC8.java
 *
 * No runtime members — this class exists solely as a porting tracker.
 */
public final class LegacyCapabilityStubs {
    private LegacyCapabilityStubs() {}
}
