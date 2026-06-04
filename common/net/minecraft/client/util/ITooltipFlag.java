// STUB(R.Chen): ITooltipFlag shim for 1.12.2 compat → maps to TooltipContext
package net.minecraft.client.util;

public interface ITooltipFlag {
    boolean isAdvanced();
    default boolean isCreative() { return false; }

    enum TooltipFlags implements ITooltipFlag {
        NORMAL, ADVANCED;
        @Override
        public boolean isAdvanced() { return this == ADVANCED; }
    }
}
