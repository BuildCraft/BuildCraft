/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import java.util.function.Consumer;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;

//@formatter:off
@Mod(
    modid = BCEnergy.MODID,
    name = "BuildCraft Energy",
    version = BCLib.VERSION,
    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
)
//@formatter:on
public class BCEnergy {
    public static final String MODID = "buildcraftenergy";
    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.Instance(MODID)
    public static BCEnergy INSTANCE;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);
        BCEnergyConfig.preInit();
        BCEnergyItems.preInit();
        BCEnergyFluids.preInit();
        BCEnergyBlocks.preInit();
        BCEnergyEntities.preInit();
        BCEnergyWorldGen.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCEnergyProxy.getProxy());

        BCEnergyProxy.getProxy().fmlPreInit();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCEnergyRecipes.init();
        BCEnergyProxy.getProxy().fmlInit();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCEnergyProxy.getProxy().fmlPostInit();
        BCEnergyConfig.validateBiomeNames();
        registerMigrations();
    }

    private static void registerMigrations() {
        /** 7.99.0 */
        // Fluid registration changed from "fluid_block_[FLUID]" to "fluid_block_heat_[HEAT]_[FLUID]"
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.crudeOil[0].getBlock(), "fluid_block_oil");
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.fuelLight[0].getBlock(), "fluid_block_fuel");
    }

    static {
        startBatch();
        // Items
        registerTag("item.glob.oil").reg("glob_oil").locale("globOil").model("glob_oil");

        // Item Blocks

        // Blocks

        // Tiles
        registerTag("tile.engine.stone").reg("engine.stone");
        registerTag("tile.engine.iron").reg("engine.iron");
        registerTag("tile.spring.oil").reg("spring.oil");

        endBatch(TagManager.prependTags("buildcraftenergy:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
            .andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
        return TagManager.registerTag(id);
    }

    private static void startBatch() {
        TagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
        TagManager.endBatch(consumer);
    }
}
