// STUB(R.Chen): ItemGroup.SEARCH compat for 1.12.2 migration.
package buildcraft.lib.compat.forge_stubs;

import net.minecraft.item.ItemGroup;

/** Provides 1.12.2 ItemGroup constants missing from 1.20.1. */
public final class ItemGroupCompat {
    private ItemGroupCompat() {}

    /** 1.12.2 ItemGroup.SEARCH - returns null in 1.20.1; callers should handle null. */
    public static final ItemGroup SEARCH = null;
    /** 1.12.2 ItemGroup.MISC */
    public static final ItemGroup MISC = null;
}
