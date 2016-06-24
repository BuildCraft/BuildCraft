package buildcraft.lib.client.guide.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import buildcraft.lib.misc.data.ZipFileHelper;

public class ZipLoadable implements ILoadableResource {
    private final ZipFileHelper zip;

    public ZipLoadable(File zip) throws IOException {
        try (FileInputStream fis = new FileInputStream(zip)) {
            ZipInputStream zis = new ZipInputStream(fis);
            this.zip = new ZipFileHelper(zis);
        }
    }

    @Override
    public ByteArrayInputStream getInputStreamFor(String location) {
        return zip.getInputStream(location);
    }
}
