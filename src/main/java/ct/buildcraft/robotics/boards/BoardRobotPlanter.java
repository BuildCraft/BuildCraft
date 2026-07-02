package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.crops.CropManager;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import ct.buildcraft.lib.inventory.filter.AggregateFilter;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotPlant;
import ct.buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Planter board. Fetches up to 16 matching seeds, then keeps planting from the held stack until it runs out. */
public class BoardRobotPlanter extends RedstoneBoardRobot {
    private BlockIndex blockFound;

    private final IStackFilter seedFilter = stack -> !stack.isEmpty() && CropManager.isSeed(stack);

    public BoardRobotPlanter(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("planter").nbt();
    }

    @Override
    public void update() {
        ItemStack held = robot.getItemBySlot(EquipmentSlot.MAINHAND);
        if (held.isEmpty()) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot,
                    new AggregateFilter(seedFilter, ActionRobotFilter.getGateFilter(robot.getLinkedStation())), 16));
            return;
        }

        final ItemStack seed = held.copy();
        startDelegateAI(new AIRobotSearchAndGotoBlock(robot, true, (level, pos) -> isPlantable(level, seed, pos), 1));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (search.success()) {
                blockFound = search.getBlockFound();
                startDelegateAI(new AIRobotPlant(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotPlant) {
            releaseBlockFound();
        } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    @Override
    public void end() {
        releaseBlockFound();
    }

    private boolean isPlantable(Level level, ItemStack seed, BlockPos pos) {
        if (!level.isLoaded(pos) || seed.isEmpty()) {
            return false;
        }
        if (level.isEmptyBlock(pos) || level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        if (robot.getRegistry() != null && robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
            return false;
        }
        return CropManager.canSustainPlant(level, seed, pos);
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            if (robot.getRegistry() != null) {
                robot.getRegistry().release(new ResourceIdBlock(blockFound));
            }
            blockFound = null;
        }
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockFound != null) {
            CompoundTag tag = new CompoundTag();
            blockFound.writeTo(tag);
            nbt.put("blockFound", tag);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockFound")) {
            blockFound = new BlockIndex(nbt.getCompound("blockFound"));
        }
    }
}
