/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.api.BCModules;
import buildcraft.lib.config.BCConfig;
import buildcraft.lib.config.ConfigCategory;
import buildcraft.lib.config.Configuration;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.silicon.item.ItemPluggableFacade;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.stream.Collectors;

public class BCSiliconConfig {
    private static Configuration config;

    public static boolean renderLaserBeams = true;
    // Calen
    public static boolean differStatesOfNoteBlockForFacade = false;
    private static EnumFacadeFilterType facadeBlockNamespaceFilterType = EnumFacadeFilterType.BLACK_LIST;
    private static EnumFacadeFilterType facadeBlockIdFilterType = EnumFacadeFilterType.BLACK_LIST;
    private static List<String> facadeBlockNamespaceFilter = List.of();
    private static List<ResourceLocation> facadeBlockIdFilter = List.of();
    public static boolean facadesNotInCreativeModTabCanBeCrafted = true;

    private static ConfigCategory<Boolean> propRenderLaserBeams;
    // Calen
    private static ConfigCategory<Boolean> propDifferStatesOfNoteBlockForFacade;
    private static ConfigCategory<EnumFacadeFilterType> propFacadeBlockNamespaceFilterType;
    private static ConfigCategory<EnumFacadeFilterType> propFacadeBlockIdFilterType;
    private static ConfigCategory<List<String>> propFacadeBlockNamespaceFilter;
    private static ConfigCategory<List<String>> propFacadeBlockIdFilter;
    private static ConfigCategory<Boolean> propFacadesNotInCreativeModTabCanBeCrafted;

    public static void preInit() {
//        Configuration config = BCCoreConfig.config;
        BCModules module = BCModules.SILICON;
        config = new Configuration(module);
        createProps();

//        reloadConfig(EnumRestartRequirement.NONE);
        reloadConfig();
//        MinecraftForge.EVENT_BUS.register(BCSiliconConfig.class);
        BCConfig.registerReloadListener(module, BCSiliconConfig::reloadConfig);
    }

    public static void createProps() {
        String display = "display";

        propRenderLaserBeams = config
                .define(display,
                        "When false laser beams will not be visible while transmitting power without wearing Goggles",
                        EnumRestartRequirement.NONE,
                        "renderLaserBeams", true);

        // Calen
        propDifferStatesOfNoteBlockForFacade = config
                .define(display,
                        "If different textures in resource packs are used for different instruments and notes, or whether powered, please set this [true]",
                        EnumRestartRequirement.WORLD,
                        "differStatesOfNoteBlockForFacade", false);

        propFacadeBlockNamespaceFilterType = config
                .defineEnum(display,
                        "The type of facadeBlockNamespaceFilter",
                        EnumRestartRequirement.WORLD,
                        "facadeBlockNamespaceFilterType", EnumFacadeFilterType.BLACK_LIST);
        propFacadeBlockIdFilterType = config
                .defineEnum(display,
                        "The type of facadeBlockIdFilter",
                        EnumRestartRequirement.WORLD,
                        "facadeBlockIdFilterType", EnumFacadeFilterType.BLACK_LIST);
        propFacadeBlockNamespaceFilter = config
                .defineList(display,
                        "The block namespace filter for blocks to create facades to display in CreativeModTab. Namespace filter has lower priority than id filter. [Warn: blocks allowed here may be disallowed by in-code rules.] Example value: minecraft",
                        EnumRestartRequirement.WORLD,
                        "facadeBlockNamespaceFilter", List.of());
        propFacadeBlockIdFilter = config
                .defineList(display,
                        "The block id(ResourceLocation) filter for blocks to create facades to display in CreativeModTab. Id filter has higher priority than namespace filter. [Warn: blocks allowed here may be disallowed by in-code rules.] [stone, oak_planks and oak_log are forced enabled.] Example value: minecraft:dirt",
                        EnumRestartRequirement.WORLD,
                        "facadeBlockIdFilter", List.of());

        propFacadesNotInCreativeModTabCanBeCrafted = config
                .define(display,
                        "Whether facades not in the facade tab can be crafted by facade assembly recipes",
                        EnumRestartRequirement.WORLD,
                        "facadesNotInCreativeModTabCanBeCrafted", true);
    }

    // public static void reloadConfig(EnumRestartRequirement restarted)
    public static void reloadConfig() {
        renderLaserBeams = propRenderLaserBeams.get();
        differStatesOfNoteBlockForFacade = propDifferStatesOfNoteBlockForFacade.get();

        facadeBlockIdFilterType = propFacadeBlockIdFilterType.get();
        facadeBlockNamespaceFilterType = propFacadeBlockNamespaceFilterType.get();
        facadeBlockIdFilter = propFacadeBlockIdFilter.get().stream().map(ResourceLocation::tryParse).collect(Collectors.toList());
        facadeBlockNamespaceFilter = propFacadeBlockNamespaceFilter.get();
        facadesNotInCreativeModTabCanBeCrafted = propFacadesNotInCreativeModTabCanBeCrafted.get();

        saveConfigs();
    }

    public static void saveConfigs() {
        if (config.hasChanged()) {
            config.save();
        }
    }

//    @SubscribeEvent
//    public static void onConfigChange(OnConfigChangedEvent cce) {
//        if (BCModules.isBcMod(cce.getModID())) {
//            reloadConfig(EnumRestartRequirement.NONE);
//        }
//    }

    // Calen
    private enum EnumFacadeFilterType {
        BLACK_LIST,
        WHITE_LIST,
        ;
    }

    /** Forced in {@link ItemPluggableFacade#addSubItems(CreativeModeTab, NonNullList)} */
    private static final List<Block> FORCED_BLOCKS = Lists.newArrayList(Blocks.STONE, Blocks.OAK_PLANKS, Blocks.OAK_LOG);

    public static boolean isFacadeBlockIdAllowedInCreativeModTabByConfig(Block block) {
        if (FORCED_BLOCKS.contains(block)) {
            return true;
        }
        ResourceLocation id = block.getRegistryName();
        // id has higher priority
        switch (facadeBlockIdFilterType) {
            case BLACK_LIST:
                if (facadeBlockIdFilter.contains(id)) {
                    return false;
                }
                break;
            case WHITE_LIST:
                if (facadeBlockIdFilter.contains(id)) {
                    return true;
                }
                break;
        }
        // namespace has lower priority
        switch (facadeBlockNamespaceFilterType) {
            case BLACK_LIST:
                return !facadeBlockNamespaceFilter.contains(id.getNamespace());
            case WHITE_LIST:
                return facadeBlockNamespaceFilter.contains(id.getNamespace());
        }
        throw new IllegalArgumentException(
                "Unexpected facade block filter condition provided by config. " +
                        "[facadeBlockIdFilterType=" + facadeBlockIdFilterType + "] " +
                        "[facadeBlockIdFilter=" + facadeBlockIdFilter + "] " +
                        "[facadeBlockNamespaceFilterType=" + facadeBlockNamespaceFilterType + "] " +
                        "[facadeBlockNamespaceFilter=" + facadeBlockNamespaceFilter + "] " +
                        "This exception seems impossible."
        );
    }
}
