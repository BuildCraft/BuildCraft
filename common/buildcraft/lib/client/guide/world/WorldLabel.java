/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.world;

import net.minecraft.world.phys.Vec3;

public class WorldLabel {
    public final String label;
    public final double size;
    public final double offset;
    public final Vec3 position;

    public WorldLabel(String label, double size, double offset, Vec3 position) {
        this.label = label;
        this.size = size;
        this.offset = offset;
        this.position = position;
    }
}
