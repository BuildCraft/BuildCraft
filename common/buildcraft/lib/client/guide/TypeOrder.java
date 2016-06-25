package buildcraft.lib.client.guide;

import com.google.common.collect.ImmutableList;

public class TypeOrder {
    public final ImmutableList<ETypeTag> tags;

    public TypeOrder(ETypeTag... tags) {
        this.tags = ImmutableList.copyOf(tags);
    }
}