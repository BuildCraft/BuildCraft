/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import java.util.Locale;

/** Stores an argument description: a name, and a type. Used by
 * {@link InternalCompiler#compileFunction(String, FunctionContext, Argument...)}, for string-based functions. */
public class Argument {
    public final String name;
    public final Class<?> type;

    public Argument(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public static Argument argLong(String name) {
        return new Argument(name, long.class);
    }

    public static Argument argDouble(String name) {
        return new Argument(name, double.class);
    }

    public static Argument argBoolean(String name) {
        return new Argument(name, boolean.class);
    }

    public static Argument argString(String name) {
        return new Argument(name, String.class);
    }

    public static Argument argObject(String name, Class<?> type) {
        return new Argument(name, type);
    }

    @Override
    public String toString() {
        return type.getSimpleName().toLowerCase(Locale.ROOT) + " '" + name + "'";
    }
}
