/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IEntityFilter;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.Lists;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;

public class AIRobotSearchEntity extends AIRobot {

    public Entity target;

    private float maxRange;
    private IZone zone;
    private IEntityFilter filter;

    public AIRobotSearchEntity(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchEntity(EntityRobotBase iRobot, IEntityFilter iFilter, float iMaxRange, IZone iZone) {
        this(iRobot);

        maxRange = iMaxRange;
        zone = iZone;
        filter = iFilter;
    }

    @Override
    public void start() {
        double previousDistance = Double.MAX_VALUE;

        // for (Object o : robot.level.loadedEntityList)
        Collection<Entity> entities;
        if (robot.level instanceof ServerWorld) {
            entities = ((ServerWorld) robot.level).entitiesByUuid.values();
        } else if (robot.level instanceof ClientWorld) {
            entities = ((ClientWorld) robot.level).entitiesById.values();
        } else {
            entities = Lists.newArrayList();
        }
        for (Object o : entities) {
            Entity e = (Entity) o;

            // if (!e.isDead && filter.matches(e) && (zone == null || zone.contains(VecUtil.getVec(e))) && (!robot.isKnownUnreachable(e)))
            if (e.isAlive() && filter.matches(e) && (zone == null || zone.contains(VecUtil.getVec(e))) && (!robot.isKnownUnreachable(e))) {
                double dx = e.getX() - robot.getX();
                double dy = e.getY() - robot.getY();
                double dz = e.getZ() - robot.getZ();

                double sqrDistance = dx * dx + dy * dy + dz * dz;
                double maxDistance = maxRange * maxRange;

                if (sqrDistance >= maxDistance) {
                    continue;
                } else {
                    if (target == null) {
                        previousDistance = sqrDistance;
                        target = e;
                    } else {
                        if (sqrDistance < previousDistance) {
                            previousDistance = sqrDistance;
                            target = e;
                        }
                    }
                }
            }
        }

        terminate();
    }

    @Override
    public boolean success() {
        return target != null;
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 2 * MjAPI.MJ / 10;
    }
}
