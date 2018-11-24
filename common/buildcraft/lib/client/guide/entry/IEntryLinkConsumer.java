package buildcraft.lib.client.guide.entry;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.parts.contents.PageLink;

public interface IEntryLinkConsumer {
    void addChild(JsonTypeTags tags, PageLink link);
}
