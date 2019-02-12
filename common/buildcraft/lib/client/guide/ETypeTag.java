/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

public enum ETypeTag {
    MOD("mod."),
    SUB_MOD("submod."),
    TYPE("type."),
    SUB_TYPE("subtype.");

    public final String preText;

    ETypeTag(String preText) {
        this.preText = "buildcraft.guide.chapter." + preText;
    }
}
