package buildcraft.transport.api_move;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.data.LoadingException;

public final class PipeDefinition {
    private static final Map<ResourceLocation, PipeDefinition> definitions = new HashMap<>();

    public final ResourceLocation key;
    public final IPipeCreator logicConstructor;
    public final IPipeLoader logicLoader;
    private final String texturePrefix;
    private final String[] textureSuffixes;

    @SideOnly(Side.CLIENT)
    private SpriteHolder[] sprites;

    public PipeDefinition(ResourceLocation key, String texturePrefix, String[] textureSuffixes, IPipeCreator logicConstructor, IPipeLoader logicLoader) {
        this.key = key;
        this.texturePrefix = texturePrefix;
        this.textureSuffixes = textureSuffixes;
        this.logicConstructor = logicConstructor;
        this.logicLoader = logicLoader;
    }

    public static void register(PipeDefinition definition) {
        register(definition.key, definition);
    }

    public static void register(ResourceLocation key, PipeDefinition definition) {
        definitions.put(key, definition);
        BCLog.logger.info("[pipe-reg] Registered a pipe defintion for " + key);
    }

    @Nullable
    public static PipeDefinition getDefinition(ResourceLocation identifier) {
        return definitions.get(identifier);
    }

    @Nonnull
    public static PipeDefinition loadDefinition(String identifier) throws LoadingException {
        PipeDefinition def = getDefinition(new ResourceLocation(identifier));
        if (def == null) {
            throw new LoadingException("Unknown pipe defintion " + identifier);
        }
        return def;
    }

    @SideOnly(Side.CLIENT)
    public static void fmlInit() {
        for (PipeDefinition def : definitions.values()) {
            def.sprites = new SpriteHolder[def.textureSuffixes.length];
            for (int i = 0; i < def.textureSuffixes.length; i++) {
                def.sprites[i] = SpriteHolderRegistry.getHolder(def.texturePrefix + def.textureSuffixes[i]);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSprite(int index) {
        return sprites[index % sprites.length].getSprite();
    }

    @FunctionalInterface
    public interface IPipeCreator {
        PipeBehaviour createBehaviour(IPipe t);
    }

    @FunctionalInterface
    public interface IPipeLoader {
        PipeBehaviour loadBehaviour(IPipe t, NBTTagCompound u);
    }
}
