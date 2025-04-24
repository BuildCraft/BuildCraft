/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;


import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.energy.blocks.BlockDynamoMJ;
import buildcraft.energy.tile.*;
import buildcraft.lib.block.BlockPropertiesCreator;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.lib.registry.TagManager;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class BCEnergyBlocks {
    public static final RegistrationHelper HELPER = new RegistrationHelper(BCEnergy.MODID);

    public static RegistryObject<BlockEngine_BC8> engineStone;
    public static RegistryObject<BlockEngine_BC8> engineIron;
    public static RegistryObject<BlockEngine_BC8> engineRf;

    public static RegistryObject<BlockDynamoMJ> mjDynamo;

    public static RegistryObject<TileEntityType<TileSpringOil>> springTile;
    public static RegistryObject<TileEntityType<TileEngineStone_BC8>> engineStoneTile;
    public static RegistryObject<TileEntityType<TileEngineIron_BC8>> engineIronTile;
    public static RegistryObject<TileEntityType<TileEngineRF>> engineRfTile;

    public static RegistryObject<TileEntityType<TileDynamoMJ>> mjDynamoTile;

    public static void preInit() {
        if (BCCoreBlocks.engineWood != null) {
            engineStone = BCCoreBlocks.registerEngine(EnumEngineType.STONE, TileEngineStone_BC8::new);
            engineIron = BCCoreBlocks.registerEngine(EnumEngineType.IRON, TileEngineIron_BC8::new);

            engineStoneTile = BCCoreBlocks.HELPER.registerTile("tile.engine.stone", TileEngineStone_BC8::new, BCEnergyBlocks.engineStone);
            engineIronTile = BCCoreBlocks.HELPER.registerTile("tile.engine.iron", TileEngineIron_BC8::new, BCEnergyBlocks.engineIron);
            if (BCEnergyConfig.enableRfEngine) {
                engineRf = BCCoreBlocks.registerEngine(EnumEngineType.RF, TileEngineRF::new);

                engineRfTile = BCCoreBlocks.HELPER.registerTile("tile.engine.rf", TileEngineRF::new, BCEnergyBlocks.engineRf);
            }
        }

        if (BCEnergyConfig.enableMjDynamo) {
            mjDynamo = HELPER.addForcedBlock(
                    "block.mj_dynamo",
                    BlockPropertiesCreator.createDefaultProperties(Material.METAL)
                            .strength(5.0F, 10.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion(),
                    BlockDynamoMJ::new
            );
            String mjDynamoItemRegId = TagManager.getTag("item.block.mj_dynamo", TagManager.EnumTagType.REGISTRY_NAME).replace(BCEnergy.MODID + ":", "");
            HELPER.addForcedItem(mjDynamoItemRegId, () -> new ItemBlockBC_Neptune(mjDynamo.get(), ItemPropertiesCreator.common64()));
            mjDynamoTile = HELPER.registerTile("tile.mj_dynamo", TileDynamoMJ::new, mjDynamo);
        }

//        EnumSpring.OIL.liquidBlock = BCEnergyFluids.crudeOil[0].getBlock().getDefaultState(); // 1.18.2: moved to BCEnergy#postInit
        EnumSpring.OIL.tileConstructor = TileSpringOil::new;

        springTile = BCCoreBlocks.HELPER.registerTile("tile.spring.oil", TileSpringOil::new, BCCoreBlocks.springOil);
    }
}
