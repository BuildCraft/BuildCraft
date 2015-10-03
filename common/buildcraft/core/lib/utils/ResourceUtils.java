package buildcraft.core.lib.utils;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public final class ResourceUtils {
	private ResourceUtils() {

	}

	public static IIcon getIconPriority(IIconRegister register, String prefix, String[] suffixes) {
		for (int i = 0; i < suffixes.length; i++) {
			String suffix = suffixes[i];
			String path = prefix + "/" + suffix;
			if (i == suffixes.length - 1 || resourceExists(iconToResourcePath(register, path))) {
				return register.registerIcon(path);
			}
		}
		return null;
	}

	public static IIcon getIcon(IIconRegister register, String prefix, String suffix) {
		return register.registerIcon(prefix + "/" + suffix);
	}

	public static String iconToResourcePath(IIconRegister register, String name) {
		int splitLocation = name.indexOf(":");

		if (register instanceof TextureMap) {
			String dir = ((TextureMap) register).getTextureType() == 1 ? "items" : "blocks";
			return name.substring(0, splitLocation) + ":textures/" + dir + "/" + name.substring(splitLocation + 1) + ".png";
		} else {
			// ???
			return name;
		}
	}

	/**
	 * Turns a block/item name into a prefix for finding textures.
	 *
	 * @param objectName
	 * @return
	 */
	public static String getObjectPrefix(String objectName) {
		if (objectName == null) {
			return null;
		}

		int splitLocation = objectName.indexOf(":");
		return objectName.substring(0, splitLocation).replaceAll("[^a-zA-Z0-9\\s]", "") + objectName.substring(splitLocation);
	}

	public static boolean resourceExists(String name) {
		try {
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(name));
			return resource != null;
		} catch (IOException e) {
			return false;
		}
	}
}
