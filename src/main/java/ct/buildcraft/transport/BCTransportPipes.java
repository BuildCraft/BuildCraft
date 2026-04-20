/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import java.util.Arrays;

import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeDefinition;
import ct.buildcraft.api.transport.pipe.PipeDefinition.IPipeCreator;
import ct.buildcraft.api.transport.pipe.PipeDefinition.IPipeLoader;
import ct.buildcraft.api.transport.pipe.PipeDefinition.PipeDefinitionBuilder;
import ct.buildcraft.api.transport.pipe.PipeFlowType;
import ct.buildcraft.transport.pipe.PipeRegistry;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourClay;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourCobble;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourDaizuli;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourDiamondFluid;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourDiamondItem;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourGold;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourIron;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourLapis;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourObsidian;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourQuartz;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourSandstone;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourStone;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourStructure;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourVoid;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourWood;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourWoodPower;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;


public class BCTransportPipes {
    public static PipeDefinition structure;

    public static PipeDefinition woodItem;
    public static PipeDefinition woodFluid;
    public static PipeDefinition woodPower;

    public static PipeDefinition stoneItem;
    public static PipeDefinition stoneFluid;
    public static PipeDefinition stonePower;

    public static PipeDefinition cobbleItem;
    public static PipeDefinition cobbleFluid;
    public static PipeDefinition cobblePower;

    public static PipeDefinition quartzItem;
    public static PipeDefinition quartzFluid;
    public static PipeDefinition quartzPower;

    public static PipeDefinition goldItem;
    public static PipeDefinition goldFluid;
    public static PipeDefinition goldPower;

    public static PipeDefinition sandstoneItem;
    public static PipeDefinition sandstoneFluid;
    public static PipeDefinition sandstonePower;

    public static PipeDefinition ironItem;
    public static PipeDefinition ironFluid;
    // public static PipeDefinition ironPower;

    public static PipeDefinition diamondItem;
    public static PipeDefinition diamondFluid;
    // public static PipeDefinition diamondPower;

    public static PipeDefinition diaWoodItem;
    public static PipeDefinition diaWoodFluid;

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
    
//    public static int tex_int = 0;

    public static void preInit() {
        DefinitionBuilder builder = new DefinitionBuilder();

        builder.logic(PipeBehaviourStructure::new, PipeBehaviourStructure::new);
        builder.builder.enableBorderColouring();
        structure = builder.idTex("structure").flow(PipeApi.flowStructure).define();
        builder.builder.enableColouring();

        builder.logic(PipeBehaviourWood::new, PipeBehaviourWood::new).texSuffixes("_clear", "_filled");
        woodItem = builder.idTexPrefix("wood_item").flowItem().define();
        woodFluid = builder.idTexPrefix("wood_fluid").flowFluid().define();
        builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new);
        woodPower = builder.idTexPrefix("wood_power").flowPower().define();

        builder.logic(PipeBehaviourStone::new, PipeBehaviourStone::new);
        stoneItem = builder.idTex("stone_item").flowItem().define();
        stoneFluid = builder.idTex("stone_fluid").flowFluid().define();
        stonePower = builder.idTex("stone_power").flowPower().define();

        builder.logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new);
        cobbleItem = builder.idTex("cobblestone_item").flowItem().define();
        cobbleFluid = builder.idTex("cobblestone_fluid").flowFluid().define();
        cobblePower = builder.idTex("cobblestone_power").flowPower().define();

        builder.logic(PipeBehaviourQuartz::new, PipeBehaviourQuartz::new);
        quartzItem = builder.idTex("quartz_item").flowItem().define();
        quartzFluid = builder.idTex("quartz_fluid").flowFluid().define();
        quartzPower = builder.idTex("quartz_power").flowPower().define();

        builder.logic(PipeBehaviourGold::new, PipeBehaviourGold::new);
        goldItem = builder.idTex("gold_item").flowItem().define();
        goldFluid = builder.idTex("gold_fluid").flowFluid().define();
        goldPower = builder.idTex("gold_power").flowPower().define();

        builder.logic(PipeBehaviourSandstone::new, PipeBehaviourSandstone::new);
        sandstoneItem = builder.idTex("sandstone_item").flowItem().define();
        sandstoneFluid = builder.idTex("sandstone_fluid").flowFluid().define();
        sandstonePower = builder.idTex("sandstone_power").flowPower().define();

        builder.logic(PipeBehaviourIron::new, PipeBehaviourIron::new).texSuffixes("_clear", "_filled");
        ironItem = builder.idTexPrefix("iron_item").flowItem().define();
        ironFluid = builder.idTexPrefix("iron_fluid").flowFluid().define();
        // ironPower = builder.idTexPrefix("iron_power").flowPower().define();

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

        builder.logic(PipeBehaviourWoodDiamond::new, PipeBehaviourWoodDiamond::new).texSuffixes("_clear", "_filled");
        diaWoodItem = builder.idTexPrefix("diamond_wood_item").flowItem().define();
        diaWoodFluid = builder.idTexPrefix("diamond_wood_fluid").flowFluid().define();

        builder.logic(PipeBehaviourClay::new, PipeBehaviourClay::new);
        clayItem = builder.idTex("clay_item").flowItem().define();
        clayFluid = builder.idTex("clay_fluid").flowFluid().define();

        builder.logic(PipeBehaviourVoid::new, PipeBehaviourVoid::new);
        voidItem = builder.idTex("void_item").flowItem().define();
        voidFluid = builder.idTex("void_fluid").flowFluid().define();

        builder.logic(PipeBehaviourObsidian::new, PipeBehaviourObsidian::new);
        obsidianItem = builder.idTex("obsidian_item").flowItem().define();
        // obsidianFluid = builder.idTex("obsidian_fluid").flowFluid().define();

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
        daizuliItem = builder.idTexPrefix("daizuli_item").flowItem().define();

        builder.logic(PipeBehaviourEmzuli::new, PipeBehaviourEmzuli::new).texSuffixes("_clear", "_filled");
        emzuliItem = builder.idTexPrefix("emzuli_item").flowItem().define();

        builder.logic(PipeBehaviourStripes::new, PipeBehaviourStripes::new);
        stripesItem = builder.idTex("stripes_item").flowItem().define();
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
