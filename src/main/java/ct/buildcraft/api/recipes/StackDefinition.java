package ct.buildcraft.api.recipes;

import ct.buildcraft.api.core.IStackFilter;

/**
 * @deprecated TEMPORARY CLASS DO NOT USE!
 */
@Deprecated
public final class StackDefinition {
    public final IStackFilter filter;
    public final int count;

    public StackDefinition(IStackFilter filter, int count) {
        this.filter = filter;
        this.count = count;
    }

    public StackDefinition(IStackFilter filter) {
        this(filter, 1);
    }
}
