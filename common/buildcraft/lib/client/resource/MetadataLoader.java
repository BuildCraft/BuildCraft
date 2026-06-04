package buildcraft.lib.client.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resources.IResource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.Identifier;

/** Alternate metadata loader for {@link IResource#getMetadata(String)} */
public class MetadataLoader {

    private static boolean hasRegistered = false;

    private static void register() {
        if (!hasRegistered) {
            hasRegistered = true;
            MetadataSerializer metaReg = MinecraftClient.getInstance().getResourcePackRepository().rprMetadataSerializer;
            metaReg.registerMetadataSectionType(DataMetadataSection.DESERIALISER, DataMetadataSection.class);
        }
    }

    /** @param samePack If true, then only the data in the same resource pack will be returned. */
    @Nullable
    public static DataMetadataSection getData(Identifier location, boolean samePack) {
        ResourceManager resManager = MinecraftClient.getInstance().getResourceManager();
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
            // That's not fine
            e.printStackTrace();
            return null;
        }
    }
}
