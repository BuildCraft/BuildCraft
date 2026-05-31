/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import java.util.Arrays;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeDefinition.IPipeCreator;
import buildcraft.api.transport.pipe.PipeDefinition.IPipeLoader;
import buildcraft.api.transport.pipe.PipeDefinition.PipeDefinitionBuilder;
import buildcraft.api.transport.pipe.PipeFlowType;

import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.behaviour.PipeBehaviourClay;
import buildcraft.transport.pipe.behaviour.PipeBehaviourCobble;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDaizuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamondFluid;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamondItem;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourGold;
import buildcraft.transport.pipe.behaviour.PipeBehaviourIron;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLapis;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;
import buildcraft.transport.pipe.behaviour.PipeBehaviourObsidian;
import buildcraft.transport.pipe.behaviour.PipeBehaviourQuartz;
import buildcraft.transport.pipe.behaviour.PipeBehaviourSandstone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStructure;
import buildcraft.transport.pipe.behaviour.PipeBehaviourVoid;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWood;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodPower;

public class BCTransportPipes {
    public static PipeDefinition structure;
    public static PipeDefinition woodItem, woodFluid, woodPower, woodRf;
    public static PipeDefinition stoneItem, stoneFluid, stonePower, stoneRf;
    public static PipeDefinition cobbleItem, cobbleFluid, cobblePower, cobbleRf;
    public static PipeDefinition quartzItem, quartzFluid, quartzPower, quartzRf;
    public static PipeDefinition goldItem, goldFluid, goldPower, goldRf;
    public static PipeDefinition sandstoneItem, sandstoneFluid, sandstonePower, sandstoneRf;
    public static PipeDefinition ironItem, ironFluid, ironPower, ironRf;
    public static PipeDefinition diamondItem, diamondFluid, diamondPower, diamondRf;
    public static PipeDefinition diaWoodItem, diaWoodFluid, diaWoodPower, diaWoodRf;
    public static PipeDefinition clayItem, clayFluid;
    public static PipeDefinition voidItem, voidFluid;
    public static PipeDefinition obsidianItem, obsidianFluid;
    public static PipeDefinition lapisItem, daizuliItem, emzuliItem, stripesItem;

    public static void preInit() {
        DefinitionBuilder builder = new DefinitionBuilder();

        builder.logic(PipeBehaviourStructure::new, PipeBehaviourStructure::new);
        builder.builder.enableBorderColouring();
        structure = builder.idTex("structure").flow(PipeApi.flowStructure).define();
        builder.builder.enableColouring();

        builder.logic(PipeBehaviourWood::new, PipeBehaviourWood::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        woodItem = builder.idTexPrefix("wood_item").flowItem().define();
        woodFluid = builder.idTexPrefix("wood_fluid").flowFluid().define();
        builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new);
        woodPower = builder.idTexPrefix("wood_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            woodRf = builder.idTexPrefix("wood_rf").flowRf().define();
        }
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourStone::new, PipeBehaviourStone::new);
        stoneItem = builder.idTex("stone_item").flowItem().define();
        stoneFluid = builder.idTex("stone_fluid").flowFluid().define();
        stonePower = builder.idTex("stone_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            stoneRf = builder.idTexPrefix("stone_rf").flowRf().define();
        }

