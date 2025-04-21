/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.robotics.boards.BoardRobotPicker;
import com.google.common.collect.Lists;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;

public class AIRobotFetchItem extends AIRobot {

    private ItemEntity target;

    private float maxRange;
    private IStackFilter stackFilter;
    private int pickTime = -1;
    private IZone zone;

    public AIRobotFetchItem(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotFetchItem(EntityRobotBase iRobot, float iMaxRange, IStackFilter iStackFilter, IZone iZone) {
        this(iRobot);

        maxRange = iMaxRange;
        stackFilter = iStackFilter;
        zone = iZone;
    }

    @Override
    public void preempt(AIRobot ai) {
//        if (target != null && target.isDead)
        if (target != null && !target.isAlive()) {
            terminate();
        }
    }

    @Override
    public void update() {
        if (target == null) {
            scanForItem();
        } else {
            pickTime++;

            if (pickTime > 5) {
                // TransactorSimple inventoryInsert = new TransactorSimple(robot);
                IItemHandler inventoryInsert = robot.getCapability(CapUtil.CAP_ITEMS).orElse(null);

                // target.getEntityItem().stackSize -= inventoryInsert.inject(target.getItem(), null, true);
                target.setItem(InventoryUtil.insert(inventoryInsert, target.getItem(), false));

                if (target.getItem().getCount() <= 0) {
                    target.kill();
                }

                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (target == null) {
                // This would happen after a load. As we reached the item
                // location already, just consider that the item is not there
                // anymore and allow user to try to find another one.
                setSuccess(false);
                terminate();
                return;
            }

            if (!ai.success()) {
                robot.unreachableEntityDetected(target);
                setSuccess(false);
                terminate();
            }
        }
    }

    @Override
    public void end() {
        if (target != null) {
            BoardRobotPicker.targettedItems.remove(target.getId());
        }
    }

    private void scanForItem() {
        double previousDistance = Double.MAX_VALUE;
        // TransactorSimple inventoryInsert = new TransactorSimple(robot);
        IItemHandler inventoryInsert = robot.getCapability(CapUtil.CAP_ITEMS).orElse(null);

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

            if (e.isAlive() && e instanceof ItemEntity && !BoardRobotPicker.targettedItems.contains(e.getId()) && !robot.isKnownUnreachable(e)
                    && (zone == null || zone.contains(VecUtil.getVec(e)))) {
                double dx = e.getX() - robot.getX();
                double dy = e.getY() - robot.getY();
                double dz = e.getZ() - robot.getZ();

                double sqrDistance = dx * dx + dy * dy + dz * dz;
                double maxDistance = maxRange * maxRange;

                if (sqrDistance >= maxDistance) {
                    continue;
                } else if (stackFilter != null && !stackFilter.matches(((ItemEntity) e).getItem())) {
                    continue;
                } else {
                    ItemEntity item = (ItemEntity) e;

                    // if (inventoryInsert.inject(item.getItem(), null, false) > 0)
                    if (item.getItem().getCount() > InventoryUtil.insert(inventoryInsert, item.getItem(), true).getCount()) {
                        if (target == null) {
                            previousDistance = sqrDistance;
                            target = item;
                        } else {
                            if (sqrDistance < previousDistance) {
                                previousDistance = sqrDistance;
                                target = item;
                            }
                        }
                    }
                }
            }
        }

        if (target != null) {
            BoardRobotPicker.targettedItems.add(target.getId());
            if (Math.floor(target.getX()) != Math.floor(robot.getX()) || Math.floor(target.getY()) != Math.floor(robot.getY()) || Math.floor(target.getZ()) != Math.floor(robot.getZ())) {
                startDelegateAI(new AIRobotGotoBlock(robot, VecUtil.getPos(target), true));
            }
        } else {
            // No item was found, terminate this AI
            setSuccess(false);
            terminate();
        }
    }

    @Override
    public long getPowerCost() {
//        return 15;
        return MjAPI.MJ * 15 / 10;
    }
}
