package buildcraft.lib.client.guide.loader;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

public interface ILoadableResource {
    @Nullable
    InputStream getInputStreamFor(String location) throws IOException;
}
