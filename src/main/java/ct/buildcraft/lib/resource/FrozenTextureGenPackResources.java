package ct.buildcraft.lib.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.NativeImage;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.BCLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class FrozenTextureGenPackResources implements PackResources{
	
	public static final ResourceManager manager = Minecraft.getInstance().getResourceManager();
	public static final Logger logger = BCLog.logger;

	//public final Map<ResourceLocation, Function<ResourceLocation, InputStream>> resources = new HashMap<>();
	public final HashBiMap<ResourceLocation, ResourceLocation> transLocations = HashBiMap.create();//<transformed, src>
	
	protected InputStream load(ResourceLocation srcLocation) {
		Optional<Resource> resourceOp = manager.getResource(srcLocation);
		if(resourceOp.isEmpty()) {
			logger.error("Can not read resource : "+srcLocation+" when creating forzen sprite, maybe called too early");
			return null;
		}
		Resource resource = resourceOp.get();

		try {
			NativeImage srcimage = NativeImage.read(resource.open());
            int widthOld = srcimage.getWidth();
//            int heightOld = srcimage.getHeight();
	        try {
	        	var animationmetadatasection = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER);
	            if (animationmetadatasection.isEmpty() && widthOld != srcimage.getWidth()) {
	                BCLog.logger.warn(
	                    "[lib.fluid] Failed to create a frozen sprite of " + srcLocation.toString()
	                        + " as the source sprite wasnn't an animation and had a different width ("
	                        + srcimage.getHeight() + ") from height (" + widthOld + ")!"
	                );
	                return null;
	            }
	        } catch (Exception exception) {
	        	logger.error("Unable to parse metadata from {} : {}", srcLocation, exception);
	        }

            int width = widthOld * 2;
            int height = width;//heightOld * 2;

            NativeImage outImage = new NativeImage(width, height, false);
			for (int x = 0; x < width; x++) {
				int fx = (x % widthOld);
				for (int y = 0; y < height; y++) {
					int fy = y % width;//heightOld;
					outImage.setPixelRGBA(x, y, srcimage.getPixelRGBA(fx, fy));
				}
			}
			ByteArrayInputStream data = new ByteArrayInputStream(outImage.asByteArray());
			if(outImage!=null)
				outImage.close();
			if(srcimage!=null)
				srcimage.close();
            return data;
		} catch (IOException e) {
			logger.error("fail to open NativeImage at "+srcLocation);
			e.printStackTrace();
		}
		return null;
	}
	
	public ResourceLocation registry(ResourceLocation srcLocation) {
		ResourceLocation tran = new ResourceLocation(BCLib.MODID, "generated/forzen/"+srcLocation.getPath());
		transLocations.put(tran, srcLocation);
		return tran;
	}
	
	@Override
	public InputStream getRootResource(String p_10294_) throws IOException {
		return null;
	}

	@Override
	public InputStream getResource(PackType p_10289_, ResourceLocation location) throws IOException {
		return load(transLocations.get(location));
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType p_215339_, String p_215340_, String p_215341_,
			Predicate<ResourceLocation> p_215342_) {
		return transLocations.keySet();
	}

	@Override
	public boolean hasResource(PackType p_10292_, ResourceLocation location) {
		return transLocations.containsKey(location);
	}

	@Override
	public Set<String> getNamespaces(PackType p_10283_) {
		return Set.of(BCLib.MODID);
	}

	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> p_10291_) throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return "Generated";
	}

	@Override
	public void close() {
		
	}

}
