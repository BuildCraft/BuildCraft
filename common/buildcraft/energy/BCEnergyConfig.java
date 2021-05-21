package buildcraft.energy;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.apache.logging.log4j.Level;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.core.BCLog;

import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.misc.ConfigUtil;

import buildcraft.core.BCCoreConfig;

public class BCEnergyConfig {

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

    public static final TIntSet excludedDimensions = new TIntHashSet();
    /** If false then {@link #excludedDimensions} should be treated as a whitelist rather than a blacklist. */
    public static boolean excludedDimensionsIsBlackList;
    public static final Set<ResourceLocation> excessiveBiomes = new HashSet<>();
    public static final Set<ResourceLocation> surfaceDepositBiomes = new HashSet<>();
    public static final Set<ResourceLocation> excludedBiomes = new HashSet<>();
    /** If false then {@link #excludedBiomes} should be treated as a whitelist rather than a blacklist. */
    public static boolean excludedBiomesIsBlackList;
    public static SpecialEventType christmasEventStatus = SpecialEventType.DAY_ONLY;

    private static Property propEnableOilOceanBiome;
    private static Property propEnableOilDesertBiome;

    private static Property propEnableOilGeneration;
    private static Property propOilWellGenerationRate;
    private static Property propEnableOilSpouts;
    private static Property propEnableOilBurn;
    private static Property propOilIsSticky;

    private static Property propSmallSpoutMinHeight;
    private static Property propSmallSpoutMaxHeight;
    private static Property propLargeSpoutMinHeight;
    private static Property propLargeSpoutMaxHeight;

    private static Property propSmallOilGenProb;
    private static Property propMediumOilGenProb;
    private static Property propLargeOilGenProb;

    private static Property propExcessiveBiomes;
    private static Property propSurfaceDepositBiomes;
    private static Property propExcludedBiomes;
    private static Property propExcludedBiomesIsBlacklist;
    private static Property propExcludedDimensions;
    private static Property propExcludedDimensionsIsBlacklist;
    private static Property propChristmasEventType;

