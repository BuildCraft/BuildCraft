package ct.buildcraft.energy;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import ct.buildcraft.api.core.BCLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class BCEnergyConfig {
	
	public static ForgeConfigSpec config;

    public static boolean enableOilOceanBiome;
    public static boolean enableOilDesertBiome;

    public static boolean enableOilGeneration;
    public static double oilWellGenerationRate;
    public static boolean enableOilSpouts;
    public static boolean enableOilBurn;
    public static boolean oilIsSticky;

    public static int smallSpoutMinHeight;
    public static int smallSpoutMaxHeight;
    public static int largeSpoutMinHeight;
    public static int largeSpoutMaxHeight;

    public static double smallOilGenProb;
    public static double mediumOilGenProb;
    public static double largeOilGenProb;

//    public static IntArrayList excludedDimensions = new IntArrayList();
    /** If false then {@link #excludedDimensions} should be treated as a whitelist rather than a blacklist. */
    public static boolean excludedDimensionsIsBlackList;
    public static final Set<ResourceLocation> excessiveBiomes = new HashSet<>();
    public static final Set<ResourceLocation> excessiveVanillaBiomes = new HashSet<>();
    public static final Set<ResourceLocation> surfaceDepositBiomes = new HashSet<>();
    public static final Set<ResourceLocation> excludedBiomes = new HashSet<>();
    /** If false then {@link #excludedBiomes} should be treated as a whitelist rather than a blacklist. */
    public static boolean excludedBiomesIsBlackList;
    public static SpecialEventType christmasEventStatus = SpecialEventType.DAY_ONLY;

    private static BooleanValue propEnableOilOceanBiome;
    private static BooleanValue propEnableOilDesertBiome;

    private static BooleanValue propEnableOilGeneration;
    private static DoubleValue propOilWellGenerationRate;
    private static BooleanValue propEnableOilSpouts;
    private static BooleanValue propEnableOilBurn;
    private static BooleanValue propOilIsSticky;

    private static IntValue propSmallSpoutMinHeight;
    private static IntValue propSmallSpoutMaxHeight;
    private static IntValue propLargeSpoutMinHeight;
    private static IntValue propLargeSpoutMaxHeight;

    private static DoubleValue propSmallOilGenProb;
    private static DoubleValue propMediumOilGenProb;
    private static DoubleValue propLargeOilGenProb;

    private static ConfigValue<String> propExcessiveBiomes;
/*    private static ConfigValue<String[]> propSurfaceDepositBiomes;
    private static ConfigValue<String[]> propExcludedBiomes;*/
    private static BooleanValue propExcludedBiomesIsBlacklist;
//    private static ConfigValue<int[]> propExcludedDimensions;
    private static BooleanValue propExcludedDimensionsIsBlacklist;
//    private static int propChristmasEventType;

    

    public static void preInit() {
        String[] _excessive = { //
        		BCEnergy.MODID + ":oil_desert", //
                BCEnergy.MODID + ":oil_deep_ocean"
            };
        ForgeConfigSpec.Builder con_builder = new ForgeConfigSpec.Builder();

        con_builder.push("worldgen.oil");
        
        propEnableOilOceanBiome = con_builder.comment("Should Oil Ocean biomes generate at all?")
        	.define("oil_ocean_biome", true);
        
        propEnableOilDesertBiome = con_builder.comment("Should Oil Desert biomes generate at all?")
            	.define("oil_desert_biome", true);
/*con_builder.comment().define();*/
        propEnableOilGeneration = con_builder.comment("Should any oil sprouts or lakes be generated at all?")
        		.define("enable", true);
        
        propEnableOilBurn = con_builder.comment("Can oil blocks burn?")
            	.define("can_burn", true);
        
        propOilIsSticky = con_builder.comment("Should oil be dense and drag entities down?")
            	.define("oilIsDense", false);

        propOilWellGenerationRate = con_builder.comment("The rate of occurrence of oil wells.")
        		.defineInRange("generationRate", 1.0, 0, 100);
        
        propExcessiveBiomes = con_builder.comment("Biome registry names (e.g. 'minecraft:ocean','minecraft:plains')"
                + " of biomes that should have GREATLY increased oil generation rates.")
        		.define("excessiveBiomes", String.join(",", _excessive));//temp

/*        String[] _surface = {};
        propSurfaceDepositBiomes = con_builder.comment("Biome registry names (e.g. 'minecraft:ocean','minecraft:hills')"
                + " of biomes that should have slightly increased oil generation rates.")
        		.define("surfaceDepositBiomes", _surface);

        String[] _excluded = { "minecraft:hell", "minecraft:sky", };//TODO
        propExcludedBiomes = con_builder.comment("Biome registry names (e.g. 'minecraft:hell','minecraft:jungle') of biomes that should never generate oil.")
        		.define("excludedBiomes", _excluded);//temp*/

        propExcludedBiomesIsBlacklist = con_builder.comment("If true then the excluded biomes list will be treated as a blacklist, otherwise it will be treated as a whitelist.")
        		.define("excludedBiomesIsBlacklist", true);
        
/*        int[] _dims = { -1, 1 };
        propExcludedDimensions = con_builder.comment("Dimension ID's (e.g. '-1' for the nether,'1' for the end) of dimensions that should never generate oil.")
        		.define("excludedDimensions", _dims);//temp
*/
        propExcludedDimensionsIsBlacklist = con_builder.comment("If true then the excluded dimenstions list will be treated as a blacklist, otherwise it will be treated as a whitelist.")
        		.define("excludedDimensionsIsBlacklist", true);
        
        con_builder.pop();
        con_builder.push("worldgen.oil.spawn_probability");

        propSmallOilGenProb = con_builder.comment("The percentage probability of a small oil spawn")
        		.defineInRange("small", 2.0, 0, 100);
        
        propMediumOilGenProb = con_builder.comment("The percentage probability of a medium oil spawn")
        		.defineInRange("medium", 0.1, 0, 100);
        
        propLargeOilGenProb = con_builder.comment("The percentage probability of a large oil spawn")
        		.defineInRange("large", 0.04, 0, 100);
        
        con_builder.pop();
        con_builder.push("worldgen.oil.spouts");

        propEnableOilSpouts = con_builder.comment("Whether oil spouts are generated or not. The oil spring at the bottom of large lakes will still exist.")
			.define("enable", true);

        propSmallSpoutMinHeight = con_builder.comment("The minimum height for small oil spouts")
        		.defineInRange("small_min_height", 6, 0, 100);
        
        propSmallSpoutMaxHeight = con_builder.comment("The maximum height for small oil spouts")
        		.defineInRange("small_max_height", 12, 0, 100);

        propLargeSpoutMinHeight = con_builder.comment("The minimum height for large oil spouts")
        		.defineInRange("large_min_height", 10, 0, 100);
        
        propLargeSpoutMaxHeight = con_builder.comment("The maximum height for large oil spouts")
        		.defineInRange("large_max_height", 20, 0, 100);

        con_builder.pop();

/*        game.setTo(propEnableOilOceanBiome);
        game.setTo(propEnableOilDesertBiome);
        game.setTo(propEnableOilGeneration);
        game.setTo(propOilWellGenerationRate);
        game.setTo(propEnableOilBurn);
        game.setTo(propSmallOilGenProb);
        game.setTo(propMediumOilGenProb);
        game.setTo(propLargeOilGenProb);
        game.setTo(propEnableOilSpouts);
        game.setTo(propSmallSpoutMinHeight);
        game.setTo(propSmallSpoutMaxHeight);
        game.setTo(propLargeSpoutMinHeight);
        game.setTo(propLargeSpoutMaxHeight);




        world.setTo(propExcessiveBiomes);
        world.setTo(propSurfaceDepositBiomes);
        world.setTo(propExcludedBiomes);
        world.setTo(propExcludedBiomesIsBlacklist);
        world.setTo(propExcludedDimensions);
        world.setTo(propExcludedDimensionsIsBlacklist);*/

/*        propChristmasEventType =
            con_builder.comment().define();("events", "christmas_chocolate", SpecialEventType.DAY_ONLY.lowerCaseName);
        ConfigUtil.setEnumProperty(propChristmasEventType, SpecialEventType.values());
        game.setTo(propChristmasEventType);*/

        // Move old configs
        // boolean containes(String category, String key)
        // boolean moveProperty(String oldCategory, String propName, String newCategory);
        // boolean renameProperty(String category, String oldPropName, String newPropName)
/*        if (BCCoreConfig.config.containes("worldgen", "enableOilGen")) {
            BCCoreConfig.config.moveProperty("worldgen", "enableOilGen", "worldgen.oil");
            BCCoreConfig.config.renameProperty("worldgen.oil", "enableOilGen", "enable");
        }
        if (BCCoreConfig.config.containes("worldgen", "oilWellGenerationRate")) {
            BCCoreConfig.config.moveProperty("worldgen", "oilWellGenerationRate", "worldgen.oil");
            BCCoreConfig.config.renameProperty("worldgen.oil", "oilWellGenerationRate", "generationRate");
        }
        if (BCCoreConfig.config.containes("worldgen", "enableOilSpouts")) {
            BCCoreConfig.config.moveProperty("worldgen", "enableOilSpouts", "worldgen.oil.spouts");
            BCCoreConfig.config.renameProperty("worldgen.oil.spouts", "enableOilSpouts", "enable");
        }

        if (BCCoreConfig.config.containes("worldgen", "excessiveBiomes")) {
            BCCoreConfig.config.moveProperty("worldgen", "excessiveBiomes", "worldgen.oil");
        }
        if (BCCoreConfig.config.containes("worldgen", "surfaceDepositBiomes")) {
            BCCoreConfig.config.moveProperty("worldgen", "surfaceDepositBiomes", "worldgen.oil");
        }
        if (BCCoreConfig.config.containes("worldgen", "excludedBiomes")) {
            BCCoreConfig.config.moveProperty("worldgen", "excludedBiomes", "worldgen.oil");
        }
        if (BCCoreConfig.config.containes("worldgen", "excludedDimensions")) {
            BCCoreConfig.config.moveProperty("worldgen", "excludedDimensions", "worldgen.oil");
        }

        reloadConfig(EnumRestartRequirement.GAME);
        BCCoreConfig.addReloadListener(BCEnergyConfig::reloadConfig);*/
        config = con_builder.build();

        MinecraftForge.EVENT_BUS.register(BCEnergyConfig.class);
    }
    @SubscribeEvent
    public static void onReloadConfig(final ModConfigEvent.Reloading restarted) {
    	reloadConfig(restarted.getConfig().getModId());
    }
    
    @SubscribeEvent
    public static void onLoadConfig(final ModConfigEvent.Loading load) {
//    	validateBiomeNames();
    	reloadConfig(load.getConfig().getModId());
    }
    
    protected static void reloadConfig(String modid) {
        if (modid.equals(BCEnergy.MODID)) {

//            addBiomeNames(propExcludedBiomes, excludedBiomes);
            addBiomeNames(propExcessiveBiomes, excessiveBiomes);
//            addBiomeNames(propSurfaceDepositBiomes, surfaceDepositBiomes);*/
/*            excludedDimensions.clear();
            excludedDimensions = new IntArrayList(propExcludedDimensions.get());*/
        	//excessiveBiomes.add(BCEnergyWorldGen.OIL_DESERT_KEY);
            excessiveBiomes.forEach((name) ->{
            	excessiveVanillaBiomes.add(new ResourceLocation(name.getPath().substring(4)));//remove "oil_"
            });
        	
        	excludedBiomes.add(Biomes.NETHER_WASTES.location());
        	excludedBiomes.add(Biomes.SOUL_SAND_VALLEY.location());
        	excludedBiomes.add(Biomes.CRIMSON_FOREST.location());
        	excludedBiomes.add(Biomes.WARPED_FOREST.location());
        	excludedBiomes.add(Biomes.BASALT_DELTAS.location());//Nether
        	
            excludedBiomesIsBlackList = propExcludedBiomesIsBlacklist.get();
            excludedDimensionsIsBlackList = propExcludedDimensionsIsBlacklist.get();

            enableOilOceanBiome = propEnableOilOceanBiome.get();
            enableOilDesertBiome = propEnableOilDesertBiome.get();

            enableOilGeneration = propEnableOilGeneration.get();
            oilWellGenerationRate = propOilWellGenerationRate.get();
            enableOilSpouts = propEnableOilSpouts.get();
            enableOilBurn = propEnableOilBurn.get();
            oilIsSticky = propOilIsSticky.get();

            smallSpoutMinHeight = propSmallSpoutMinHeight.get();
            smallSpoutMaxHeight = propSmallSpoutMaxHeight.get();
            largeSpoutMinHeight = propLargeSpoutMinHeight.get();
            largeSpoutMaxHeight = propLargeSpoutMaxHeight.get();

            smallOilGenProb = propSmallOilGenProb.get() / 100;
            mediumOilGenProb = propMediumOilGenProb.get() / 100;
            largeOilGenProb = propLargeOilGenProb.get() / 100;

          //christmasEventStatus = ConfigUtil.parseEnumForConfig(propChristmasEventType, SpecialEventType.DAY_ONLY);
        }
    }

    private static void addBiomeNames(ConfigValue<String> propExcessiveBiomes2, Set<ResourceLocation> excessivebiomes2) {
        excessivebiomes2.clear();
        for (String s : propExcessiveBiomes2.get().split(",")) {
            excessivebiomes2.add(new ResourceLocation(s));
        }
    }

    /** Called in post-init, after all biomes should have been registered. In 1.12 this should be called after the
     * registry event for biomes has been fired. */
    public static void validateBiomeNames() {
        Set<ResourceLocation> invalids = new HashSet<>();
        addInvalidBiomeNames(excessiveBiomes, invalids);
        addInvalidBiomeNames(excludedBiomes, invalids);
        addInvalidBiomeNames(surfaceDepositBiomes, invalids);

        if (invalids.isEmpty()) {
            return;
        }

        List<ResourceLocation> invalidList = new ArrayList<>();
        invalidList.addAll(invalids);
        Collections.sort(invalidList, Comparator.comparing(ResourceLocation::toString));

        List<ResourceLocation> allValid = new ArrayList<>();
//        allValid.addAll(ForgeRegistries.BIOMES.getKeys());//temp
        Collections.sort(allValid, Comparator.comparing(ResourceLocation::toString));

        BCLog.logger.warn("****************************************************");
        BCLog.logger.warn("*");
        BCLog.logger.warn("* Unknown biome name detected in buildcraft config!");
//        BCLog.logger.warn("* (Config file = " + config.getSpec() + ")");
        BCLog.logger.warn("*");
        BCLog.logger.warn("* Unknown biomes: ");
        printList(Level.WARNING, invalidList);
        BCLog.logger.warn("*");
        BCLog.logger.info("* All possible known names: ");
        printList(Level.INFO, allValid);
        BCLog.logger.info("*");
        BCLog.logger.warn("****************************************************");
    }

    private static void printList(Level level, List<ResourceLocation> list) {
        for (ResourceLocation location : list) {
            BCLog.logger.info("*    - " + location);
        }
    }

    private static void addInvalidBiomeNames(Set<ResourceLocation> toTest, Set<ResourceLocation> invalidDest) {
        for (ResourceLocation test : toTest) {
            if (!ForgeRegistries.BIOMES.containsKey(test)) {//temp
                invalidDest.add(test);
            }
        }
    }

    public enum SpecialEventType {
        DISABLED,
        DAY_ONLY,
        MONTH,
        ENABLED;

        public final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        public boolean isEnabled(MonthDay date) {
            if (this == DISABLED) {
                return false;
            }
            if (this == ENABLED) {
                return true;
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.getMonth() != date.getMonth()) {
                return false;
            }
            if (this == MONTH) {
                return true;
            }
            int thisDay = now.getDayOfMonth();
            int wantedDay = date.getDayOfMonth();
            return thisDay >= wantedDay - 1 && thisDay <= wantedDay + 1;
        }
    }

    public enum excessiveBiomes{
    	DESERT(BCEnergy.MODID + ":oil_desert"),
    	OCEAN(BCEnergy.MODID + ":oil_ocean");
    	public final String name ;
    	excessiveBiomes(String name){
    		this.name = name;
    	}
    }
}
