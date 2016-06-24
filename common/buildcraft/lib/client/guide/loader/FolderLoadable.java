package buildcraft.lib.client.guide.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FolderLoadable implements ILoadableResource {
    private final File base;

    public FolderLoadable(File base) {
        this.base = base;
    }

    @Override
    public InputStream getInputStreamFor(String location) throws IOException {
        File file = new File(base, location);
        if (!file.isFile()) {
            return null;
        }
        return new FileInputStream(file);
    }
}
