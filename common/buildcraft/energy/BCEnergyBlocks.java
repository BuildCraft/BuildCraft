/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.minecraft.block.Block;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.enums.EnumSpring;

import buildcraft.energy.tile.TileSpringOil;

@Mod.EventBusSubscriber(modid = BCEnergy.MODID)
@GameRegistry.ObjectHolder(BCEnergy.MODID)
public class BCEnergyBlocks {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        BCEnergyFluids.register();
        EnumSpring.OIL.tileConstructor = TileSpringOil::new;
    }
}
