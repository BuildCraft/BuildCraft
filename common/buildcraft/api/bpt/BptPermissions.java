package buildcraft.api.bpt;

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
    INSERT_CUSTOM
}
