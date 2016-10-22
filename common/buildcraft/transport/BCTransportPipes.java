package buildcraft.transport;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipeDefinition;
import buildcraft.api.transport.neptune.PipeDefinition.IPipeCreator;
import buildcraft.api.transport.neptune.PipeDefinition.IPipeLoader;
import buildcraft.api.transport.neptune.PipeDefinition.PipeDefinitionBuilder;
import buildcraft.api.transport.neptune.PipeFlowType;

import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.behaviour.*;

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

    public static PipeDefinition clayItem;
    public static PipeDefinition clayFluid;

    public static PipeDefinition voidItem;
    public static PipeDefinition voidFluid;

    public static void preInit() {
        DefinitionBuilder builder = new DefinitionBuilder();

        builder.logic(PipeBehaviourStructure::new, PipeBehaviourStructure::new);
        structure = builder.idTex("structure").flow(PipeAPI.flowStructure).define();

        builder.logic(PipeBehaviourWood::new, PipeBehaviourWood::new).texSuffixes("_clear", "_filled");
        woodItem = builder.idTexPrefix("wood_item").flowItem().define();
        woodFluid = builder.idTexPrefix("wood_fluid").flowFluid().define();
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

        builder.logic(PipeBehaviourClay::new, PipeBehaviourClay::new);
        clayItem = builder.idTex("clay_item").flowItem().define();
        clayFluid = builder.idTex("clay_fluid").flowFluid().define();

        builder.logic(PipeBehaviourVoid::new, PipeBehaviourVoid::new);
        voidItem = builder.idTex("void_item").flowItem().define();
        voidFluid = builder.idTex("void_fluid").flowFluid().define();
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
            return flow(PipeAPI.flowItems);
        }

        public DefinitionBuilder flowFluid() {
            return flow(PipeAPI.flowFluids);
        }

        public DefinitionBuilder flowPower() {
            return flow(PipeAPI.flowPower);
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