    public static void preInit() {
        EnumRestartRequirement world = EnumRestartRequirement.WORLD;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propEnableOilOceanBiome = BCCoreConfig.config.get("worldgen.oil", "oil_ocean_biome", true,
            "Should Oil Ocean biomes generate at all?");
        propEnableOilDesertBiome = BCCoreConfig.config.get("worldgen.oil", "oil_desert_biome", true,
            "Should Oil Desert biomes generate at all?");

        propEnableOilGeneration = BCCoreConfig.config.get("worldgen.oil", "enable", true,
            "Should any oil sprouts or lakes be generated at all?");
        propEnableOilBurn = BCCoreConfig.config.get("worldgen.oil", "can_burn", true, "Can oil blocks burn?");
        propOilIsSticky = BCCoreConfig.config.get("general","oilIsDense", false, "Should oil be dense and drag entities down?");

        propOilWellGenerationRate =
            BCCoreConfig.config.get("worldgen.oil", "generationRate", 1.0, "The rate of occurrence of oil wells.");

        propSmallOilGenProb = BCCoreConfig.config.get("worldgen.oil.spawn_probability", "small", 2.0,
            "The percentage probability of a small oil spawn");
        propMediumOilGenProb = BCCoreConfig.config.get("worldgen.oil.spawn_probability", "medium", 0.1,
            "The percentage probability of a medium oil spawn");
        propLargeOilGenProb = BCCoreConfig.config.get("worldgen.oil.spawn_probability", "large", 0.04,
            "The percentage probability of a large oil spawn");

        propEnableOilSpouts = BCCoreConfig.config.get("worldgen.oil.spouts", "enable", true,
            "Whether oil spouts are generated or not. The oil spring at the bottom of large lakes will still exist.");

        propSmallSpoutMinHeight = BCCoreConfig.config.get("worldgen.oil.spouts", "small_min_height", 6,
            "The minimum height for small oil spouts");
        propSmallSpoutMaxHeight = BCCoreConfig.config.get("worldgen.oil.spouts", "small_max_height", 12,
            "The maximum height for small oil spouts");

        propLargeSpoutMinHeight = BCCoreConfig.config.get("worldgen.oil.spouts", "large_min_height", 10,
            "The minimum height for large oil spouts");
        propLargeSpoutMaxHeight = BCCoreConfig.config.get("worldgen.oil.spouts", "large_max_height", 20,
            "The maximum height for large oil spouts");

        game.setTo(propEnableOilOceanBiome);
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

        String[] _excessive = { //
            BCEnergy.MODID + ":oil_desert", //
            BCEnergy.MODID + ":oil_ocean", //
        };
        propExcessiveBiomes = BCCoreConfig.config.get("worldgen.oil", "excessiveBiomes", _excessive,
            "Biome registry names (e.g. 'minecraft:ocean','minecraft:plains')"
                + " of biomes that should have GREATLY increased oil generation rates.");

        String[] _surface = {};
        propSurfaceDepositBiomes = BCCoreConfig.config.get("worldgen.oil", "surfaceDepositBiomes", _surface,
            "Biome registry names (e.g. 'minecraft:ocean','minecraft:hills')"
                + " of biomes that should have slightly increased oil generation rates.");

        String[] _excluded = { "minecraft:hell", "minecraft:sky", };
        propExcludedBiomes = BCCoreConfig.config.get("worldgen.oil", "excludedBiomes", _excluded,
            "Biome registry names (e.g. 'minecraft:hell','minecraft:jungle') of biomes that should never generate oil.");

        propExcludedBiomesIsBlacklist = BCCoreConfig.config.get("worldgen.oil", "excludedBiomesIsBlacklist", true,
            "If true then the excluded biomes list will be treated as a blacklist, otherwise it will be treated as a whitelist.");

        int[] _dims = { -1, 1 };
        propExcludedDimensions = BCCoreConfig.config.get("worldgen.oil", "excludedDimensions", _dims,
            "Dimension ID's (e.g. '-1' for the nether,'1' for the end) of dimensions that should never generate oil.");

        propExcludedDimensionsIsBlacklist = BCCoreConfig.config.get("worldgen.oil", "excludedDimensionsIsBlacklist", true,
            "If true then the excluded dimenstions list will be treated as a blacklist, otherwise it will be treated as a whitelist.");

        world.setTo(propExcessiveBiomes);
        world.setTo(propSurfaceDepositBiomes);
        world.setTo(propExcludedBiomes);
        world.setTo(propExcludedBiomesIsBlacklist);
        world.setTo(propExcludedDimensions);
        world.setTo(propExcludedDimensionsIsBlacklist);

        propChristmasEventType =
            BCCoreConfig.config.get("events", "christmas_chocolate", SpecialEventType.DAY_ONLY.lowerCaseName);
        ConfigUtil.setEnumProperty(propChristmasEventType, SpecialEventType.values());
        game.setTo(propChristmasEventType);

        // Move old configs
        // boolean hasKey(String category, String key)
        // boolean moveProperty(String oldCategory, String propName, String newCategory);
        // boolean renameProperty(String category, String oldPropName, String newPropName)
        if (BCCoreConfig.config.hasKey("worldgen", "enableOilGen")) {
            BCCoreConfig.config.moveProperty("worldgen", "enableOilGen", "worldgen.oil");
            BCCoreConfig.config.renameProperty("worldgen.oil", "enableOilGen", "enable");
        }
        if (BCCoreConfig.config.hasKey("worldgen", "oilWellGenerationRate")) {
            BCCoreConfig.config.moveProperty("worldgen", "oilWellGenerationRate", "worldgen.oil");
            BCCoreConfig.config.renameProperty("worldgen.oil", "oilWellGenerationRate", "generationRate");
        }
        if (BCCoreConfig.config.hasKey("worldgen", "enableOilSpouts")) {
            BCCoreConfig.config.moveProperty("worldgen", "enableOilSpouts", "worldgen.oil.spouts");
            BCCoreConfig.config.renameProperty("worldgen.oil.spouts", "enableOilSpouts", "enable");
        }

        if (BCCoreConfig.config.hasKey("worldgen", "excessiveBiomes")) {
            BCCoreConfig.config.moveProperty("worldgen", "excessiveBiomes", "worldgen.oil");
        }
        if (BCCoreConfig.config.hasKey("worldgen", "surfaceDepositBiomes")) {
            BCCoreConfig.config.moveProperty("worldgen", "surfaceDepositBiomes", "worldgen.oil");
        }
        if (BCCoreConfig.config.hasKey("worldgen", "excludedBiomes")) {
            BCCoreConfig.config.moveProperty("worldgen", "excludedBiomes", "worldgen.oil");
        }
        if (BCCoreConfig.config.hasKey("worldgen", "excludedDimensions")) {
            BCCoreConfig.config.moveProperty("worldgen", "excludedDimensions", "worldgen.oil");
        }

        reloadConfig(EnumRestartRequirement.GAME);
        BCCoreConfig.addReloadListener(BCEnergyConfig::reloadConfig);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {

            addBiomeNames(propExcludedBiomes, excludedBiomes);
            addBiomeNames(propExcessiveBiomes, excessiveBiomes);
            addBiomeNames(propSurfaceDepositBiomes, surfaceDepositBiomes);
            excludedDimensions.clear();
            excludedDimensions.addAll(propExcludedDimensions.getIntList());
            excludedBiomesIsBlackList = propExcludedBiomesIsBlacklist.getBoolean();
            excludedDimensionsIsBlackList = propExcludedDimensionsIsBlacklist.getBoolean();

            if (EnumRestartRequirement.GAME.hasBeenRestarted(restarted)) {
                enableOilOceanBiome = propEnableOilOceanBiome.getBoolean();
                enableOilDesertBiome = propEnableOilDesertBiome.getBoolean();

                enableOilGeneration = propEnableOilGeneration.getBoolean();
                oilWellGenerationRate = propOilWellGenerationRate.getDouble();
                enableOilSpouts = propEnableOilSpouts.getBoolean();
                enableOilBurn = propEnableOilBurn.getBoolean();
                oilIsSticky = propOilIsSticky.getBoolean();

                smallSpoutMinHeight = propSmallSpoutMinHeight.getInt();
                smallSpoutMaxHeight = propSmallSpoutMaxHeight.getInt();
                largeSpoutMinHeight = propLargeSpoutMinHeight.getInt();
                largeSpoutMaxHeight = propLargeSpoutMaxHeight.getInt();

                smallOilGenProb = propSmallOilGenProb.getDouble() / 100;
                mediumOilGenProb = propMediumOilGenProb.getDouble() / 100;
                largeOilGenProb = propLargeOilGenProb.getDouble() / 100;

                christmasEventStatus = ConfigUtil.parseEnumForConfig(propChristmasEventType, SpecialEventType.DAY_ONLY);
            } else {
                validateBiomeNames();
            }
        }
    }

