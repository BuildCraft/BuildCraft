package buildcraft.api.bpt;

import com.google.common.collect.ImmutableSet;

public enum BptPermissions {
    /** Materials (items, fluids, power, etc) are not required by blueprints. Only available in creative mode. */
    FREE_MATERIALS,
    /** Items can be added to inventories. */
    INSERT_ITEMS,
    /** Power can be inserted into machines */
    INSERT_POWER,
    /** Fluids can be inserted into machines/tiles/entities/etc */
    INSERT_FLUID,
    /** Custom resources (perhaps essentia from thaumcraft or something else from a different mod. Either way something
     * that requires a special {@link IBuilder} instance that is capable of providing those materials.) */
    INSERT_CUSTOM;

    public static final ImmutableSet<BptPermissions> SET_NORMAL_SURVIVAL;
    public static final ImmutableSet<BptPermissions> SET_NORMAL_CREATIVE;

    static {
        ImmutableSet.Builder<BptPermissions> builder = ImmutableSet.builder();

        builder.add(INSERT_ITEMS);
        builder.add(INSERT_POWER);
        builder.add(INSERT_FLUID);

        SET_NORMAL_SURVIVAL = builder.build();

        builder.add(FREE_MATERIALS);

        SET_NORMAL_CREATIVE = builder.build();
    }
}
