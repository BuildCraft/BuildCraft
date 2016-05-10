package buildcraft.lib.client.guide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;

public class PageMeta {
    public final String title;
    public final String customImageLocation;
    public final String itemKey;
    public final PageTypeTags typeTags;

    public PageMeta(String title, String customImageLocation, String itemKey, PageTypeTags typeTags) {
        this.title = title;
        this.customImageLocation = customImageLocation;
        this.itemKey = itemKey;
        this.typeTags = typeTags;
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

    public enum ETypeTag {
        MOD("mod."),
        SUB_MOD("submod."),
        STAGE("stage."),
        TYPE("type."),
        SUB_TYPE("subtype.");

        public final String preText;

        private ETypeTag(String preText) {
            this.preText = "buildcraft.guide.chapter." + preText;
        }
    }

    public enum EStage implements IComparableLine {
        @SerializedName("basic") BASE,
        @SerializedName("common") COMMON,
        @SerializedName("rare") RARE,
        @SerializedName("nether") NETHER,
        @SerializedName("end") END;

        public final String langKey;

        private EStage() {
            this.langKey = "buildcraft.guide.chapter.stage." + name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getText() {
            return langKey;
        }

        @Override
        public int compareToLine(IComparableLine other) {
            if (other instanceof EStage) {
                return super.compareTo((EStage) other);
            }
            return getText().compareTo(other.getText());
        }
    }

    public static class TypeOrder {
        public final ImmutableList<ETypeTag> tags;

        public TypeOrder(ETypeTag... tags) {
            this.tags = ImmutableList.copyOf(tags);
        }
    }

    public static class PageTypeTags {
        public final String mod, submod, type, subtype;
        public final EStage stage;

        public PageTypeTags(String mod, String submod, EStage stage, String type, String subType) {
            this.mod = mod;
            this.submod = submod;
            this.stage = stage;
            this.type = type;
            this.subtype = subType;
        }

        public IComparableLine getFor(ETypeTag tag) {
            if (tag == ETypeTag.MOD) return asLine(tag.preText + mod);
            else if (tag == ETypeTag.SUB_MOD) return new SubModLine(submod);
            else if (tag == ETypeTag.STAGE) return stage;
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
