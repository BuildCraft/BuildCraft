package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IZone;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.boards.BoardRobotPicker;
import ct.buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AIRobotFetchItem extends AIRobot {
    private ItemEntity target;
    private int targetId = -1;
    private float maxRange = 250;
    private IStackFilter stackFilter;
    private int pickTime = -1;
    private IZone zone;

    public AIRobotFetchItem(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotFetchItem(EntityRobotBase robot, float maxRange, IZone zone) {
        this(robot, maxRange, null, zone);
    }

    public AIRobotFetchItem(EntityRobotBase robot, float maxRange, IStackFilter stackFilter, IZone zone) {
        this(robot);
        this.maxRange = maxRange;
        this.stackFilter = stackFilter;
        this.zone = zone;
    }

    @Override
    public void preempt(AIRobot ai) {
        if (target != null && (!target.isAlive() || target.getItem().isEmpty())) {
            terminate();
        }
    }

    @Override
    public void update() {
        if (target == null && targetId != -1) {
            AABB lookup = robot.getBoundingBox().inflate(maxRange);
            for (ItemEntity item : robot.level.getEntitiesOfClass(ItemEntity.class, lookup)) {
                if (item.getId() == targetId) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null) {
            scanForItem();
        } else {
            if (!canPickTargetNow()) {
                BlockPos pickupPos = findPickupTargetPos(target);
                if (pickupPos == null) {
                    robot.unreachableEntityDetected(target);
                    setSuccess(false);
                    terminate();
                    return;
                }
                startDelegateAI(new AIRobotGotoBlock(robot, pickupPos.getX(), pickupPos.getY(), pickupPos.getZ(), maxRange));
                return;
            }
            pickTime++;
            if (pickTime > 5) {
                ItemStack stack = target.getItem();
                ItemStack remaining = insert(stack, true);
                target.setItem(remaining);
                if (remaining.isEmpty()) {
                    target.discard();
                }
                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (target == null || !target.isAlive() || target.getItem().isEmpty()) {
                setSuccess(false);
                terminate();
            } else if (!ai.success()) {
                robot.unreachableEntityDetected(target);
                setSuccess(false);
                terminate();
            } else {
                pickTime = 0;
            }
        }
    }

    @Override
    public void end() {
        if (targetId != -1) {
            BoardRobotPicker.targettedItems.remove(targetId);
        }
    }

    private void scanForItem() {
        double bestDistance = Double.MAX_VALUE;
        ItemEntity best = null;
        AABB box = robot.getBoundingBox().inflate(maxRange);
        for (ItemEntity item : robot.level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!item.isAlive() || item.getItem().isEmpty()) continue;
            if (BoardRobotPicker.targettedItems.contains(item.getId())) continue;
            if (robot.isKnownUnreachable(item)) continue;
            if (stackFilter != null && !stackFilter.matches(item.getItem())) continue;
            if (zone != null && !zone.contains(new Vec3(item.getX(), item.getY(), item.getZ()))) continue;
            double sqrDistance = item.distanceToSqr(robot);
            if (sqrDistance >= maxRange * maxRange) continue;
            if (insert(item.getItem(), false).getCount() >= item.getItem().getCount()) continue;
            if (best == null || sqrDistance < bestDistance) {
                best = item;
                bestDistance = sqrDistance;
            }
        }
        if (best != null) {
            target = best;
            targetId = best.getId();
            BoardRobotPicker.targettedItems.add(targetId);
            if (!canPickTargetNow()) {
                BlockPos pickupPos = findPickupTargetPos(target);
                if (pickupPos == null) {
                    robot.unreachableEntityDetected(target);
                    setSuccess(false);
                    terminate();
                    return;
                }
                startDelegateAI(new AIRobotGotoBlock(robot, pickupPos.getX(), pickupPos.getY(), pickupPos.getZ(), maxRange));
            }
        } else {
            setSuccess(false);
            terminate();
        }
    }

    private boolean canPickTargetNow() {
        if (target == null) {
            return false;
        }
        if (Math.floor(target.getX()) == Math.floor(robot.getX())
                && Math.floor(target.getY()) == Math.floor(robot.getY())
                && Math.floor(target.getZ()) == Math.floor(robot.getZ())) {
            return true;
        }
        return target.distanceToSqr(robot) <= 2.25D;
    }

    private BlockPos findPickupTargetPos(ItemEntity item) {
        BlockPos itemPos = new BlockPos((int) Math.floor(item.getX()), (int) Math.floor(item.getY()), (int) Math.floor(item.getZ()));
        BlockPos[] candidates = new BlockPos[] {
                itemPos,
                itemPos.above(),
                itemPos.north(),
                itemPos.south(),
                itemPos.west(),
                itemPos.east(),
                itemPos.above().north(),
                itemPos.above().south(),
                itemPos.above().west(),
                itemPos.above().east()
        };

        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : candidates) {
            if (!isSoft(robot.level, candidate)) {
                continue;
            }
            double distance = candidate.distToCenterSqr(item.getX(), item.getY(), item.getZ());
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static boolean isSoft(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
    }

    private ItemStack insert(ItemStack stack, boolean doInsert) {
        if (robot instanceof EntityRobot entityRobot) {
            return entityRobot.insertIntoInventory(stack, doInsert);
        }
        return stack;
    }

    @Override
    public int getEnergyCost() {
        return 15;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        nbt.putFloat("maxRange", maxRange);
        nbt.putInt("targetId", targetId);
        nbt.putInt("pickTime", pickTime);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        maxRange = nbt.contains("maxRange") ? nbt.getFloat("maxRange") : 250;
        targetId = nbt.getInt("targetId");
        pickTime = nbt.getInt("pickTime");
    }
}
