/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.world;

import com.google.common.collect.ImmutableList;
import org.joml.Vector3f;

import java.util.List;

public class WorldState {
    private final Vector3f cameraPos;
    private final double cameraYaw, cameraPitch;
    // private final Vec3i size;
    // private final FakeWorld world;
    private final List<WorldLabel> labels;

    public WorldState(WorldInfo info) {
        byte[] schematic = info.getSchematic();
        // BlueprintBase blueprint = BlueprintBase.loadBluePrint(NBTUtils.load(schematic));
        // this.size = blueprint.size;
        // if (blueprint instanceof Blueprint) {
        // this.world = new FakeWorld((Blueprint) blueprint);
        // } else {
        // this.world = new FakeWorld((Template) blueprint, Blocks.BRICK_BLOCK.getDefaultState());
        // }
        this.labels = ImmutableList.copyOf(info.labels);
        this.cameraPos = null;// TODO Utils.convertFloat(info.cameraPos);

        double xDiff = cameraPos.x - info.cameraFacing.x;
        double yDiff = cameraPos.y - info.cameraFacing.y;
        double zDiff = cameraPos.z - info.cameraFacing.z;

        double opposite = Math.abs(yDiff);
        double adjacent = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

        cameraPitch = Math.atan2(adjacent, opposite);
        cameraYaw = Math.atan2(zDiff, xDiff) - Math.PI / 2;

    }
}
