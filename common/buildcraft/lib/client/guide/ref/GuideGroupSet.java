package buildcraft.lib.client.guide.ref;

import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GuideGroupSet {

    public enum GroupDirection {
        SRC_TO_ENTRY("to."),
        ENTRY_TO_SRC("from.");

        public final String localePrefix;

        private GroupDirection(String localePrefix) {
            this.localePrefix = "buildcraft.guide.group." + localePrefix;
        }
    }

    public final ResourceLocation group;

    /** Everything that points to this group, and so will include this group in pages. */
    public final List<PageValue<?>> sources;

    /** Everything that is in the group, and will be displayed under this group. */
    public final List<PageValue<?>> entries;

    public GuideGroupSet(ResourceLocation group) {
        this.group = group;
        this.sources = new ArrayList<>();
        this.entries = new ArrayList<>();
    }

    public String getTitle(GroupDirection dir) {
        String post = group.getNamespace() + "." + group.getPath();
        return LocaleUtil.localize(dir.localePrefix + post);
    }

    public List<PageValue<?>> getValues(GroupDirection direction) {
        return direction == GroupDirection.SRC_TO_ENTRY ? entries : sources;
    }

    public GuideGroupSet addSingle(Object value) {
        PageValue<?> entry = GuideGroupManager.toPageValue(value);
        if (entry != null) {
            entries.add(entry);
        }
        return this;
    }

    public GuideGroupSet addArray(Object... values) {
        for (Object value : values) {
            addSingle(value);
        }
        return this;
    }

    public GuideGroupSet addCollection(Collection<? extends Object> values) {
        for (Object value : values) {
            addSingle(value);
        }
        return this;
    }

    public GuideGroupSet addKey(Object value) {
        PageValue<?> entry = GuideGroupManager.toPageValue(value);
        if (entry != null) {
            sources.add(entry);
        }
        return this;
    }

    public GuideGroupSet addKeyArray(Object... values) {
        for (Object value : values) {
            addKey(value);
        }
        return this;
    }

    public GuideGroupSet addKeyCollection(Collection<? extends Object> values) {
        for (Object value : values) {
            addKey(value);
        }
        return this;
    }
}
