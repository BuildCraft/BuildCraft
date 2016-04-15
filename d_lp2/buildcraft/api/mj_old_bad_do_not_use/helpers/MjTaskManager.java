package buildcraft.api._mj.helpers;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.api._mj.helpers.task.IMjTask;
import buildcraft.api._mj.helpers.task.IMjTaskReadable;
import buildcraft.api._mj.helpers.task.MjTaskRegistry;
import buildcraft.api._mj.helpers.task.MjTaskRegistry.ISimpleMjTaskDeserializer;
import buildcraft.api.core.BCLog;

public class MjTaskManager implements INBTSerializable<NBTTagCompound> {
    private final MjSimpleConsumer powerProvider;

    // ###################################
    //
    // WARNING
    //
    // ###################################
    //
    // THIS IS OVERCOMPLICATED
    //
    // ###################################
    //
    // REMOVE DELTA-MJ AND MJ TASKS
    //
    // ###################################

    // Holds only the non-running tasks, in the order they were added
    private final Queue<IMjTask> waitingTasks = new LinkedList<>();
    // Holds all of the currently running tasks
    private final Queue<IMjTask> runningTasks = new LinkedList<>();
    /** Holds all of the tasks that power has been requested for. */
    private final Queue<IMjTask> requestedTasks = new LinkedList<>();
    /** Holds the amount of power that the task requested. */
    private final Map<IMjTask, Integer> taskPowerWanted = new IdentityHashMap<>();

    public MjTaskManager(MjSimpleConsumer provider) {
        this.powerProvider = provider;
        powerProvider.setCallback(this);
    }

    // ###################################
    //
    // Usage (Public API, use these)
    //
    // ###################################

    public void addTask(IMjTask task) {
        waitingTasks.add(task);
    }

    public void tick() {
        IMjTask toCheck = waitingTasks.poll();
        if (toCheck != null) {
            int wanted = toCheck.requiredMilliWatts();
            if (wanted > 0) {
                powerProvider.addRequest(wanted);
                taskPowerWanted.put(toCheck, wanted);
            } else {
                waitingTasks.add(toCheck);
            }
        }

        for (IMjTask task : waitingTasks) {
            task.tick(false);
        }

        Iterator<IMjTask> iter = runningTasks.iterator();
        while (iter.hasNext()) {
            IMjTask task = iter.next();
            int preWanted = taskPowerWanted.get(task);
            if (preWanted != task.requiredMilliWatts()) {
                iter.remove();
                task.tick(false);
                waitingTasks.add(task);
                taskPowerWanted.remove(task);
            } else {
                task.tick(true);
            }
        }
    }

    // ###################################
    //
    // Usage (Public API, override these)
    //
    // ###################################

    /** Gets the task reader for this task. Used for handling multiple types of task that use additional arguments
     * related to the reader. (for example IBptTask has an additional IBuilder argument) */
    protected String getTaskReaderId(IMjTask task) {
        if (task instanceof IMjTaskReadable) {
            return "exist";
        }
        return "mj";
    }

    protected IMjTask deserializeTask(String reg, String id, String uid, NBTTagCompound data) {
        return null;
    }

    // ###################################
    //
    // NBT reading + writing
    //
    // ###################################

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("waitingTasks", serializeTaskQueue(waitingTasks));
        nbt.setTag("requestedTasks", serializeTaskQueue(requestedTasks));
        nbt.setTag("runningTasks", serializeTaskQueue(runningTasks));
        return nbt;
    }

    private NBTTagList serializeTaskQueue(Queue<IMjTask> taskQueue) {
        NBTTagList list = new NBTTagList();
        for (IMjTask task : taskQueue) {
            NBTTagCompound taskNbt = new NBTTagCompound();
            taskNbt.setString("reg", getTaskReaderId(task));
            taskNbt.setString("id", task.getRegistryName().toString());
            if (task instanceof IMjTaskReadable) {
                IMjTaskReadable readable = (IMjTaskReadable) task;
                taskNbt.setString("uid", readable.getUID());
            }
            taskNbt.setTag("data", task.serializeNBT());
            list.appendTag(taskNbt);
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound taskNbt = list.getCompoundTagAt(i);
            String reg = taskNbt.getString("reg");
            String id = taskNbt.getString("id");
            String uid = taskNbt.getString("uid");
            NBTTagCompound data = taskNbt.getCompoundTag("data");
            IMjTask task = deserializeTaskInternal(reg, id, uid, data);
            if (task != null) {
                tasks.add(task);
            }
        }
    }

    private IMjTask deserializeTaskInternal(String reg, String id, String uid, NBTTagCompound data) {
        if ("mj".equals(reg)) {
            ISimpleMjTaskDeserializer reader = MjTaskRegistry.INSTANCE.getReaderFor(id);
            if (reader == null) {
                BCLog.logger.warn("Unable to deserialize MjTask id " + id);
                return null;
            } else {
                return reader.deserialize(data, this);
            }
        } else if ("exist".equals(reg)) {
            for (IMjTask task : tasks) {
                if (task instanceof IMjTaskReadable) {
                    IMjTaskReadable readable = (IMjTaskReadable) task;
                    if (uid.equals(readable.getUID())) {
                        readable.deserializeNBT(data);
                        return null;
                    }
                }
            }
            BCLog.logger.warn("Unknown existing MjTask id " + id + " uid " + uid);
            return null;
        } else {
            IMjTask other = deserializeTask(reg, id, uid, data);
            if (other != null) return other;
            BCLog.logger.warn("Unknown MjTask registry name " + reg + " for id " + id);
            return null;
        }
    }
}
