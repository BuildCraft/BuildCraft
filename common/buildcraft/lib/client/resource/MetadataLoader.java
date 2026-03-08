package buildcraft.lib.client.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

// Calen: never used in 1.12.2

/** Alternate metadata loader for {@link Resource#getMetadata(String)} */
@Deprecated(forRemoval = true)
public class MetadataLoader {

    private static boolean hasRegistered = false;

    private static void register() {
        if (!hasRegistered) {
            hasRegistered = true;
//            MetadataSerializer metaReg = Minecraft.getInstance().getResourcePackRepository().rprMetadataSerializer;
//            metaReg.registerMetadataSectionType(DataMetadataSection.DESERIALISER, DataMetadataSection.class);
        }
    }

    /** @param samePack If true, then only the data in the same resource pack will be returned. */
    @Nullable
    public static DataMetadataSection getData(ResourceLocation location, boolean samePack) {
        ResourceManager resManager = Minecraft.getInstance().getResourceManager();
        register();
        try {
//            List<IResource> resources = resManager.getAllResources(location);
            List<Resource> resources = resManager.getResources(location);
            DataMetadataSection section = null;
            for (Resource resource : resources) {
//                section = resource.getMetadata(DataMetadataSection.SECTION_NAME);
                section = resource.getMetadata(DataMetadataSection.DESERIALISER);
                if (section != null || samePack) {
                    break;
                }
            }
            for (Resource res : resources) {
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
            // That's not fine
            e.printStackTrace();
            return null;
        }
    }
}
