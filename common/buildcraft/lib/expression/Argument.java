/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.NodeType;

import java.util.Locale;

public class Argument {
    public final String name;
    public final NodeType type;

    public Argument(String name, NodeType type) {
        this.name = name;
        this.type = type;
    }

    public static Argument argLong(String name) {
        return new Argument(name, NodeType.LONG);
    }

    public static Argument argDouble(String name) {
        return new Argument(name, NodeType.DOUBLE);
    }

    public static Argument argBoolean(String name) {
        return new Argument(name, NodeType.BOOLEAN);
    }

    public static Argument argString(String name) {
        return new Argument(name, NodeType.STRING);
    }

    @Override
    public String toString() {
        return type.name().toLowerCase(Locale.ROOT) + " '" + name + "'";
    }
}
