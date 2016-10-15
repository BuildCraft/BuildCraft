package buildcraft.transport;

import net.minecraft.util.ResourceLocation;

import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.api_move.PipeDefinition.IPipeCreator;
import buildcraft.transport.api_move.PipeDefinition.IPipeLoader;
import buildcraft.transport.api_move.PipeDefinition.PipeDefinitionBuilder;
import buildcraft.transport.api_move.PipeFlowType;
import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.behaviour.PipeBehaviourGold;
import buildcraft.transport.pipe.behaviour.PipeBehaviourSandstone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWood;

public class BCTransportPipes {
    public static PipeDefinition woodItem;
    public static PipeDefinition woodFluid;
    public static PipeDefinition woodPower;

    public static PipeDefinition stoneItem;
    public static PipeDefinition stoneFluid;
    public static PipeDefinition stonePower;

    public static PipeDefinition goldItem;
    public static PipeDefinition goldFluid;
    public static PipeDefinition goldPower;

    public static PipeDefinition sandstoneItem;
    public static PipeDefinition sandstoneFluid;
    public static PipeDefinition sandstonePower;

    public static void preInit() {
        DefinitionBuilder builder = new DefinitionBuilder();
        builder.logic(PipeBehaviourWood::new, PipeBehaviourWood::new).texSuffixes("_clear", "_filled");
        woodItem = builder.idTexPrefix("wood_item").flowItem().define();
        woodFluid = builder.idTexPrefix("wood_fluid").flowFluid().define();
        woodPower = builder.idTexPrefix("wood_power").flowPower().define();

        builder.logic(PipeBehaviourStone::new, PipeBehaviourStone::new);
        stoneItem = builder.idTex("stone_item").flowItem().define();
        stoneFluid = builder.idTex("stone_fluid").flowFluid().define();
        stonePower = builder.idTex("stone_power").flowPower().define();

        builder.logic(PipeBehaviourGold::new, PipeBehaviourGold::new);
        goldItem = builder.idTex("gold_item").flowItem().define();
        goldFluid = builder.idTex("gold_fluid").flowFluid().define();
        goldPower = builder.idTex("gold_power").flowPower().define();

        builder.logic(PipeBehaviourSandstone::new, PipeBehaviourSandstone::new);
        sandstoneItem = builder.idTex("sandstone_item").flowItem().define();
        sandstoneFluid = builder.idTex("sandstone_fluid").flowFluid().define();
        sandstonePower = builder.idTex("sandstone_power").flowPower().define();
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