    private static void addBiomeNames(Property prop, Set<ResourceLocation> set) {
        set.clear();
        for (String s : prop.getStringList()) {
            set.add(new ResourceLocation(s));
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
        allValid.addAll(ForgeRegistries.BIOMES.getKeys());
        Collections.sort(allValid, Comparator.comparing(ResourceLocation::toString));

        BCLog.logger.warn("****************************************************");
        BCLog.logger.warn("*");
        BCLog.logger.warn("* Unknown biome name detected in buildcraft config!");
        BCLog.logger.warn("* (Config file = " + BCCoreConfig.config.getConfigFile().getAbsolutePath() + ")");
        BCLog.logger.warn("*");
        BCLog.logger.warn("* Unknown biomes: ");
        printList(Level.WARN, invalidList);
        BCLog.logger.warn("*");
        BCLog.logger.info("* All possible known names: ");
        printList(Level.INFO, allValid);
        BCLog.logger.info("*");
        BCLog.logger.warn("****************************************************");
    }

    private static void printList(Level level, List<ResourceLocation> list) {
        for (ResourceLocation location : list) {
            BCLog.logger.log(level, "*    - " + location);
        }
    }

    private static void addInvalidBiomeNames(Set<ResourceLocation> toTest, Set<ResourceLocation> invalidDest) {
        for (ResourceLocation test : toTest) {
            if (!ForgeRegistries.BIOMES.containsKey(test)) {
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
}
