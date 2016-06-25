package buildcraft.lib.client.guide;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.loader.MarkdownPageLoader;

@Deprecated
public class PageMeta {
    public final String title;
    public final String itemStack;
    public final PageTypeTags typeTags;

    public PageMeta(String title, String itemStack, PageTypeTags typeTags) {
        this.title = title;
        this.itemStack = itemStack;
        this.typeTags = typeTags;
    }

    public ItemStack getItemStack() {
        return MarkdownPageLoader.loadItemStack(itemStack);
    }

    public IComparableLine[] getLocationArray(TypeOrder order) {
        if (order == null || typeTags == null) {
            return new IComparableLine[0];
        } else {
            List<IComparableLine> strings = new ArrayList<>();
            for (ETypeTag tag : order.tags) {
                IComparableLine s = typeTags.getFor(tag);
                if (StringUtils.isNotEmpty(s.getText())) {
                    strings.add(s);
                }
            }
            return strings.toArray(new IComparableLine[strings.size()]);
        }
    }

    public static class PageTypeTags {
        public final String mod, submod, type, subtype;

        public PageTypeTags(String mod, String submod, String type, String subType) {
            this.mod = mod;
            this.submod = submod;
            this.type = type;
            this.subtype = subType;
        }

        public IComparableLine getFor(ETypeTag tag) {
            if (tag == ETypeTag.MOD) return asLine(tag.preText + mod);
            else if (tag == ETypeTag.SUB_MOD) return new SubModLine(submod);
            else if (tag == ETypeTag.TYPE) return asLine(tag.preText + type);
            else if (tag == ETypeTag.SUB_TYPE) return asLine(tag.preText + subtype);
            throw new IllegalArgumentException("Unknown type tag " + tag);
        }
    }

    private static class SubModLine implements IComparableLine {
        private final String submod;

        public SubModLine(String submod) {
            this.submod = submod;
        }

        @Override
        public String getText() {
            return ETypeTag.SUB_MOD.preText + submod;
        }

        @Override
        public int compareToLine(IComparableLine other) {
            if (other instanceof SubModLine) {
                boolean thisCore = "core".equalsIgnoreCase(submod);
                boolean otherCore = "core".equalsIgnoreCase(((SubModLine) other).submod);
                if (thisCore && !otherCore) {
                    return -1;
                } else if (!thisCore && otherCore) {
                    return 1;
                }
            }
            return getText().compareTo(other.getText());
        }
    }

    private static IComparableLine asLine(String text) {
        return new IComparableLine() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public int compareToLine(IComparableLine other) {
                return text.compareTo(other.getText());
            }
        };
    }
}
