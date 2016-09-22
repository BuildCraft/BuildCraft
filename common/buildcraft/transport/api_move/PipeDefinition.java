package buildcraft.transport.api_move;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public final class PipeDefinition {
    public final ResourceLocation identifier;
    public final IPipeCreator logicConstructor;
    public final IPipeLoader logicLoader;
    public final PipeFlowType flowType;
    private final String texturePrefix;
    private final String[] textureSuffixes;

    // TODO: this can't be in the API as-is, as this class is in lib!
    // TODO: Move sprite handling out of this class to be handled by the model baker
    @SideOnly(Side.CLIENT)
    private SpriteHolder[] sprites;

    public PipeDefinition(ResourceLocation identifier, String texturePrefix, String[] textureSuffixes, IPipeCreator logicConstructor, IPipeLoader logicLoader, PipeFlowType flowType) {
        this.identifier = identifier;
        this.texturePrefix = texturePrefix;
        this.textureSuffixes = textureSuffixes;
        this.logicConstructor = logicConstructor;
        this.logicLoader = logicLoader;
        this.flowType = flowType;
    }

    @SideOnly(Side.CLIENT)
    public void initSprites() {
        sprites = new SpriteHolder[textureSuffixes.length];
        for (int i = 0; i < textureSuffixes.length; i++) {
            sprites[i] = SpriteHolderRegistry.getHolder(texturePrefix + textureSuffixes[i]);
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
