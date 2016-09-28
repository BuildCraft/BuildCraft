package buildcraft.transport.api_move;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;

import buildcraft.transport.pipe.PipeRegistry;

public final class PipeDefinition {
    public final ResourceLocation identifier;
    public final IPipeCreator logicConstructor;
    public final IPipeLoader logicLoader;
    public final PipeFlowType flowType;
    public final String[] textures;
    public final int itemTextureTop, itemTextureCenter, itemTextureBottom;

    public PipeDefinition(PipeDefinitionBuilder builder) {
        this.identifier = builder.identifier;
        this.textures = new String[builder.textureSuffixes.length];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = builder.texturePrefix + builder.textureSuffixes[i];
        }
        this.logicConstructor = builder.logicConstructor;
        this.logicLoader = builder.logicLoader;
        this.flowType = builder.flowType;
        this.itemTextureTop = builder.itemTextureTop;
        this.itemTextureCenter = builder.itemTextureCenter;
        this.itemTextureBottom = builder.itemTextureBottom;
    }

    @FunctionalInterface
    public interface IPipeCreator {
        PipeBehaviour createBehaviour(IPipe t);
    }

    @FunctionalInterface
    public interface IPipeLoader {
        PipeBehaviour loadBehaviour(IPipe t, NBTTagCompound u);
    }

    public static class PipeDefinitionBuilder {
        public ResourceLocation identifier;
        public String texturePrefix;
        public String[] textureSuffixes = { "" };
        public IPipeCreator logicConstructor;
        public IPipeLoader logicLoader;
        public PipeFlowType flowType;
        public int itemTextureTop = 0;
        public int itemTextureCenter = 0;
        public int itemTextureBottom = 0;

        public PipeDefinitionBuilder() {}

        public PipeDefinitionBuilder(ResourceLocation identifier, IPipeCreator logicConstructor, IPipeLoader logicLoader, PipeFlowType flowType) {
            this.identifier = identifier;
            this.logicConstructor = logicConstructor;
            this.logicLoader = logicLoader;
            this.flowType = flowType;
        }

        public PipeDefinitionBuilder idTexPrefix(String both) {
            return id(both).texPrefix(both);
        }

        public PipeDefinitionBuilder idTex(String both) {
            return id(both).tex(both);
        }

        public PipeDefinitionBuilder id(String post) {
            identifier = new ResourceLocation(Loader.instance().activeModContainer().getModId(), post);
            return this;
        }

        public PipeDefinitionBuilder tex(String prefix, String... suffixes) {
            return texPrefix(prefix).texSuffixes(suffixes);
        }

        public PipeDefinitionBuilder texPrefix(String prefix) {
            texturePrefix = "buildcrafttransport:pipes/" + prefix;
            return this;
        }

        public PipeDefinitionBuilder texSuffixes(String... suffixes) {
            if (suffixes.length == 0) {
                textureSuffixes = new String[] { "" };
            } else {
                textureSuffixes = suffixes;
            }
            return this;
        }

        public PipeDefinitionBuilder itemTex(int all) {
            itemTextureTop = all;
            itemTextureCenter = all;
            itemTextureBottom = all;
            return this;
        }

        public PipeDefinitionBuilder itemTex(int top, int center, int bottom) {
            itemTextureTop = top;
            itemTextureCenter = center;
            itemTextureBottom = bottom;
            return this;
        }

        public PipeDefinitionBuilder logic(IPipeCreator creator, IPipeLoader loader) {
            logicConstructor = creator;
            logicLoader = loader;
            return this;
        }

        public PipeDefinitionBuilder flowItem() {
            return flow(PipeAPI.flowItems);
        }

        public PipeDefinitionBuilder flowFluid() {
            return flow(PipeAPI.flowFluids);
        }

        public PipeDefinitionBuilder flowPower() {
            return flow(PipeAPI.flowPower);
        }

        public PipeDefinitionBuilder flow(PipeFlowType flow) {
            flowType = flow;
            return this;
        }

        public PipeDefinition define() {
            PipeDefinition def = new PipeDefinition(this);
            PipeRegistry.INSTANCE.registerPipe(def);
            return def;
        }
    }
}
