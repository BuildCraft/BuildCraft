/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import java.util.Locale;
import java.util.Set;
import com.google.common.base.Throwables;
import org.apache.logging.log4j.Level;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.core.StackKey;
import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.enums.EnumSpring;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.BCRegistry;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.config.ConfigManager.RestartRequirement;
import buildcraft.core.lib.client.sprite.SpriteColourMapper;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.fluids.FluidDefinition;
import buildcraft.core.lib.network.base.ChannelHandler;
import buildcraft.core.lib.network.base.PacketHandler;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.energy.EnergyGuiHandler;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.IMCHandlerEnergy;
import buildcraft.energy.TileEngineCreative;
import buildcraft.energy.TileEngineIron;
import buildcraft.energy.TileEngineStone;
import buildcraft.energy.fuels.CoolantManager;
import buildcraft.energy.fuels.FuelManager;
import buildcraft.energy.statements.EnergyStatementProvider;
import buildcraft.energy.statements.TriggerCoolantBelowThreshold;
import buildcraft.energy.statements.TriggerEngineHeat;
import buildcraft.energy.statements.TriggerFuelBelowThreshold;
import buildcraft.energy.worldgen.BiomeGenOilDesert;
import buildcraft.energy.worldgen.BiomeGenOilOcean;
import buildcraft.energy.worldgen.BiomeInitializer;
import buildcraft.energy.worldgen.OilPopulate;

