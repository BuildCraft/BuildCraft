/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.core.marker.volume;

import java.util.HashMap;
import java.util.Map;

// Yarn 1.20.1: net.minecraft.util.Identifier → net.minecraft.util.Identifier
import net.minecraft.util.Identifier;

public enum AddonsRegistry {
    INSTANCE;

    private final Map<Identifier, Class<? extends Addon>> addonClasses = new HashMap<>();

    public void register(Identifier name, Class<? extends Addon> clazz) {
        if (!addonClasses.containsKey(name)) {
            addonClasses.put(name, clazz);
        }
    }

    public Class<? extends Addon> getClassByName(Identifier name) {
        return addonClasses.get(name);
    }

    public Identifier getNameByClass(Class<? extends Addon> clazz) {
        return addonClasses.entrySet().stream().filter(nameClass -> nameClass.getValue().equals(clazz)).findFirst().orElse(null).getKey();
    }
}
