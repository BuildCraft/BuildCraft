package buildcraft.lib.client.resource;

import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;

// Calen: never used in 1.12.2

/** Generic metadata section, containing any types of data. */
@Deprecated()
//public class DataMetadataSection implements MetadataSection {
public class DataMetadataSection {
    public static final String SECTION_NAME = "buildcraft_data";

    public final JsonObject data;

    public DataMetadataSection(JsonObject data) {
        this.data = data;
    }

    public static final IMetadataSectionSerializer<DataMetadataSection> DESERIALISER =
            new IMetadataSectionSerializer<DataMetadataSection>() {
                @Override
//            public DataMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                public DataMetadataSection fromJson(JsonObject json) {
                    return new DataMetadataSection(JSONUtils.getAsJsonObject(json, SECTION_NAME));
                }

                @Override
//            public String getSectionName()
                public String getMetadataSectionName() {
                    return SECTION_NAME;
                }
            };
}
