/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.boards;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import ct.buildcraft.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobotNBT extends RedstoneBoardNBT<EntityRobotBase> {
    @Override
    public RedstoneBoardRobot create(CompoundTag nbt, EntityRobotBase robot) {
        return create(robot);
    }

    public abstract RedstoneBoardRobot create(EntityRobotBase robot);

    public abstract ResourceLocation getRobotTexture();
}
