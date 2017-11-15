package buildcraft.lib.client.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

/** Alternate metadata loader for {@link IResource#getMetadata(String)} */
public class MetadataLoader {

    private static boolean hasRegistered = false;

    private static void register() {
        if (!hasRegistered) {
            hasRegistered = true;
            MetadataSerializer metaReg = Minecraft.getMinecraft().getResourcePackRepository().rprMetadataSerializer;
            metaReg.registerMetadataSectionType(DataMetadataSection.DESERIALISER, DataMetadataSection.class);
        }
    }

    /** @param samePack If true, then only the data
     * @return */
    @Nullable
    public static DataMetadataSection getData(ResourceLocation location, boolean samePack) {
        IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
        register();
        try {
            List<IResource> resources = resManager.getAllResources(location);
            DataMetadataSection section = null;
            for (IResource resource : resources) {
                section = resource.getMetadata(DataMetadataSection.SECTION_NAME);
                if (section != null || samePack) {
                    break;
                }
            }
            for (IResource res : resources) {
                try {
                    res.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
            return section;
        } catch (FileNotFoundException fnfe) {
            // That's fine
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
