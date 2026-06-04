// STUB(R.Chen): 1.12 IResource → net.minecraft.resource.Resource in 1.20.1.
package net.minecraft.client.resources;

import java.io.InputStream;
import java.io.IOException;

public interface IResource extends AutoCloseable {
    InputStream getInputStream() throws IOException;
    boolean hasMetadata();
    <T> T getMetadata(String type);
    String getResourceLocation();
    @Override void close() throws IOException;
}
