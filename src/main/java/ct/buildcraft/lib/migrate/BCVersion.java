/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.migrate;

public enum BCVersion {
    BEFORE_RECORDS(0, "0.x -> 7.2.0-pre12"),
    v7_2_0_pre_12(1, "7.2.0-pre12 -> 7.x"),
    v8_0_0(2, "8.0.0 -> ?");

    public static final BCVersion CURRENT = v8_0_0;

    public final int dataVersion;
    public final String name;

    BCVersion(int dataTag, String name) {
        this.dataVersion = dataTag;
        this.name = name;
    }

    public static BCVersion getVersion(int version) {
        if (version == 2) return v8_0_0;
        if (version == 1) return v7_2_0_pre_12;
        return BEFORE_RECORDS;
    }
}
