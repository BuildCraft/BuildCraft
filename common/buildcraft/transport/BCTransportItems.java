/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import net.minecraft.item.Item;

import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.item.ItemWire;

// STUB(R.Chen): item registration deferred — ItemPipeHolder/ItemWire/ItemBC_Neptune/ItemPluggableSimple
// not yet migrated (lib.item layer). preInit is a no-op. Phase 4F.
public class BCTransportItems {

    public static ItemPipeHolder pipeStructure;
    public static ItemPipeHolder pipeItemWood, pipeFluidWood, pipePowerWood, pipeRfWood;
    public static ItemPipeHolder pipeItemStone, pipeFluidStone, pipePowerStone, pipeRfStone;
    public static ItemPipeHolder pipeItemCobble, pipeFluidCobble, pipePowerCobble, pipeRfCobble;
    public static ItemPipeHolder pipeItemQuartz, pipeFluidQuartz, pipePowerQuartz, pipeRfQuartz;
    public static ItemPipeHolder pipeItemGold, pipeFluidGold, pipePowerGold, pipeRfGold;
    public static ItemPipeHolder pipeItemSandstone, pipeFluidSandstone, pipePowerSandstone, pipeRfSandstone;
    public static ItemPipeHolder pipeItemIron, pipeFluidIron, pipePowerIron, pipeRfIron;
    public static ItemPipeHolder pipeItemDiamond, pipeFluidDiamond, pipePowerDiamond, pipeRfDiamond;
    public static ItemPipeHolder pipeItemDiaWood, pipeFluidDiaWood, pipePowerDiaWood, pipeRfDiaWood;
    public static ItemPipeHolder pipeItemClay, pipeFluidClay;
    public static ItemPipeHolder pipeItemVoid, pipeFluidVoid;
    public static ItemPipeHolder pipeItemObsidian, pipeFluidObsidian;
    public static ItemPipeHolder pipeItemLapis;
    public static ItemPipeHolder pipeItemDaizuli;
    public static ItemPipeHolder pipeItemEmzuli;
    public static ItemPipeHolder pipeItemStripes;

    public static Item waterproof;
    public static Item plugBlocker;
    public static Item plugPowerAdaptor;
    public static ItemWire wire;

    public static void preInit() {
        // STUB(R.Chen): item registration deferred to Phase 4F.
    }
}
