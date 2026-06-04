// STUB(R.Chen): 1.12 IMetadataSectionSerializer — removed.
package net.minecraft.client.resources.data;
public interface IMetadataSectionSerializer<T extends IMetadataSection> {
    String getSectionName();
    T deserialize(com.google.gson.JsonObject json);
}
