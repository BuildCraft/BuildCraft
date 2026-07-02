/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.robots;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

public abstract class ResourceId {
    protected ResourceId() {
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putString("resourceName", RobotManager.getResourceIdName(getClass()));
    }

    protected void readFromNBT(CompoundTag nbt) {
    }

    @Nullable
    public static ResourceId load(CompoundTag nbt) {
        try {
            Class<?> cls;
            if (nbt.contains("class")) {
                cls = RobotManager.getResourceIdByLegacyClassName(nbt.getString("class"));
            } else {
                cls = RobotManager.getResourceIdByName(nbt.getString("resourceName"));
            }

            if (cls == null) {
                return null;
            }

            ResourceId id = (ResourceId) cls.getDeclaredConstructor().newInstance();
            id.readFromNBT(nbt);
            return id;
        } catch (Throwable throwable) {
            ct.buildcraft.api.core.BCLog.logger.warn("Failed to load robot resource id from NBT", throwable);
        }

        return null;
    }
}
