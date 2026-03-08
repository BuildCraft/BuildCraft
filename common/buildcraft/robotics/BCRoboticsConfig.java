package buildcraft.robotics;

import buildcraft.api.BCModules;
import buildcraft.lib.config.BCConfig;
import buildcraft.lib.config.ConfigCategory;
import buildcraft.lib.config.Configuration;
import buildcraft.lib.config.EnumRestartRequirement;
import com.google.common.collect.Lists;

import java.util.List;

public class BCRoboticsConfig {
    private static Configuration config;

    public static final List<String> blacklistedRobots = Lists.newArrayList();

    private static ConfigCategory<List<String>> propBlacklistedRobots;

    public static void preInit() {
        BCModules module = BCModules.ROBOTICS;
        config = new Configuration(module);
        createProps();

        reloadConfig();
        BCConfig.registerReloadListener(module, BCRoboticsConfig::reloadConfig);
    }

    public static void createProps() {
        String display = "display";

        propBlacklistedRobots = config
                .defineList("worldgen.oil",
                        "Dimension ID's (e.g. 'minecraft:the_nether' for the nether,'minecraft:the_end' for the end) of dimensions that should never generate oil.",
                        EnumRestartRequirement.WORLD,
                        "excludedDimensions", List.of());
    }

    // public static void reloadConfig(EnumRestartRequirement restarted)
    public static void reloadConfig() {
        blacklistedRobots.addAll(propBlacklistedRobots.get());

        saveConfigs();
    }

    public static void saveConfigs() {
        if (config.hasChanged()) {
            config.save();
        }
    }
}
