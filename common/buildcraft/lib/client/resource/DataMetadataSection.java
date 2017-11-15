package buildcraft.lib.client.resource;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JsonUtils;

/** Generic metadata section, containing any types of data. */
public class DataMetadataSection implements IMetadataSection {
    public static final String SECTION_NAME = "buildcraft_data";

    public final JsonObject data;

    public DataMetadataSection(JsonObject data) {
        this.data = data;
    }

    public static final IMetadataSectionSerializer<DataMetadataSection> DESERIALISER =
        new IMetadataSectionSerializer<DataMetadataSection>() {
            @Override
            public DataMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return new DataMetadataSection(JsonUtils.getJsonObject(json, "data section"));
            }

            @Override
            public String getSectionName() {
                return SECTION_NAME;
            }
        };
}
