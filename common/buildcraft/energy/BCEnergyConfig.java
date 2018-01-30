package buildcraft.energy;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.misc.ConfigUtil;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.*;

public class BCEnergyConfig {

    public static boolean enableOilGeneration;
    public static final TIntSet excludedDimensions = new TIntHashSet();
    public static final Set<ResourceLocation> excessiveBiomes = new HashSet<>();
    public static final Set<ResourceLocation> surfaceDepositBiomes = new HashSet<>();
    public static final Set<ResourceLocation> excludedBiomes = new HashSet<>();
    public static SpecialEventType christmasEventStatus = SpecialEventType.DAY_ONLY;

    private static Property propEnableOilGeneration;
    private static Property propExcessiveBiomes;
    private static Property propSurfaceDepositBiomes;
    private static Property propExcludedBiomes;
    private static Property propExcludedDimensions;
    private static Property propChristmasEventType;

    public static void preInit() {
        EnumRestartRequirement world = EnumRestartRequirement.WORLD;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propEnableOilGeneration = BCCoreConfig.config.get("worldgen", "enableOilGen", true);
        propEnableOilGeneration.setComment("Should any oil sprouts or lakes generate, at all?");
        game.setTo(propEnableOilGeneration);

        String[] _excessive = { //
            BCEnergy.MODID + ":oil_desert", //
            BCEnergy.MODID + ":oil_ocean", //
        };
        propExcessiveBiomes = BCCoreConfig.config.get("worldgen", "excessiveBiomes", _excessive);
        propExcessiveBiomes.setComment("Biome registry names (e.g. 'minecraft:ocean','minecraft:plains')"
            + " of biomes that should have GREATLY increased oil generation rates.");
        world.setTo(propExcessiveBiomes);

        String[] _surface = {};
        propSurfaceDepositBiomes = BCCoreConfig.config.get("worldgen", "surfaceDepositBiomes", _surface);
        propSurfaceDepositBiomes.setComment("Biome registry names (e.g. 'minecraft:ocean','minecraft:hills')"
            + " of biomes that should have slightly increased oil generation rates.");
        world.setTo(propSurfaceDepositBiomes);

        String[] _excluded = { //
            "minecraft:hell", //
            "minecraft:sky",//
        };
        propExcludedBiomes = BCCoreConfig.config.get("worldgen", "excludedBiomes", _excluded);
        propExcludedBiomes.setComment("Biome registry names (e.g. 'minecraft:hell','minecraft:jungle')"
            + " of biomes that should never generate oil.");
        world.setTo(propExcludedBiomes);

        int[] _dims = { -1, 1 };
        propExcludedDimensions = BCCoreConfig.config.get("worldgen", "excludedDimensions", _dims);
        propExcludedDimensions.setComment("Dimension ID's (e.g. '-1' for the nether,'1' for the end)"
            + " of dimensions that should never generate oil.");
        world.setTo(propExcludedDimensions);

        propChristmasEventType =
            BCCoreConfig.config.get("events", "christmas_chocolate", SpecialEventType.DAY_ONLY.lowerCaseName);
        ConfigUtil.setEnumProperty(propChristmasEventType, SpecialEventType.values());
        game.setTo(propChristmasEventType);

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

            if (EnumRestartRequirement.GAME.hasBeenRestarted(restarted)) {
                enableOilGeneration = propEnableOilGeneration.getBoolean();
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

        List<ResourceLocation> invalidList = new ArrayList<>(invalids);
        invalidList.sort(Comparator.comparing(ResourceLocation::toString));

        //TODO Fix me, causes weird abstract set error
        //List<ResourceLocation> allValid = new ArrayList<>(ForgeRegistries.BIOMES.getKeys());
        //allValid.sort(Comparator.comparing(ResourceLocation::toString));

        BCLog.logger.warn("****************************************************");
        BCLog.logger.warn("*");
        BCLog.logger.warn("* Unknown biome name detected in buildcraft config!");
        BCLog.logger.warn("* (Config file = " + BCCoreConfig.config.getConfigFile().getAbsolutePath() + ")");
        BCLog.logger.warn("*");
        BCLog.logger.warn("* Unknown biomes: ");
        printList(Level.WARN, invalidList);
        BCLog.logger.warn("*");
        //BCLog.logger.info("* All possible known names: ");
        //printList(Level.INFO, allValid);
        //BCLog.logger.info("*");
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
