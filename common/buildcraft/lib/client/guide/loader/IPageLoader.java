package buildcraft.lib.client.guide.loader;

import java.io.IOException;
import java.io.InputStream;

import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public interface IPageLoader {
    GuidePartFactory<? extends GuidePageBase> loadPage(InputStream in, JsonEntry entry) throws IOException;
}