        builder.logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new);
        cobbleItem = builder.idTex("cobblestone_item").flowItem().define();
        cobbleFluid = builder.idTex("cobblestone_fluid").flowFluid().define();
        cobblePower = builder.idTex("cobblestone_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            cobbleRf = builder.idTexPrefix("cobblestone_rf").flowRf().define();
        }

        builder.logic(PipeBehaviourQuartz::new, PipeBehaviourQuartz::new);
        quartzItem = builder.idTex("quartz_item").flowItem().define();
        quartzFluid = builder.idTex("quartz_fluid").flowFluid().define();
        quartzPower = builder.idTex("quartz_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            quartzRf = builder.idTex("quartz_rf").flowRf().define();
        }

        builder.logic(PipeBehaviourGold::new, PipeBehaviourGold::new);
        goldItem = builder.idTex("gold_item").flowItem().define();
        goldFluid = builder.idTex("gold_fluid").flowFluid().define();
        goldPower = builder.idTex("gold_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            goldRf = builder.idTex("gold_rf").flowRf().define();
        }

        builder.logic(PipeBehaviourSandstone::new, PipeBehaviourSandstone::new);
        sandstoneItem = builder.idTex("sandstone_item").flowItem().define();
        sandstoneFluid = builder.idTex("sandstone_fluid").flowFluid().define();
        sandstonePower = builder.idTex("sandstone_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            sandstoneRf = builder.idTexPrefix("sandstone_rf").flowRf().define();
        }

        builder.logic(PipeBehaviourIron::new, PipeBehaviourIron::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        ironItem = builder.idTexPrefix("iron_item").flowItem().define();
        ironFluid = builder.idTexPrefix("iron_fluid").flowFluid().define();
        builder.builder.itemTex(0);

        String[] diamondTextureSuffixes = new String[8];
        diamondTextureSuffixes[0] = "";
        diamondTextureSuffixes[7] = "_itemstack";
        for (Direction face : Direction.values()) {
            diamondTextureSuffixes[face.ordinal() + 1] = "_" + face.getName();
        }
        builder.logic(PipeBehaviourDiamondItem::new, PipeBehaviourDiamondItem::new).texSuffixes(diamondTextureSuffixes);
        builder.builder.itemTex(7);
        diamondItem = builder.idTexPrefix("diamond_item").flowItem().define();
        builder.logic(PipeBehaviourDiamondFluid::new, PipeBehaviourDiamondFluid::new);
        diamondFluid = builder.idTexPrefix("diamond_fluid").flowFluid().define();
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourLimiter::new, PipeBehaviourLimiter::new).flowPower();
        builder.texSuffixes("_m0", "_m4", "_m8", "_m16", "_m32", "_m64", "_m128");
        builder.builder.itemTex(6);
        ironPower = builder.idTexPrefix("iron_power").define();
        diamondPower = builder.idTexPrefix("diamond_power").define();
        if (!BCTransportConfig.disableRfPipe) {
            builder.flowRf();
            ironRf = builder.idTexPrefix("iron_rf").define();
            diamondRf = builder.idTexPrefix("diamond_rf").define();
        }
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourWoodDiamond::new, PipeBehaviourWoodDiamond::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        diaWoodItem = builder.idTexPrefix("diamond_wood_item").flowItem().define();
        diaWoodFluid = builder.idTexPrefix("diamond_wood_fluid").flowFluid().define();
        builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new);
        diaWoodPower = builder.idTexPrefix("diamond_wood_power").flowPower().define();
        if (!BCTransportConfig.disableRfPipe) {
            diaWoodRf = builder.idTexPrefix("diamond_wood_rf").flowRf().define();
        }
        builder.builder.itemTex(0);

        builder.logic(PipeBehaviourClay::new, PipeBehaviourClay::new);
        clayItem = builder.idTex("clay_item").flowItem().define();
        clayFluid = builder.idTex("clay_fluid").flowFluid().define();

        builder.logic(PipeBehaviourVoid::new, PipeBehaviourVoid::new);
        voidItem = builder.idTex("void_item").flowItem().define();
        voidFluid = builder.idTex("void_fluid").flowFluid().define();

        builder.logic(PipeBehaviourObsidian::new, PipeBehaviourObsidian::new);
        obsidianItem = builder.idTex("obsidian_item").flowItem().define();

        DyeColor[] colourArray = DyeColor.values();
        String[] texSuffix = new String[16];
        for (int i = 0; i < 16; i++) {
            texSuffix[i] = "_" + colourArray[i].getName();
        }
        builder.logic(PipeBehaviourLapis::new, PipeBehaviourLapis::new).texSuffixes(texSuffix);
        lapisItem = builder.idTexPrefix("lapis_item").flowItem().define();

        String[] texSuffixPlus = Arrays.copyOf(texSuffix, 17);
        texSuffixPlus[16] = "_filled";
        builder.logic(PipeBehaviourDaizuli::new, PipeBehaviourDaizuli::new).texSuffixes(texSuffixPlus);
        builder.builder.itemTex(0, 0, 16);
        daizuliItem = builder.idTexPrefix("daizuli_item").flowItem().define();

        builder.logic(PipeBehaviourEmzuli::new, PipeBehaviourEmzuli::new).texSuffixes("_clear", "_filled");
        builder.builder.itemTex(0, 0, 1);
        emzuliItem = builder.idTexPrefix("emzuli_item").flowItem().define();

        builder.builder.itemTex(0);
        builder.logic(PipeBehaviourStripes::new, PipeBehaviourStripes::new);
        stripesItem = builder.idTex("stripes_item").flowItem().define();
    }

    private static class DefinitionBuilder {
        public final PipeDefinitionBuilder builder = new PipeDefinitionBuilder();

        public DefinitionBuilder idTexPrefix(String both) {
            return id(both).texPrefix(both);
        }

        public DefinitionBuilder idTex(String both) {
            return id(both).texture(both);
        }

        public DefinitionBuilder id(String post) {
            builder.identifier = new Identifier("buildcrafttransport", post);
            return this;
        }

        public DefinitionBuilder tex(String prefix, String... suffixes) {
            return texPrefix(prefix).texSuffixes(suffixes);
        }

        public DefinitionBuilder texPrefix(String prefix) {
            if (BCTransportConfig.powerPipeUseOldMjTexture && prefix.endsWith("_power")) {
                prefix = prefix.substring(0, prefix.length() - "_power".length()) + "_rf";
            }
            builder.texturePrefix = "buildcrafttransport:pipes/" + prefix;
            return this;
        }

        public DefinitionBuilder texSuffixes(String... suffixes) {
            builder.textureSuffixes = suffixes.length == 0 ? new String[] { "" } : suffixes;
            return this;
        }

        public DefinitionBuilder logic(IPipeCreator creator, IPipeLoader loader) {
            builder.logicConstructor = creator;
            builder.logicLoader = loader;
            return this;
        }

        public DefinitionBuilder flowItem()  { return flow(PipeApi.flowItems);  }
        public DefinitionBuilder flowFluid() { return flow(PipeApi.flowFluids); }
        public DefinitionBuilder flowPower() { return flow(PipeApi.flowPower);  }
        public DefinitionBuilder flowRf()    { return flow(PipeApi.flowRf);     }

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
