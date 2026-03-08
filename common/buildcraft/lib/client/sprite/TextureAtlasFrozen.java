package buildcraft.lib.client.sprite;

import buildcraft.lib.BCLib;
import buildcraft.lib.client.render.fluid.SpriteFluidFrozen;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TextureAtlasFrozen extends TextureAtlas {
    private Map<ResourceLocation, TextureAtlasSprite> srcSprites;
    private Map<ResourceLocation, ResourceLocation> frozenLocation2RegistryName = Maps.newConcurrentMap();

    public TextureAtlasFrozen(ResourceLocation location) {
        super(location);
    }

    public void setSrcSprites(Map<ResourceLocation, TextureAtlasSprite> srcSprites) {
        this.srcSprites = srcSprites;
    }

    public Collection<TextureAtlasSprite.Info> getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> registryNames) {
        List<CompletableFuture<?>> list = Lists.newArrayList();
        Queue<TextureAtlasSprite.Info> queue = new ConcurrentLinkedQueue();

        for (ResourceLocation registryName : registryNames) {
            if (!MissingTextureAtlasSprite.getLocation().equals(registryName)) {
                list.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite srcSprite = this.srcSprites.get(registryName);
                    if (srcSprite != null) {
                        ResourceLocation frozenLocation = new ResourceLocation(BCLib.MODID, "fluid_" + registryName.toString().replace(':', '_') + "_convert_frozen");
                        frozenLocation2RegistryName.put(frozenLocation, registryName);

                        TextureAtlasSprite.Info frozenInfo = new TextureAtlasSprite.Info(frozenLocation, srcSprite.getWidth() * 2, srcSprite.getHeight() * 2, AnimationMetadataSection.EMPTY);

                        queue.add(frozenInfo);
                    }
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf((CompletableFuture[]) list.toArray(new CompletableFuture[0])).join();
        return queue;
    }

    public List<TextureAtlasSprite> getLoadedSprites(ResourceManager resourceManager, Stitcher stitcher, int mipmapLevel) {
        Queue<TextureAtlasSprite> queue = new ConcurrentLinkedQueue();
        List<CompletableFuture<?>> list = Lists.newArrayList();
        stitcher.gatherSprites((frozenInfo, atlasWidth, atlasHeight, x, y) -> {
            if (frozenInfo == MissingTextureAtlasSprite.info()) {
                MissingTextureAtlasSprite missingtextureatlassprite = MissingTextureAtlasSprite.newInstance(this, mipmapLevel, atlasWidth, atlasHeight, x, y);
                queue.add(missingtextureatlassprite);
            } else {
                list.add(CompletableFuture.runAsync(() -> {
                    ResourceLocation fluidRegistryName = frozenLocation2RegistryName.get(frozenInfo.name());
                    NativeImage nativeImage = SpriteFluidFrozen.createSpriteContents(this.srcSprites.get(fluidRegistryName));
                    TextureAtlasSprite textureatlassprite = new SpriteFluidFrozen(this, frozenInfo, mipmapLevel, atlasWidth, atlasHeight, x, y, nativeImage, fluidRegistryName);
                    queue.add(textureatlassprite);
                }, Util.backgroundExecutor()));
            }

        });
        CompletableFuture.allOf((CompletableFuture[]) list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(queue);
    }
}
