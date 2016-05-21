package buildcraft.api.bpt;

public enum BptPermissions {
    /** Items are not required by blueprints. Only available in creative mode. */
    FREE_ITEMS,
    /** Indicates that this builder has been given operator permissions. Only available in creative and is either A)
     * singleplayer or B) the placing player has been opped by the server. */
    SERVER_OP,
    /** Block placement logic does *not* have to check to see if it can place. Used in singleplayer worlds and by server
     * OP's. */
    NO_OWNERSHIP,
}
