package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.crops.CropManager;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.lib.misc.FakePlayerProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Plants one item from the robot's held seed stack on the reserved soil block. */
public class AIRobotPlant extends AIRobot {
    private static final int PLANT_DELAY_TICKS = 30;

    private BlockIndex blockFound;
    private int delay;

    public AIRobotPlant(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotPlant(EntityRobotBase robot, BlockIndex blockFound) {
        this(robot);
        this.blockFound = blockFound;
    }

    @Override
    public void start() {
        if (blockFound == null) {
            setSuccess(false);
            terminate();
            return;
        }
        robot.aimItemAt(blockFound.toBlockPos());
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (!(robot.level instanceof ServerLevel serverLevel) || blockFound == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (delay++ <= PLANT_DELAY_TICKS) {
            return;
        }

        ItemStack held = robot.getItemBySlot(EquipmentSlot.MAINHAND);
        if (held.isEmpty()) {
            setSuccess(false);
            terminate();
            return;
        }

        BlockPos pos = blockFound.toBlockPos();
        Player player = FakePlayerProvider.INSTANCE.getBuildCraftPlayer(serverLevel);
        player.setPos(robot.getX(), robot.getY(), robot.getZ());

        ItemStack before = held.copy();
        int beforeCount = held.getCount();
        boolean planted = CropManager.plantCrop(serverLevel, player, held, pos);
        if (!planted) {
            setSuccess(false);
            robot.setItemInUse(before);
            terminate();
            return;
        }

        // Keep the remaining seeds in the robot's hand so the planter can work a whole batch before returning
        // to a station. Some modded plant handlers may not shrink the stack themselves, so consume one item here
        // if the use action reported success but left the count unchanged.
        if (held.getCount() >= beforeCount && ItemStack.isSameItemSameTags(held, before)) {
            held.shrink(1);
        }
        robot.setItemInUse(held.isEmpty() ? ItemStack.EMPTY : held);
        terminate();
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockFound != null) {
            CompoundTag tag = new CompoundTag();
            blockFound.writeTo(tag);
            nbt.put("blockFound", tag);
        }
        nbt.putInt("delay", delay);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockFound")) {
            blockFound = new BlockIndex(nbt.getCompound("blockFound"));
        }
        delay = nbt.getInt("delay");
    }
}
