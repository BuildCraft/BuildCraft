package buildcraft.core.guide;

import java.io.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

public class LocationLoader {
    protected static InputStream getStream(ResourceLocation location) {
        try {
            InputStream stream = getStreamInternal(getForLang(location), true);
            if (stream == null) {
                stream = getStreamInternal(location, false);
            }
            return stream;
        } catch (FileNotFoundException fnf) {
            BCLog.logger.warn("Could not load the resource location " + location + " because it did not exist!");
            return null;
        } catch (IOException io) {
            BCLog.logger.warn("Could not load the resource location " + location + " because an exception was thrown!", io);
            return null;
        }
    }

    private static InputStream getStreamInternal(ResourceLocation location, boolean suppress) throws IOException {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            if (resource == null) {
                if (!suppress) {
                    BCLog.logger.warn("Could not load the resource location " + location + " because null was returned!");
                }
            } else {
                return resource.getInputStream();
            }
        } catch (IOException io) {
            if (!suppress) {
                throw io;
            }
        }
        return null;
    }

    /** @param location
     * @return A string that was the contents of the file, or an empty string if it could not be read */
    protected static String asString(ResourceLocation location) {
        StringBuilder builder = new StringBuilder();
        InputStream stream = getStream(location);
        if (stream == null) {
            return "";
        }
        try (BufferedReader buffered = new BufferedReader(new InputStreamReader(stream))) {
            String line = "";
            while ((line = buffered.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException io) {
            BCLog.logger.warn("Could not load the resource location " + location + " because an exception was thrown!", io);
            return "";
        }
        return builder.toString();
    }

    public static ResourceLocation getForLang(ResourceLocation location) {
        String domain = location.getResourceDomain();
        String path = location.getResourcePath();
        String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        path = path.replaceFirst("guide", "guide/" + lang);
        return new ResourceLocation(domain, path);
    }
}
