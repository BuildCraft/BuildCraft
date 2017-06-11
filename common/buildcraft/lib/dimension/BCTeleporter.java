/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class BCTeleporter extends Teleporter {
    private final int targetX, targetY, targetZ;

    public BCTeleporter(WorldServer worldIn, int targetX, int targetY, int targetZ) {
        super(worldIn);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    @Override
    public void placeInPortal(Entity entityIn, float rotationYaw) {
        entityIn.setPosition(targetX, targetY, targetZ);
    }

    @Override
    public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
        placeInPortal(entityIn, rotationYaw);
        return true;
    }

    @Override
    public boolean makePortal(Entity entityIn) {
        return true;
    }
}