@Mod(name = "BuildCraft Energy", version = DefaultProps.VERSION, useMetadata = false, modid = "BuildCraft|Energy",
        dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftEnergy extends BuildCraftMod {

    @Mod.Instance("BuildCraft|Energy")
    public static BuildCraftEnergy instance;

    public static boolean spawnOilSprings = true;
    public static BiomeGenOilDesert biomeOilDesert;
    public static BiomeGenOilOcean biomeOilOcean;

    public static FluidDefinition oil;
    public static FluidDefinition fuel;
    public static FluidDefinition redPlasma;

    public static Achievement engineAchievement2;
    public static Achievement engineAchievement3;

    public static boolean canOilBurn;
    public static boolean isOilDense;
    public static double oilWellScalar = 1.0;

    public static ITriggerExternal triggerBlueEngineHeat = new TriggerEngineHeat(EnumEnergyStage.BLUE);
    public static ITriggerExternal triggerGreenEngineHeat = new TriggerEngineHeat(EnumEnergyStage.GREEN);
    public static ITriggerExternal triggerYellowEngineHeat = new TriggerEngineHeat(EnumEnergyStage.YELLOW);
    public static ITriggerExternal triggerRedEngineHeat = new TriggerEngineHeat(EnumEnergyStage.RED);
    public static ITriggerExternal triggerEngineOverheat = new TriggerEngineHeat(EnumEnergyStage.OVERHEAT);

    public static ITriggerExternal triggerFuelBelow25 = new TriggerFuelBelowThreshold(0.25F);
    public static ITriggerExternal triggerFuelBelow50 = new TriggerFuelBelowThreshold(0.50F);

    public static ITriggerExternal triggerCoolantBelow25 = new TriggerCoolantBelowThreshold(0.25F);
    public static ITriggerExternal triggerCoolantBelow50 = new TriggerCoolantBelowThreshold(0.50F);

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        BuildcraftFuelRegistry.fuel = FuelManager.INSTANCE;
        BuildcraftFuelRegistry.coolant = CoolantManager.INSTANCE;

        int oilDesertBiomeId = BuildCraftCore.mainConfigManager.register("worldgen.biomes", "biomeOilDesert", DefaultProps.BIOME_OIL_DESERT,
                "The id for the Oil Desert biome", RestartRequirement.GAME).getInt();
        int oilOceanBiomeId = BuildCraftCore.mainConfigManager.register("worldgen.biomes", "biomeOilOcean", DefaultProps.BIOME_OIL_OCEAN,
                "The id for the Oil Ocean biome", RestartRequirement.GAME).getInt();

        BuildCraftCore.mainConfigManager.register("worldgen.spawnOilSprings", true, "Should I spawn oil springs?",
                ConfigManager.RestartRequirement.GAME);
        BuildCraftCore.mainConfigManager.register("worldgen.oilWellGenerationRate", 1.0D,
                "How high should be the probability of an oil well generating?", ConfigManager.RestartRequirement.NONE);

        setBiomeList(OilPopulate.INSTANCE.surfaceDepositBiomes, BuildCraftCore.mainConfigManager.register("worldgen.biomes", "increasedOilIDs",
                new String[] { BiomeDictionary.Type.SANDY.toString(), BiomeGenBase.taiga.biomeName },
                "IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that should have increased oil generation rates.", RestartRequirement.GAME));

        setBiomeList(OilPopulate.INSTANCE.excessiveBiomes, BuildCraftCore.mainConfigManager.register("worldgen.biomes", "excessiveOilIDs",
                new String[] {}, "IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that should have GREATLY increased oil generation rates.",
                RestartRequirement.GAME));

        setBiomeList(OilPopulate.INSTANCE.excludedBiomes, BuildCraftCore.mainConfigManager.register("worldgen.biomes", "excludeOilIDs", new String[] {
            BiomeGenBase.sky.biomeName, BiomeGenBase.hell.biomeName },
                "IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that are excluded from generating oil.", RestartRequirement.GAME));

        BuildCraftCore.mainConfigManager.register("general", "fuel.oil.combustion", 1.0F, "adjust energy value of Oil in Combustion Engines",
                RestartRequirement.GAME);
        BuildCraftCore.mainConfigManager.register("general", "fuel.fuel.combustion", 1.0F, "adjust energy value of Fuel in Combustion Engines",
                RestartRequirement.GAME);

        BuildCraftCore.mainConfigManager.register("general", "fuel.oil.combustion.energyOutput", 30,
                "adjust output energy by Oil in Combustion Engines", RestartRequirement.GAME);
        BuildCraftCore.mainConfigManager.register("general", "fuel.fuel.combustion.energyOutput", 60,
                "adjust output energy by Fuel in Combustion Engines", RestartRequirement.GAME);

        BuildCraftCore.mainConfiguration.save();

        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();

        if (oilDesertBiomeId > 0) {
            if (oilDesertBiomeId >= biomeGenArray.length || biomeGenArray[oilDesertBiomeId] != null) {
                oilDesertBiomeId = findUnusedBiomeID("oilDesert");
                // save changes to config file
                BuildCraftCore.mainConfiguration.get("worldgen.biomes", "biomeOilDesert", oilDesertBiomeId).set(oilDesertBiomeId);
                BuildCraftCore.mainConfiguration.save();
            }
            biomeOilDesert = BiomeGenOilDesert.makeBiome(oilDesertBiomeId);
        }

        if (oilOceanBiomeId > 0) {
            if (oilOceanBiomeId >= biomeGenArray.length || biomeGenArray[oilOceanBiomeId] != null) {
                oilOceanBiomeId = findUnusedBiomeID("oilOcean");
                // save changes to config file
                BuildCraftCore.mainConfiguration.get("worldgen.biomes", "biomeOilOcean", oilOceanBiomeId).set(oilOceanBiomeId);
                BuildCraftCore.mainConfiguration.save();
            }
            biomeOilOcean = BiomeGenOilOcean.makeBiome(oilOceanBiomeId);
        }

        // Only register oil and fuel if factory is NOT loaded, as then factory controls all refining stuffs.
        if (!Loader.isModLoaded("BuildCraft|Factory") || !BuildCraftCore.DEVELOPER_MODE) {
            oil = new FluidDefinition("oil", 800, 10000, true);
            oil.block.setLightOpacity(8);
            oil.fluid.setColour(0x50_50_50, 0x05_05_05);
            oil.block.setFlammability(0);
            BuildCraftCore.mainConfigManager.register("general.oilCanBurn", true, "Should oil burn when lit on fire?",
                    ConfigManager.RestartRequirement.NONE);
            BuildCraftCore.mainConfigManager.register("general.oilIsDense", true, "Should oil be dense and push enties up?",
                    ConfigManager.RestartRequirement.NONE);

            fuel = new FluidDefinition("fuel", 1000, 1000, true);
            fuel.block.setFlammable(true).setFlammability(5).setParticleColor(0.7F, 0.7F, 0.0F);
            fuel.fluid.setColour(0xFF_FF_30, 0xE4_CF_00);

            spawnOilSprings = BuildCraftCore.mainConfigManager.get("worldgen.spawnOilSprings").getBoolean(true);
            EnumSpring.OIL.canGen = spawnOilSprings;
            EnumSpring.OIL.liquidBlock = oil.block.getDefaultState();
        }

        if (BuildCraftCore.DEVELOPER_MODE) {
            redPlasma = new FluidDefinition("redplasma", 10000, 10000, false);
            redPlasma.fluid.setLuminosity(30);
            redPlasma.block.setFlammable(false).setParticleColor(0.9F, 0, 0);
        }

        BuildCraftCore.engineBlock.registerTile(TileEngineStone.class, 1, "tile.engineStone");
        BuildCraftCore.engineBlock.registerTile(TileEngineIron.class, 2, "tile.engineIron");
        BuildCraftCore.engineBlock.registerTile(TileEngineCreative.class, 3, "tile.engineCreative");

        InterModComms.registerHandler(new IMCHandlerEnergy());

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {
            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
            oilWellScalar = BuildCraftCore.mainConfigManager.get("worldgen.oilWellGenerationRate").getDouble();

            if (!Loader.isModLoaded("BuildCraft|Factory") || !BuildCraftCore.DEVELOPER_MODE) {
                canOilBurn = BuildCraftCore.mainConfigManager.get("general.oilCanBurn").getBoolean();
                isOilDense = BuildCraftCore.mainConfigManager.get("general.oilIsDense").getBoolean();
                oil.block.setFlammable(canOilBurn).setDense(isOilDense);
            }

            if (BuildCraftCore.mainConfiguration.hasChanged()) {
                BuildCraftCore.mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if ("BuildCraft|Core".equals(event.modID)) {
            reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
        }
    }

    private void setBiomeList(Set<Integer> list, Property configuration) {
        for (String id : configuration.getStringList()) {
            String strippedId = JavaTools.stripSurroundingQuotes(id.trim());

            if (strippedId.length() > 0) {
                if (strippedId.matches("-?\\d+(\\.\\d+)?")) {
                    try {
                        list.add(Integer.parseInt(strippedId));
                    } catch (NumberFormatException ex) {
                        BCLog.logger.log(Level.WARN, configuration.getName() + ": Could not find biome id: " + strippedId + " ; Skipping!");
                    }
                } else {
                    boolean found = false;
                    String biomeName = strippedId.toUpperCase();

                    for (BiomeDictionary.Type t : BiomeDictionary.Type.values()) {
                        String biomeType = t.name().toUpperCase();

                        for (BiomeGenBase b : BiomeDictionary.getBiomesForType(t)) {
                            if (b.biomeName.toUpperCase().equals(biomeName) || biomeType.toUpperCase().equals(biomeName)) {
                                list.add(b.biomeID);
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        BCLog.logger.log(Level.WARN, configuration.getName() + ": Could not find biome id: " + strippedId + " ; Skipping!");
                    }
                }
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        channels = NetworkRegistry.INSTANCE.newChannel(DefaultProps.NET_CHANNEL_NAME + "-ENERGY", new ChannelHandler(), new PacketHandler());

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new EnergyGuiHandler());

        StatementManager.registerTriggerProvider(new EnergyStatementProvider());

        if (BuildCraftCore.loadDefaultRecipes) {
            loadRecipes();
        }

        reloadConfig(ConfigManager.RestartRequirement.GAME);

        BuildcraftRecipeRegistry.refinery.addRecipe("buildcraft:fuel", new FluidStack(oil.fluid, 1), new FluidStack(fuel.fluid, 1), 120, 1);

        double fuelOilMultiplier = BuildCraftCore.mainConfigManager.get("general", "fuel.oil.combustion").getDouble();
        double fuelFuelMultiplier = BuildCraftCore.mainConfigManager.get("general", "fuel.fuel.combustion").getDouble();

        int fuelOilEnergyOutput = BuildCraftCore.mainConfigManager.get("general", "fuel.oil.combustion.energyOutput").getInt();
        int fuelFuelEnergyOutput = BuildCraftCore.mainConfigManager.get("general", "fuel.fuel.combustion.energyOutput").getInt();

        if (!Loader.isModLoaded("BuildCraft|Factory") || !BuildCraftCore.DEVELOPER_MODE) {
            BuildcraftFuelRegistry.fuel.addFuel(oil.fluid, fuelOilEnergyOutput, (int) (5000 * fuelOilMultiplier));
            BuildcraftFuelRegistry.fuel.addFuel(fuel.fluid, fuelFuelEnergyOutput, (int) (25000 * fuelFuelMultiplier));
        }

        BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(StackKey.stack(Blocks.ice), StackKey.fluid(FluidRegistry.WATER), 1.5f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(StackKey.stack(Blocks.packed_ice), StackKey.fluid(FluidRegistry.WATER), 2.0f);

        EnergyProxy.proxy.registerBlockRenderers();
        EnergyProxy.proxy.registerTileEntities();

        engineAchievement2 = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|energy:achievement.stirlingEngine",
                "engineAchievement2", 3, -2, new ItemStack(BuildCraftCore.engineBlock, 1, 1), BuildCraftCore.engineRedstoneAchievement));
        engineAchievement3 = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|energy:achievement.combustionEngine",
                "engineAchievement3", 5, -2, new ItemStack(BuildCraftCore.engineBlock, 1, 2), engineAchievement2));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        if (BuildCraftCore.modifyWorld) {
            MinecraftForge.EVENT_BUS.register(OilPopulate.INSTANCE);
            MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
        }
    }

    public static void loadRecipes() {
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(BuildCraftCore.engineBlock, 1, 1), "www", " g ", "GpG", 'w', "cobblestone", 'g',
                "blockGlass", 'G', "gearStone", 'p', Blocks.piston);
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(BuildCraftCore.engineBlock, 1, 2), "www", " g ", "GpG", 'w', "ingotIron", 'g',
                "blockGlass", 'G', "gearIron", 'p', Blocks.piston);
    }

    private int findUnusedBiomeID(String biomeName) {
        int freeBiomeID;
        // code to find a free biome
        for (int i = 1; i < 256; i++) {
            if (BiomeGenBase.getBiomeGenArray()[i] == null) {
                freeBiomeID = i;
                return freeBiomeID;
            }
        }
        // failed to find any free biome IDs
        throw new RuntimeException("You have run out of free Biome ID spaces for " + biomeName);
    }

    @Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

    @Mod.EventHandler
    public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileEngineBase.class.getCanonicalName());
    }

    @Mod.EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        Throwable error = null;
        BCLog.logger.info("Energy|Remap " + System.identityHashCode(event));
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            try {
                final String name = mapping.name;
                final String domain = mapping.resourceLocation.getResourceDomain().toLowerCase(Locale.ROOT);
                final String path = mapping.resourceLocation.getResourcePath().toLowerCase(Locale.ROOT);
                BCLog.logger.info("        - " + name + " " + mapping.type + " [" + domain + "][" + path + "]");

                if (name.equals("BuildCraft|Energy:engineBlock")) {
                    if (mapping.type == GameRegistry.Type.BLOCK) {
                        mapping.remap(BuildCraftCore.engineBlock);
                    } else if (mapping.type == GameRegistry.Type.ITEM) {
                        mapping.remap(Item.getItemFromBlock(BuildCraftCore.engineBlock));
                    }
                }

                if (domain.equals("buildcraft|energy")) {
                    if (path.contains("_")) continue;
                    if (path.endsWith("oil") && !Loader.isModLoaded("BuildCraft|Factory")) {
                        if (mapping.type == GameRegistry.Type.BLOCK) {
                            mapping.remap(oil.block);
                        } else if (mapping.type == GameRegistry.Type.ITEM) {
                            if (path.contains("bucket") && oil.bucket != null) mapping.remap(oil.bucket);
                            else if (path.contains("block")) mapping.ignore();
                        }
                        BCLog.logger.info("            " + path + " matched oil");
                    } else if (path.endsWith("fuel") && !Loader.isModLoaded("BuildCraft|Factory")) {
                        if (mapping.type == GameRegistry.Type.BLOCK) {
                            mapping.remap(fuel.block);
                        } else if (mapping.type == GameRegistry.Type.ITEM) {
                            if (path.contains("bucket") && fuel.bucket != null) mapping.remap(fuel.bucket);
                            else if (path.contains("block")) mapping.ignore();
                        }
                        BCLog.logger.info("            " + path + " matched fuel");
                    } else if (path.endsWith("redplasma") && redPlasma != null) {
                        if (mapping.type == GameRegistry.Type.BLOCK) {
                            mapping.remap(redPlasma.block);
                        } else if (mapping.type == GameRegistry.Type.ITEM) {
                            if (path.contains("bucket")) mapping.remap(redPlasma.bucket);
                            else if (path.contains("block")) mapping.ignore();
                        }
                        BCLog.logger.info("            " + path + " matched redplasma");
                    } else {
                        BCLog.logger.info("            " + path + " matched nothing");
                    }
                } else {
                    BCLog.logger.info("Unknown domain " + domain);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                error = t;
            }
        }
        if (error != null) throw Throwables.propagate(error);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelBakeEvent event) {
        FluidDefinition[] arr = { oil, fuel, redPlasma };
        if (Loader.isModLoaded("BuildCraft|Factory") && BuildCraftCore.DEVELOPER_MODE) {
            arr[0] = null;
            arr[1] = null;
        }
        if (!BuildCraftCore.DEVELOPER_MODE) {
            arr[2] = null;
        }
        for (FluidDefinition def : arr) {
            if (def == null) continue;
            IModel model = new ModelFluid(def.fluid);
            IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
            ModelResourceLocation loc = ModelHelper.getBlockResourceLocation(def.block);
            event.modelRegistry.putObject(loc, baked);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        FluidDefinition[] arr = { oil, fuel, redPlasma };
        if (Loader.isModLoaded("BuildCraft|Factory") && BuildCraftCore.DEVELOPER_MODE) {
            arr[0] = null;
            arr[1] = null;
        }
        if (!BuildCraftCore.DEVELOPER_MODE) {
            arr[2] = null;
        }
        for (FluidDefinition def : arr) {
            if (def == null) continue;
            int heat = def.fluid.getHeatValue();
            String from = "buildcraftenergy:blocks/fluids/heat_" + heat;
            SpriteColourMapper mapper = new SpriteColourMapper(def.fluid, from + "_still", true);
            event.map.setTextureEntry(def.fluid.getStill().toString(), mapper);

            mapper = new SpriteColourMapper(def.fluid, from + "_flow", false);
            event.map.setTextureEntry(def.fluid.getFlowing().toString(), mapper);
        }
    }
}
