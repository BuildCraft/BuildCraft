package ct.buildcraft.lib.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public class GeneratedPackResources implements PackResources{

	@Override
	public InputStream getRootResource(String p_10294_) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResource(PackType p_10289_, ResourceLocation p_10290_) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType p_215339_, String p_215340_, String p_215341_,
			Predicate<ResourceLocation> p_215342_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasResource(PackType p_10292_, ResourceLocation p_10293_) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getNamespaces(PackType p_10283_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> p_10291_) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
