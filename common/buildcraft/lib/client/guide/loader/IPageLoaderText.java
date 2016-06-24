package buildcraft.lib.client.guide.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;

import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public interface IPageLoaderText extends IPageLoader {
    @Override
    default GuidePartFactory<? extends GuidePageBase> loadPage(InputStream in, JsonEntry entry) throws IOException {
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        return loadPage(new BufferedReader(reader), entry);
    }

    GuidePartFactory<? extends GuidePageBase> loadPage(BufferedReader bufferedReader, JsonEntry entry) throws IOException;
}
