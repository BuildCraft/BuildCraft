/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.tiles;

import java.util.Locale;

/** This interface should be implemented by any Tile Entity which wishes to have non-redstone automation (for example,
 * BuildCraft Gates, but also other mods which implement it, e.g. OpenComputers). */
public interface IControllable {
    enum Mode {
        ON,
        OFF,
        LOOP;

        public static final Mode[] VALUES = values();

        public final String lowerCaseName = name().toLowerCase(Locale.ROOT);
    }

    Mode getControlMode();

    void setControlMode(Mode mode);

    default boolean acceptsControlMode(Mode mode) {
        return mode != null;
    }
}
