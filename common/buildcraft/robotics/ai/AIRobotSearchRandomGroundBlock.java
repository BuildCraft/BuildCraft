/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IBlockFilter;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AIRobotSearchRandomGroundBlock extends AIRobot {

    private static final int MAX_ATTEMPTS = 4096;

    public BlockPos blockFound;

    private int range;
    private IBlockFilter filter;
    private IZone zone;
    private int attempts = 0;

    public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot, int iRange, IBlockFilter iFilter, IZone iZone) {
        this(iRobot);

        range = iRange;
        filter = iFilter;
        zone = iZone;
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
        }

        attempts++;

        if (attempts > MAX_ATTEMPTS) {
            terminate();
        }

        int x, z;

        if (zone == null) {
            double r = robot.level.random.nextFloat() * range;
            float a = robot.level.random.nextFloat() * 2.0F * (float) Math.PI;

            x = (int) (MathHelper.cos(a) * r + Math.floor(robot.getX()));
            z = (int) (MathHelper.sin(a) * r + Math.floor(robot.getZ()));
        } else {
            BlockPos b = zone.getRandomBlockPos(robot.level.random);
            x = b.getX();
            z = b.getZ();
        }

        BlockPos pos = new BlockPos(x, robot.level.getHeight(), z);
        for (; pos.getY() >= 0; pos = pos.below()) {
            if (filter.matches(robot.level, pos)) {
                blockFound = new BlockPos(pos);
                terminate();
                return;
            } else if (!robot.level.isEmptyBlock(pos)) {
                return;
            }
        }
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 2 * MjAPI.MJ / 10;
    }
}
