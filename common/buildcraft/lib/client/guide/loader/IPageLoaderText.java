package buildcraft.lib.client.guide.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;

import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;

public interface IPageLoaderText extends IPageLoader {
    @Override
    default GuidePageFactory loadPage(InputStream in, PageEntry entry) throws IOException {
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        return loadPage(new BufferedReader(reader), entry);
    }

    GuidePageFactory loadPage(BufferedReader bufferedReader, PageEntry entry) throws IOException;
}
