package ct.buildcraft.lib;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.textures.ITextureAtlasSpriteLoader;

public class BCLibTASLoaders {
	public static final ResourceLocation oilBucket = new ResourceLocation("buildcraftenergy:oilbucket_tas_loader");
	public static final ResourceLocation forzenTexture = new ResourceLocation("buildcrafttransport:forzen_tas_loader");
	
	public static final ITextureAtlasSpriteLoader oilBucketGenerator = (
			TextureAtlas atlas, 
			ResourceManager resourceManager, 
			TextureAtlasSprite.Info textureInfo,
            Resource resource, int atlasWidth, int atlasHeight, int spriteX, int spriteY,
            int mipmapLevel, NativeImage image)->{
		return null;
	};
}
