/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeDefinition.IPipeCreator;
import buildcraft.api.transport.pipe.PipeDefinition.IPipeLoader;
import buildcraft.api.transport.pipe.PipeDefinition.PipeDefinitionBuilder;
import buildcraft.api.transport.pipe.PipeFlowType;
import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.behaviour.*;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class BCTransportPipes {
    public static PipeDefinition structure;

    public static PipeDefinition woodItem;
    public static PipeDefinition woodFluid;
    public static PipeDefinition woodPower;
    public static PipeDefinition woodRf;

    public static PipeDefinition stoneItem;
    public static PipeDefinition stoneFluid;
    public static PipeDefinition stonePower;
    public static PipeDefinition stoneRf;

    public static PipeDefinition cobbleItem;
    public static PipeDefinition cobbleFluid;
    public static PipeDefinition cobblePower;
    public static PipeDefinition cobbleRf;

    public static PipeDefinition quartzItem;
    public static PipeDefinition quartzFluid;
    public static PipeDefinition quartzPower;
    public static PipeDefinition quartzRf;

    public static PipeDefinition goldItem;
    public static PipeDefinition goldFluid;
    public static PipeDefinition goldPower;
    public static PipeDefinition goldRf;

    public static PipeDefinition sandstoneItem;
    public static PipeDefinition sandstoneFluid;
    public static PipeDefinition sandstonePower;
    public static PipeDefinition sandstoneRf;

    public static PipeDefinition ironItem;
    public static PipeDefinition ironFluid;
    public static PipeDefinition ironPower;
    public static PipeDefinition ironRf;

    public static PipeDefinition diamondItem;
    public static PipeDefinition diamondFluid;
    public static PipeDefinition diamondPower;
    public static PipeDefinition diamondRf;

    public static PipeDefinition diaWoodItem;
    public static PipeDefinition diaWoodFluid;
    public static PipeDefinition diaWoodPower;
    public static PipeDefinition diaWoodRf;

    public static PipeDefinition clayItem;
    public static PipeDefinition clayFluid;

    public static PipeDefinition voidItem;
    public static PipeDefinition voidFluid;

    public static PipeDefinition obsidianItem;
    public static PipeDefinition obsidianFluid;

    public static PipeDefinition lapisItem;
    public static PipeDefinition daizuliItem;
    public static PipeDefinition emzuliItem;
    public static PipeDefinition stripesItem;

    public static void preInit() {
        DefinitionBuilder builder = new DefinitionBuilder();

        builder.logic(PipeBehaviourStructure::new, PipeBehaviourStructure::new);
        builder.builder.enableBorderColouring();
        structure = builder.idTex("structure_cobblestone").flow(PipeApi.flowStructure).define();
        builder.builder.enableColouring();

        builder.logic(PipeBehaviourWood::new, PipeBehaviourWood::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        woodItem = builder.idTexPrefix("items_wood").flowItem().define();
        woodFluid = builder.idTexPrefix("fluids_wood").flowFluid().define();
        builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new);
        woodPower = builder.idTexPrefix("power_wood").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            woodRf = builder.idTexPrefix("rf_wood").flowRf().define();
        }
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourStone::new, PipeBehaviourStone::new);
        stoneItem = builder.idTex("items_stone").flowItem().define();
        stoneFluid = builder.idTex("fluids_stone").flowFluid().define();
        stonePower = builder.idTex("power_stone").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            stoneRf = builder.idTexPrefix("rf_stone").flowRf().define();
        }

        builder.logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new);
        cobbleItem = builder.idTex("items_cobblestone").flowItem().define();
        cobbleFluid = builder.idTex("fluids_cobblestone").flowFluid().define();
        cobblePower = builder.idTex("power_cobblestone").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            cobbleRf = builder.idTexPrefix("rf_cobblestone").flowRf().define();
        }

        builder.logic(PipeBehaviourQuartz::new, PipeBehaviourQuartz::new);
        quartzItem = builder.idTex("items_quartz").flowItem().define();
        quartzFluid = builder.idTex("fluids_quartz").flowFluid().define();
        quartzPower = builder.idTex("power_quartz").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            quartzRf = builder.idTex("rf_quartz").flowRf().define();
        }

        builder.logic(PipeBehaviourGold::new, PipeBehaviourGold::new);
        goldItem = builder.idTex("items_gold").flowItem().define();
        goldFluid = builder.idTex("fluids_gold").flowFluid().define();
        goldPower = builder.idTex("power_gold").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            goldRf = builder.idTex("rf_gold").flowRf().define();
        }

        builder.logic(PipeBehaviourSandstone::new, PipeBehaviourSandstone::new);
        sandstoneItem = builder.idTex("items_sandstone").flowItem().define();
        sandstoneFluid = builder.idTex("fluids_sandstone").flowFluid().define();
        sandstonePower = builder.idTex("power_sandstone").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            sandstoneRf = builder.idTex("rf_sandstone").flowRf().define();
        }

        builder.logic(PipeBehaviourIron::new, PipeBehaviourIron::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        ironItem = builder.idTexPrefix("items_iron").flowItem().define();
        ironFluid = builder.idTexPrefix("fluids_iron").flowFluid().define();
        builder.builder.itemTex(0);

        String[] diamondTextureSuffixes = new String[8];
        diamondTextureSuffixes[0] = "";
        diamondTextureSuffixes[7] = "_itemstack";
        for (Direction face : Direction.values()) {
            diamondTextureSuffixes[face.ordinal() + 1] = "_" + face.getName();
        }

        builder.logic(PipeBehaviourDiamondItem::new, PipeBehaviourDiamondItem::new).texSuffixes(diamondTextureSuffixes);
        builder.builder.itemTex(7);
        diamondItem = builder.idTexPrefix("items_diamond").flowItem().define();

        builder.logic(PipeBehaviourDiamondFluid::new, PipeBehaviourDiamondFluid::new);
        diamondFluid = builder.idTexPrefix("fluids_diamond").flowFluid().define();
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourLimiter::new, PipeBehaviourLimiter::new).flowPower();
        builder.texSuffixes("_m0", "_m4", "_m8", "_m16", "_m32", "_m64", "_m128");
        builder.builder.itemTex(6);
        ironPower = builder.idTexPrefix("power_iron").define();
        diamondPower = builder.idTexPrefix("power_diamond").define();
        if (!BCTransportConfig.disableRfPipe) {
            builder.flowRf();
            ironRf = builder.idTexPrefix("rf_iron").define();
            diamondRf = builder.idTexPrefix("rf_diamond").define();
        }
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourWoodDiamond::new, PipeBehaviourWoodDiamond::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        diaWoodItem = builder.idTexPrefix("items_diamond_wood").flowItem().define();
        diaWoodFluid = builder.idTexPrefix("fluids_diamond_wood").flowFluid().define();
        builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new);
        diaWoodPower = builder.idTexPrefix("power_diamond_wood").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            diaWoodRf = builder.idTexPrefix("rf_diamond_wood").flowRf().define();
        }
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourClay::new, PipeBehaviourClay::new);
        clayItem = builder.idTex("items_clay").flowItem().define();
        clayFluid = builder.idTex("fluids_clay").flowFluid().define();

        builder.logic(PipeBehaviourVoid::new, PipeBehaviourVoid::new);
        voidItem = builder.idTex("items_void").flowItem().define();
        voidFluid = builder.idTex("fluids_void").flowFluid().define();

        builder.logic(PipeBehaviourObsidian::new, PipeBehaviourObsidian::new);
        obsidianItem = builder.idTex("items_obsidian").flowItem().define();
        // obsidianFluid = builder.idTex("obsidian_fluids").flowFluid().define();

        DyeColor[] colourArray = DyeColor.values();
        String[] texSuffix = new String[16];
        for (int i = 0; i < 16; i++) {
            texSuffix[i] = "_" + colourArray[i].getName();
        }

        builder.logic(PipeBehaviourLapis::new, PipeBehaviourLapis::new).texSuffixes(texSuffix);
        lapisItem = builder.idTexPrefix("items_lapis").flowItem().define();

        String[] texSuffixPlus = Arrays.copyOf(texSuffix, 17);
        texSuffixPlus[16] = "_filled";

        builder.logic(PipeBehaviourDaizuli::new, PipeBehaviourDaizuli::new).texSuffixes(texSuffixPlus);
        builder.builder.itemTex(0, 0, 16);
        daizuliItem = builder.idTexPrefix("items_daizuli").flowItem().define();

        builder.logic(PipeBehaviourEmzuli::new, PipeBehaviourEmzuli::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        emzuliItem = builder.idTexPrefix("items_emzuli").flowItem().define();

        builder.builder.itemTex(0);
        builder.logic(PipeBehaviourStripes::new, PipeBehaviourStripes::new);
        stripesItem = builder.idTex("items_stripes").flowItem().define();
    }

    private static class DefinitionBuilder {
        public final PipeDefinitionBuilder builder = new PipeDefinitionBuilder();

        public DefinitionBuilder idTexPrefix(String both) {
            return id(both).texPrefix(both);
        }

        public DefinitionBuilder idTex(String both) {
            return id(both).tex(both);
        }

        public DefinitionBuilder id(String post) {
            builder.identifier = new ResourceLocation("buildcrafttransport", post);
            return this;
        }

        public DefinitionBuilder tex(String prefix, String... suffixes) {
            return texPrefix(prefix).texSuffixes(suffixes);
        }

        public DefinitionBuilder texPrefix(String prefix) {
            // if (BCTransportConfig.powerPipeUseOldMjTexture && prefix.endsWith("_power"))
            if (BCTransportConfig.powerPipeUseOldMjTexture && prefix.startsWith("power_")) {
                // prefix = prefix.substring(0, prefix.length() - "_power".length()) + "_rf";
                prefix = "rf_" + prefix.substring("power_".length(), prefix.length());
            }
            builder.texturePrefix = "buildcrafttransport:pipes/" + prefix;
            return this;
        }

        public DefinitionBuilder texSuffixes(String... suffixes) {
            if (suffixes.length == 0) {
                builder.textureSuffixes = new String[] { "" };
            } else {
                builder.textureSuffixes = suffixes;
            }
            return this;
        }

        public DefinitionBuilder logic(IPipeCreator creator, IPipeLoader loader) {
            builder.logicConstructor = creator;
            builder.logicLoader = loader;
            return this;
        }

        public DefinitionBuilder flowItem() {
            return flow(PipeApi.flowItems);
        }

        public DefinitionBuilder flowFluid() {
            return flow(PipeApi.flowFluids);
        }

        public DefinitionBuilder flowPower() {
            return flow(PipeApi.flowPower);
        }

        public DefinitionBuilder flowRf() {
            return flow(PipeApi.flowRf);
        }

        public DefinitionBuilder flow(PipeFlowType flow) {
            builder.flow(flow);
            return this;
        }

        public PipeDefinition define() {
            PipeDefinition def = new PipeDefinition(builder);
            PipeRegistry.INSTANCE.registerPipe(def);
            return def;
        }
    }
}
