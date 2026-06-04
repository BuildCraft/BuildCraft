/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): RulesLoader deferred
package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class RulesLoader {

    public static final Set<String> READ_DOMAINS = new HashSet<>();

    public static List<Rule> getRules(Identifier id, NbtCompound nbt) {
        return new ArrayList<>();
    }

    public static class Rule {
        public boolean capture = false;
    }
}
