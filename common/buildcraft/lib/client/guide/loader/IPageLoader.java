package buildcraft.lib.client.guide.loader;

import java.io.IOException;
import java.io.InputStream;

import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;

public interface IPageLoader {
    GuidePageFactory loadPage(InputStream in, PageEntry entry) throws IOException;
}
