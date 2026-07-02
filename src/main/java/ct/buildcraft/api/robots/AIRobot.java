/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.robots;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class AIRobot {
    public final EntityRobotBase robot;

    private AIRobot delegateAI;
    private AIRobot parentAI;
    private boolean success;

    public AIRobot(EntityRobotBase robot) {
        this.robot = robot;
        this.success = true;
    }

    public void start() {
    }

    public void preempt(AIRobot ai) {
    }

    public void update() {
        // Update should always handle terminate. Some AIs only use start/end; if update is called on them,
        // terminating is the safest default.
        terminate();
    }

    public void end() {
    }

    /** Called when a delegate AI ends work naturally. */
    public void delegateAIEnded(AIRobot ai) {
    }

    /** Called when a delegate AI is forcibly aborted. */
    public void delegateAIAborted(AIRobot ai) {
    }

    public void writeSelfToNBT(CompoundTag nbt) {
    }

    public void loadSelfFromNBT(CompoundTag nbt) {
    }

    public boolean success() {
        return success;
    }

    protected void setSuccess(boolean success) {
        this.success = success;
    }

    public int getEnergyCost() {
        return 1;
    }

    public boolean canLoadFromNBT() {
        return false;
    }

    /** Tries to receive items and returns the items left after the operation. */
    public ItemStack receiveItem(ItemStack stack) {
        return stack;
    }

    public final void terminate() {
        abortDelegateAI();
        end();

        if (parentAI != null) {
            parentAI.delegateAI = null;
            parentAI.delegateAIEnded(this);
        }
    }

    public final void abort() {
        abortDelegateAI();

        try {
            end();

            if (parentAI != null) {
                parentAI.delegateAI = null;
                parentAI.delegateAIAborted(this);
            }
        } catch (Throwable throwable) {
            ct.buildcraft.api.core.BCLog.logger.error("Robot AI abort failed for " + getClass().getName(), throwable);
            delegateAI = null;

            if (parentAI != null) {
                parentAI.delegateAI = null;
            }
        }
    }

    public final void cycle() {
        try {
            preempt(delegateAI);

            if (delegateAI != null) {
                delegateAI.cycle();
            } else {
                robot.getBattery().extractPower(getEnergyCost());
                update();
            }
        } catch (Throwable throwable) {
            ct.buildcraft.api.core.BCLog.logger.error("Robot AI cycle failed for " + getClass().getName(), throwable);
            abort();
        }
    }

    public final void startDelegateAI(AIRobot ai) {
        abortDelegateAI();
        delegateAI = ai;
        ai.parentAI = this;
        delegateAI.start();
    }

    public final void abortDelegateAI() {
        if (delegateAI != null) {
            delegateAI.abort();
        }
    }

    public final AIRobot getActiveAI() {
        if (delegateAI != null) {
            return delegateAI.getActiveAI();
        }
        return this;
    }

    public final AIRobot getDelegateAI() {
        return delegateAI;
    }

    public final void writeToNBT(CompoundTag nbt) {
        nbt.putString("aiName", RobotManager.getAIRobotName(getClass()));

        CompoundTag data = new CompoundTag();
        writeSelfToNBT(data);
        nbt.put("data", data);

        if (delegateAI != null && delegateAI.canLoadFromNBT()) {
            CompoundTag sub = new CompoundTag();
            delegateAI.writeToNBT(sub);
            nbt.put("delegateAI", sub);
        }
    }

    public final void loadFromNBT(CompoundTag nbt) {
        loadSelfFromNBT(nbt.getCompound("data"));

        if (nbt.contains("delegateAI")) {
            CompoundTag sub = nbt.getCompound("delegateAI");

            try {
                Class<?> aiRobotClass;
                if (sub.contains("class")) {
                    aiRobotClass = RobotManager.getAIRobotByLegacyClassName(sub.getString("class"));
                } else {
                    aiRobotClass = RobotManager.getAIRobotByName(sub.getString("aiName"));
                }
                if (aiRobotClass != null) {
                    delegateAI = (AIRobot) aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                    delegateAI.parentAI = this;

                    if (delegateAI.canLoadFromNBT()) {
                        delegateAI.loadFromNBT(sub);
                    }
                }
            } catch (Throwable throwable) {
                ct.buildcraft.api.core.BCLog.logger.warn("Failed to load delegate robot AI from NBT", throwable);
            }
        }
    }

    public static AIRobot loadAI(CompoundTag nbt, EntityRobotBase robot) {
        AIRobot ai = null;

        try {
            Class<?> aiRobotClass;
            if (nbt.contains("class")) {
                aiRobotClass = RobotManager.getAIRobotByLegacyClassName(nbt.getString("class"));
            } else {
                aiRobotClass = RobotManager.getAIRobotByName(nbt.getString("aiName"));
            }
            if (aiRobotClass != null) {
                ai = (AIRobot) aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                if (ai.canLoadFromNBT()) {
                    ai.loadFromNBT(nbt);
                }
            }
        } catch (Throwable throwable) {
            ct.buildcraft.api.core.BCLog.logger.warn("Failed to load robot AI from NBT", throwable);
        }

        return ai;
    }
}
